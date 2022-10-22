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

import static java.util.logging.Logger.getLogger;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.dansiviter.jule.annotations.Message;

/**
 *
 */
public interface BaseJulLog extends BaseLog<Logger> {
	@Override
	default Logger delegate(String name) {
		var bundleName = log().resourceBundleName();
		return getLogger(name, bundleName.isBlank() ? null : bundleName);
	}

	@Override
	default boolean isLoggable(Message.Level level) {
		return delegate().isLoggable(level(level));
	}

	@Override
	default void log(Message.Level level, Supplier<String> msg, Throwable thrown) {
		if (thrown != null) {
			delegate().log(level(level), thrown, msg);
		} else {
			delegate().log(level(level), msg);
		}
	}

	private static Level level(Message.Level level) {
		switch (level) {
			case ERROR:
			return Level.SEVERE;
			case WARN:
			return Level.WARNING;
			case INFO:
			return Level.INFO;
			case DEBUG:
			return Level.FINE;
			case TRACE:
			return Level.FINER;
		}
		throw new IllegalArgumentException("Unknown type! [" + level + "]");
	}
}
