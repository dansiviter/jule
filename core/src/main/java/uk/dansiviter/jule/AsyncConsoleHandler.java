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

/**
 * Async implementation which simply delegates to {@link ConsoleHandlerExt}.
 */
public class AsyncConsoleHandler extends AsyncStreamHandler<ConsoleHandlerExt> {
	/**
	 * Constructs an asynchronous {@code ConsoleHandlerExt}
	 */
	public AsyncConsoleHandler() {
		super(new ConsoleHandlerExt());
		property("stdOut")
			.map(Boolean::parseBoolean)
			.ifPresent(this::setStdOut);
	}

	/**
	 * @return {@code true} if using {@link System#out}.
	 */
	public boolean isStdOut() {
		return this.delegate.isStdOut();
	}

	/**
	 * @param stdOut if {@code true} this uses {@link System#out}.
	 */
	public void setStdOut(boolean stdOut) {
		this.delegate.setStdOut(stdOut);
	}
}
