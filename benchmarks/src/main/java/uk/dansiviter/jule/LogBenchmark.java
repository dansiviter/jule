/*
 * Copyright 2022 Daniel Siviter
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

import java.util.logging.Logger;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message;
import uk.dansiviter.jule.annotations.Message.Level;

public class LogBenchmark {
	@State(Scope.Benchmark)
	public static class TestState {
		BenchmarkLog log;

		@Setup
		public void setup() {
			log = LogProducer.log(BenchmarkLog.class, LogBenchmark.class);
		}
	}

	@State(Scope.Benchmark)
	public static class LegacyState {
		Logger log;

		@Setup
		public void setup() {
			log = Logger.getLogger(LogBenchmark.class.getSimpleName());
		}
	}

	@Benchmark
	public void newLog(TestState state) {
		state.log.hello(1);
		state.log.debugHello(2);
	}

	@Benchmark
	public void legLog(LegacyState state) {
		if (state.log.isLoggable(java.util.logging.Level.INFO)) {
			state.log.log(java.util.logging.Level.INFO, "Hello %d", 1);
		}
		if (state.log.isLoggable(java.util.logging.Level.FINE)) {
			state.log.log(java.util.logging.Level.FINE, "Hello %d", 2);
		}
	}

	@Log
	public interface BenchmarkLog {
		@Message("Hello %d")
		void hello(int i);

		@Message(value = "Hello %d", level = Level.DEBUG)
		void debugHello(int i);
	}
}
