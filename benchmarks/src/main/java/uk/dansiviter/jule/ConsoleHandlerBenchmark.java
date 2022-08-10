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
package uk.dansiviter.jule;

import java.util.logging.Handler;
import java.util.logging.Logger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/**
 *
 */

public class ConsoleHandlerBenchmark {
	@State(Scope.Benchmark)
	public static class BenchmarkState {
		@Param({ "java.util.logging.ConsoleHandler", "uk.dansiviter.jule.AsyncConsoleHandler" })
		public String handlerName;

		private Logger log;
		private Handler handler;

		@Setup(Level.Trial)
		public void doSetup() throws ReflectiveOperationException {
			var root = Logger.getLogger("");
			for (Handler h : root.getHandlers()) {
				root.removeHandler(h);
			}
			handler = (Handler) Class.forName(handlerName).getConstructor().newInstance();
			log = Logger.getLogger("TEST");
			log.addHandler(handler);
		}

		@TearDown(Level.Trial)
		public void doTeardown() {
			log.removeHandler(handler);
		}
	}

	@Benchmark
	public void handler(BenchmarkState state) {
		state.log.info("Hello world");
	}
}
