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

import static java.lang.System.getLogger;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import uk.dansiviter.jule.annotations.Message;

/**
 *
 */
public interface BaseSystemLog extends BaseLog<Logger> {
	@Override
	default Logger delegate(String name) {
		if (log().resourceBundleName().isEmpty()) {
			return getLogger(name);
		}
		var bundle = ResourceBundle.getBundle(log().resourceBundleName());
		return getLogger(name, bundle);
	}

	@Override
	default boolean isLoggable(Message.Level level) {
		return delegate().isLoggable(level(level));
	}

	@Override
	default void log(Message.Level level, Supplier<String> msg, Throwable thrown) {
		if (thrown != null) {
			delegate().log(level(level), msg, thrown);
		} else {
			delegate().log(level(level), msg);
		}
	}

	private static Level level(Message.Level level) {
		switch (level) {
			case ERROR:
			return Level.ERROR;
			case WARN:
			return Level.WARNING;
			case INFO:
			return Level.INFO;
			case DEBUG:
			return Level.DEBUG;
			case TRACE:
			return Level.TRACE;
		}
		return Level.OFF;
	}
}
