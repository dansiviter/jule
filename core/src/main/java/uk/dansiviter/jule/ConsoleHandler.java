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

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * A improvement on {@link java.util.logging.ConsoleHandler} where uses {@code STDOUT}.
 */
public class ConsoleHandler extends StreamHandler {
	/**
	 * Default constructor that uses permits setting of output.
	 */
	public ConsoleHandler() {
		super(System.out, new SimpleFormatter());
	}

	@Override
	public synchronized void publish(LogRecord record) {
		super.publish(record);
		flush();
	}
}
