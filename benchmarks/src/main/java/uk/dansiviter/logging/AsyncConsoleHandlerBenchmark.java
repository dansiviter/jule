package uk.dansiviter.logging;

import java.util.logging.Handler;
import java.util.logging.Logger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 *
 */

public class AsyncConsoleHandlerBenchmark {
	@State(Scope.Benchmark)
	public static class MyState {
		private Logger log = Logger.getLogger("TEST");
		private AsyncConsoleHandler handler = new AsyncConsoleHandler();

		@Setup(Level.Trial)
		public void doSetup() {
			var root = Logger.getLogger("");
			for (Handler h : root.getHandlers()) {
				root.removeHandler(h);
			}
			log.addHandler(handler);
		}
	}

	@Benchmark
	public void publish(MyState state) {
		state.log.info("Hello world");
	}
}
