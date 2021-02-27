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
package uk.dansiviter.logging.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 *
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Message {
	/**
	 * @return the log level.
	 */
	Level level() default Level.INFO;

	/**
	 * @return the message to log.
	 */
	String value();

	/**
	 * @return {@code true} if the message should only ever be logged once.
	 */
	boolean once() default false;

	public enum Level {
		ERROR(java.util.logging.Level.SEVERE),
		WARN(java.util.logging.Level.WARNING),
		INFO(java.util.logging.Level.INFO),
		DEBUG(java.util.logging.Level.FINE),
		TRACE(java.util.logging.Level.FINER);

		public final java.util.logging.Level julLevel;

		Level(java.util.logging.Level julLevel) {
			this.julLevel = julLevel;
		}
	}
}
