/*
 * Copyright 2021 Daniel Siviter
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
package uk.dansiviter.logging;

import static java.lang.String.format;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.annotation.Nonnull;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

/**
 * An abstract {@link Handler} that asynchronously delivers log messages. This
 * leverages {@link java.util.concurrent.Flow} to handle log events.
 * Back-pressure is handled by blocking the calling thread if the buffer is
 * full. Therefore, to avoid a significant thread hang ensure the processing is
 * done in a timely manner.
 * </p>
 * <b>Configuration:</b> Using the following {@code LogManager} configuration
 * properties, where {@code <handler-name>} refers to the fully-qualified class
 * name of the handler:
 * <ul>
 * <li>{@code &lt;handler-name&gt;.level} specifies the default level for the
 * {@code Handler} (defaults to {@code INFO}).</li>
 * <li>{@code &lt;handler-name&gt;.filter} specifies the name of a
 * {@code Filter} class to use (defaults to no {@code Filter}).</li>
 * <li>{@code &lt;handler-name&gt;.formatter} specifies the name of a
 * {@code Formatter} class to use (defaults to
 * {@link java.util.logging.SimpleFormatter}).</li>
 * <li>{@code &lt;handler-name&gt;.encoding} the name of the character set
 * encoding to use (defaults to the default platform encoding).</li>
 * <li>{@code &lt;handler-name&gt;.maxBuffer} specifies the maximum buffer size
 * level for the handler (defaults to
 * {@link java.util.concurrent.Flow#defaultBufferSize()}).</li>
 * </ul>
 */
public abstract class AsyncHandler extends Handler {
	private static final int MAX_DRAIN = 5;
	private final Disruptor<LogEvent> disruptor;

	protected final AtomicBoolean closed = new AtomicBoolean();

	public AsyncHandler() {
		var manager = Objects.requireNonNull(LogManager.getLogManager());

		setLevel(property(manager, "level").map(Level::parse).orElse(Level.INFO));
		setFilter(property(manager, "filter").map(AsyncHandler::<Filter>instance).orElse(null));
		setFormatter(property(manager, "formatter").map(AsyncHandler::<Formatter>instance).orElseGet(SimpleFormatter::new));
		try {
			setEncoding(property(manager, "encoding").orElse(null));
		} catch (UnsupportedEncodingException e) {
			getErrorManager().error(e.getMessage(), e, ErrorManager.OPEN_FAILURE);
		}

		var maxBuffer = property(manager, "maxBuffer").map(Integer::parseInt).orElse(1024);

		this.disruptor = new Disruptor<>(LogEvent::new, maxBuffer, DaemonThreadFactory.INSTANCE); //, ProducerType.MULTI, new YieldingWaitStrategy());
		this.disruptor.handleEventsWith((event, sequence, endOfBatch) -> doPublish(event.record));
		this.disruptor.start();
	}

	/**
	 * Extracts the {@link LogManager#getProperty(String)}.
	 *
	 * @param manager the manager instance.
	 * @param name    the name of the property.
	 * @return the value as an {@link Optional}.
	 */
	protected Optional<String> property(@Nonnull LogManager manager, @Nonnull String name) {
		return Optional.ofNullable(manager.getProperty(getClass().getName() + "." + name));
	}

	// --- Static Methods ---

	@SuppressWarnings("unchecked")
	public static @Nonnull <T> T instance(@Nonnull String name) {
		try {
			Class<?> concreteCls = Class.forName(name);
			return (T) concreteCls.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(format("Unable to create! [%s]", name), e);
		}
	}

	@Override
	public void publish(LogRecord record) {
		if (this.closed.get()) {
			throw new IllegalStateException("Handler closed!");
		}

		if (!isLoggable(record)) {
			return;
		}

		record.getSourceClassName(); // ensure source is populated

		this.disruptor.getRingBuffer().publishEvent((e, sequence, r) -> e.record = r, record);
	}

	/**
	 * This will be called asynchronously.
	 *
	 * @param record the log record to process.
	 */
	protected abstract void doPublish(LogRecord record);

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
		if (!closed.compareAndSet(false, true)) {
			throw new IllegalStateException("Already closed!");
		}

		try {
			for (int i = 0; hasBacklog(this.disruptor.getRingBuffer()) && i < MAX_DRAIN; i++) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			getErrorManager().error("Drain interrupted!", e, ErrorManager.CLOSE_FAILURE);
			return;
		}

		try {
			disruptor.shutdown(10, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			getErrorManager().error("Shutdown timed out!", e, ErrorManager.CLOSE_FAILURE);
			this.disruptor.halt();
		}
	}

	// --- Static Methods ---

	private static boolean hasBacklog(RingBuffer<?> buf) {
		return !buf.hasAvailableCapacity(buf.getBufferSize());
	}

	// --- Inner Classes ---

	private static class LogEvent {
		private LogRecord record;
	}
}
