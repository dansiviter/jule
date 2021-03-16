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

import static org.openjdk.jmh.runner.options.TimeValue.milliseconds;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class JmhMain {
	public static void main(String[] args) throws RunnerException {
		var opt = new OptionsBuilder()
				.include(JmhMain.class.getPackage().getName())
				.warmupTime(milliseconds(500))
				.measurementTime(milliseconds(500))
				.shouldDoGC(true)
				.addProfiler(UsedMemoryProfiler.class)
				.build();
		new Runner(opt).run();
	}
}
