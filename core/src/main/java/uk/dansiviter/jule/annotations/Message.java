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
package uk.dansiviter.jule.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Designates a method as a log message. Exceptions must be the last parameter in the method.
 * {@link java.util.function.Supplier} and {@link java.util.Optional} parameters will be automatically unwrapped prior
 * to logging.
 * <p>
 * Usage:
 * <pre>
 * &#064;Message(value = "Oh no! The value was %d!")
 * void error(int myInteger, IllegalStateException e);
 * </pre>
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

	/**
	 * Log levels
	 */
	public enum Level {
		/** Error level */
		ERROR,
		/** Warning level */
		WARN,
		/** Information Level */
		INFO,
		/** Debug level */
		DEBUG,
		/** Trace level */
		TRACE
	}
}
