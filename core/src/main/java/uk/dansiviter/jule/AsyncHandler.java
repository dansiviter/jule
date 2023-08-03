/*
 * Copyright 2023 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.dansiviter.jule;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.ErrorManager.CLOSE_FAILURE;
import static java.util.logging.ErrorManager.GENERIC_FAILURE;
import static java.util.logging.ErrorManager.OPEN_FAILURE;
import static java.util.logging.ErrorManager.WRITE_FAILURE;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * An abstract {@link Handler} that asynchronously delivers log messages. This leverages
 * {@link java.util.concurrent.Flow} to handle log events. Back-pressure is handled by blocking the
 * calling thread if the buffer is full. Therefore, to avoid a significant thread hang ensure the processing is done in
 * a timely manner.
 * <p>
 * <b>Configuration:</b>
 * Using the following {@code LogManager} configuration properties, where {@code <handler-name>} refers to the
 * fully-qualified class name of the handler:
 * <ul>
 * <li>   {@code &lt;handler-name&gt;.level}
 *        specifies the default level for the {@code Handler}
 *        (defaults to {@code INFO}). </li>
 * <li>   {@code &lt;handler-name&gt;.filter}
 *        specifies the name of a {@code Filter} class to use
 *        (defaults to no {@code Filter}). </li>
 * <li>   {@code &lt;handler-name&gt;.formatter}
 *        specifies the name of a {@code Formatter} class to use
 *        (defaults to {@link java.util.logging.SimpleFormatter}). </li>
 * <li>   {@code &lt;handler-name&gt;.encoding}
 *        the name of the character set encoding to use (defaults to
 *        the default platform encoding). </li>
 * <li>   {@code &lt;handler-name&gt;.maxBuffer}
 *        specifies the maximum buffer size level for the handler
 *        (defaults to {@link java.util.concurrent.Flow#defaultBufferSize()}). </li>
 * </ul>
 */
public abstract class AsyncHandler extends AbstractHandler {
	private static final LogRecord FLUSH = new LogRecord(Level.OFF, "flush");
	private final Subscriber<LogRecord> subscriber = new LogSubscriber();
	private final SubmissionPublisher<LogRecord> publisher;
	/** Closed status */
	protected final AtomicBoolean closed = new AtomicBoolean();

	private ExecutorService executorService;

	/**
	 * Create an asynchronous {@code Handler} and configure it based on {@code LogManager} configuration properties.
	 */
	protected AsyncHandler() {
		setLevel(property("level").map(Level::parse).orElse(Level.INFO));
		setFilter(property("filter").map(JulUtil::<Filter>newInstance).orElse(null));
		setFormatter(property("formatter").map(JulUtil::<Formatter>newInstance).orElseGet(SimpleFormatter::new));
		try {
			setEncoding(property("encoding").orElse(null));
		} catch (UnsupportedEncodingException e) {
			getErrorManager().error(e.getMessage(), e, OPEN_FAILURE);
		}

		var maxBuffer = property("maxBuffer").map(Integer::parseInt).orElseGet(Flow::defaultBufferSize);
		this.publisher = new SubmissionPublisher<>(this.executorService = newSingleThreadExecutor(r -> new Thread(r, getClass().getSimpleName())), maxBuffer);
		this.publisher.subscribe(this.subscriber);
	}

	@Override
	public void publish(LogRecord r) {
		if (isClosed()) {
			throw new IllegalStateException("Closed!");
		}
		if (!isLoggable(r)) {
			return;
		}

		r.getSourceClassName();  // ensure source is populated

		this.publisher.submit(transform(r));
	}

	@Override
	public void flush() {
		this.publisher.submit(FLUSH);
	}

	/**
	 * Perform any pre-flight transformation of this record. This will be called on the log calling thread.
	 *
	 * @param r the record to transform.
	 * @return the transformed record.
	 */
	protected LogRecord transform(LogRecord r) {
		return r;
	}

	/**
	 * This will be called asynchronously.
	 *
	 * @param r the log record to process.
	 */
	protected abstract void doPublish(LogRecord r);

	/**
	 * This will be called asynchronously.
	 */
	protected void doFlush() { }

	/**
	 * @return {@code true} if closed.
	 */
	public boolean isClosed() {
		return this.closed.get();
	}

	@Override
	public void close() throws SecurityException {
		if (!this.closed.compareAndSet(false, true)) {
			throw new IllegalStateException("Already closed!");
		}
		this.publisher.close();
		shutdown(this.executorService);
	}

	private void shutdown(ExecutorService executor) {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(5, SECONDS)) {
				executor.shutdownNow();
				if (!executor.awaitTermination(5, SECONDS)) {
					getErrorManager().error("Executor did not terminate within timeout!", null, CLOSE_FAILURE);
				}
			}
		} catch (InterruptedException ie) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
    }
	}

	public ThreadFactory threadFactory() {
		return ;
	}


	// --- Inner Classes ---

	/**
	 *
	 */
	private class LogSubscriber implements Subscriber<LogRecord> {
		private Subscription subscription;

		@Override
		public void onSubscribe(Subscription subscription) {
			this.subscription = subscription;
			this.subscription.request(1);
		}

		@Override
		public void onNext(LogRecord item) {
			try {
				if (item == FLUSH) {
					doFlush();
				}

				doPublish(item);
			} catch (RuntimeException e) {
				getErrorManager().error(e.getMessage(), e, WRITE_FAILURE);
			} finally {
				this.subscription.request(1);
			}
		}

		@Override
		public void onError(Throwable t) {
			getErrorManager().error(t.getMessage(), new Exception(t), GENERIC_FAILURE);
			close();  // this handler is effectively dead, so prevent other log messages
		}

		@Override
		public void onComplete() {
			// Nothing to see here
		}
	}
}
