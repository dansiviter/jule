package uk.dansiviter.logging;

import static java.util.concurrent.ForkJoinPool.commonPool;

import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.ErrorManager;
import java.util.logging.LogRecord;
import java.util.concurrent.SubmissionPublisher;


public class DefaultEmitter implements Emitter<LogRecord> {
	private final Subscriber<LogRecord> subscriber = new LogSubscriber();

	private AsyncHandler handler;
	private SubmissionPublisher<LogRecord> delegate;

	@Override
	public void init(AsyncHandler handler) {
		this.handler = handler;
		var maxBuffer = handler.property("maxBuffer").map(Integer::parseInt).orElseGet(Flow::defaultBufferSize);
		this.delegate = new SubmissionPublisher<>(commonPool(), maxBuffer);
		this.delegate.subscribe(this.subscriber);
	}

	@Override
	public int submit(LogRecord item) {
		return this.delegate.submit(item);
	}

	@Override
	public void close() {
		this.delegate.close();
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
			handler.doPublish(item);
			this.subscription.request(1);
		}

		@Override
		public void onError(Throwable t) {
			handler.getErrorManager().error(t.getMessage(), new Exception(t), ErrorManager.GENERIC_FAILURE);
		}

		@Override
		public void onComplete() {
			// Nothing to see here
		}
	}
}
