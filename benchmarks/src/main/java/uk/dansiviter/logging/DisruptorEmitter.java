package uk.dansiviter.logging;

import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.logging.ErrorManager;
import java.util.logging.LogRecord;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;


public class DisruptorEmitter implements Emitter<LogRecord> {
	private static final int MAX_DRAIN = 5;
	private AsyncHandler handler;
	private Disruptor<LogEvent> disruptor;

	@Override
	public void init(AsyncHandler handler) {
		this.handler = handler;
		var maxBuffer = handler.property("maxBuffer").map(Integer::parseInt).orElseGet(Flow::defaultBufferSize);
		this.disruptor = new Disruptor<>(LogEvent::new, maxBuffer, DaemonThreadFactory.INSTANCE); //, ProducerType.MULTI, new YieldingWaitStrategy());
		this.disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
			this.handler.doPublish(event.record);
		});
		this.disruptor.start();
	}

	@Override
	public int submit(LogRecord item) {
		this.disruptor.getRingBuffer().publishEvent((e, sequence, r) -> e.record = r, item);
		return 1;
	}

	@Override
	public void close() {

		try {
			for (int i = 0; hasBacklog(this.disruptor.getRingBuffer()) && i < MAX_DRAIN; i++) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			this.handler.getErrorManager().error("Drain interrupted!", e, ErrorManager.CLOSE_FAILURE);
			return;
		}

		try {
			disruptor.shutdown(10, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			this.handler.getErrorManager().error("Shutdown timed out!", e, ErrorManager.CLOSE_FAILURE);
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
