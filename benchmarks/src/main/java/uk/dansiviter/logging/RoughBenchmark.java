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

import static java.lang.Thread.sleep;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class RoughBenchmark {
	public static void main(String[] args) throws InterruptedException {
		var root = Logger.getLogger("");
		for (Handler h : root.getHandlers()) {
			root.removeHandler(h);
		}

		var syncHandler = new ConsoleHandler();
		var asyncHandler = new DisruptorAsyncConsoleHandler();

		var log = Logger.getLogger("TEST");

		int[] iterations = { 50, 100, 200, 400, 800, 1_600, 3_200, 6_400, 12_800 };

		for (int i : iterations) {  // warm up
			test(log, syncHandler, "SYNC-", i);
			test(log, asyncHandler, "ASYNC", i);
		}

		sleep(1_000);  // wait for async to finish before writing results

		var results = new ArrayList<Result>();
		for (int i : iterations) {
			results.add(new Result(i,
					test(log, syncHandler, "SYNC-", i),
					test(log, asyncHandler, "ASYNC", i)));
		}

		sleep(1_000);  // wait for async to finish before writing results
		for (Result r : results) {
			var sync = toBigDecimalSeconds(r.sync);
			var async = toBigDecimalSeconds(r.async);
			var percent = async.compareTo(BigDecimal.ZERO) != 0 ? sync.divide(async, MathContext.DECIMAL32).multiply(BigDecimal.valueOf(100)) : null;
			System.out.printf("x%d - Sync=%s, Async=%s (%s%%)\n", r.iterations, r.sync, r.async, percent != null ? percent : "NaN");
		}
	}

	private static Duration test(Logger log, Handler handler, String message, int iterations)
			throws InterruptedException
	{
		log.addHandler(handler);
		sleep(250);
		Instant start = Instant.now();
		for (int i = 0; i < iterations; i++) {
			log.info(message);
		}
		Instant end = Instant.now();
		log.removeHandler(handler);
		return Duration.between(start, end);
	}

	private static BigDecimal toBigDecimalSeconds(Duration d) {
		return BigDecimal.valueOf(d.toSecondsPart()).add(BigDecimal.valueOf(d.toNanosPart(), 9));
	}

	private static class Result {
		final int iterations;
		final Duration sync;
		final Duration async;

		Result(int iterations, Duration sync, Duration async) {
			this.iterations = iterations;
			this.sync = sync;
			this.async = async;
		}
	}
}
