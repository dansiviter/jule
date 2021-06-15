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

import static uk.dansiviter.juli.AbstractHandler.property;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;

/**
 * Variant of ConsoleHandler where I can actually set if it uses {@code STDOUT} or {@code STDERR}.
 * <b>Configuration:</b>
 * Using the following {@code LogManager} configuration properties, where {@code <handler-name>} refers to the
 * fully-qualified class name of the handler:
 * <ul>
 * <li>   {@code &lt;handler-name&gt;.stdOut}
 *        specifies if it should use {@link System#out} or not
 *        (defaults to {@code true}). </li>
 * </ul>
 */
public class ConsoleHandlerExt extends ConsoleHandler {
	private boolean stdOut;

	/**
	 * Default constructor that uses permits setting of output.
	 */
	public ConsoleHandlerExt() {
		property(LogManager.getLogManager(), getClass(), "stdOut")
			.map(Boolean::parseBoolean)
			.ifPresentOrElse(this::setStdOut, () -> setStdOut(true));
	}

	/**
	 * @return {@code true} if using {@link System#out}.
	 */
	public boolean isStdOut() {
		return this.stdOut;
	}

	/**
	 * @param stdOut if {@code true} this uses {@link System#out}.
	 */
	public void setStdOut(boolean stdOut) {
		setOutputStream((this.stdOut = stdOut) ? System.out : System.err);
	}
}
