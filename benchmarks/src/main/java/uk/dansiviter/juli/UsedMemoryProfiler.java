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
package uk.dansiviter.juli;

import java.util.ArrayList;
import java.util.Collection;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;

public class UsedMemoryProfiler implements InternalProfiler {
	private final Runtime runtime = Runtime.getRuntime();

	@Override
	public String getDescription() {
		return "Used memory heap profiler";
	}

	@Override
	public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
		// nothing to see here
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams,
		IterationResult result) {

		double used = runtime.totalMemory() - runtime.freeMemory();

		Collection<ScalarResult> results = new ArrayList<>();
		results.add(new ScalarResult("Used memory heap", used / 1024, "KB", AggregationPolicy.AVG));

		return results;
	}
}
