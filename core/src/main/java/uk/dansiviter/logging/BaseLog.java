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

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import uk.dansiviter.logging.annotations.Log;
import uk.dansiviter.logging.annotations.Message;

/**
 * Defines the base implementation of the logger interface.
 */
public interface BaseLog {
	/**
	 * @return the delegate logger.
	 */
	Logger delegate();

	/**
	 * @return the {@link Log} instance.
	 */
	Log log();

	/**
	 *
	 * @param name
	 * @return
	 */
	default Logger getLogger(@Nonnull String name) {
		var resourceBundleName = log().resourceBundleName();
		return Logger.getLogger(name, resourceBundleName.isBlank() ? null : resourceBundleName);
	}

	/**
	 * Log a message.
	 *
	 * @param level
	 * @param msg
	 * @param params
	 */
	default void logp(@Nonnull Message.Level level, @Nonnull String msg, Object... params) {
		var delegate = delegate();
		if (!delegate.isLoggable(level.julLevel)) {
			return;
		}
		expand(params);

		LogRecord record = new LogRecord(level.julLevel, msg);
		record.setLoggerName(delegate.getName());
		if (params.length > 0) {
			if (params[params.length - 1] instanceof Throwable) {
				record.setThrown((Throwable) params[params.length - 1]);
				params = Arrays.copyOfRange(params, 0, params.length - 1);
			}
			if (params.length > 0) {
				record.setParameters(params);
			}
		}
		StackWalker.StackFrame frame = frame(3).orElseThrow();
		record.setSourceClassName(frame.getClassName());
		record.setSourceMethodName(frame.getMethodName());

		var resourceBundleName = delegate.getResourceBundleName();
		if (resourceBundleName != null) {
			record.setResourceBundleName(resourceBundleName);
			// only look up if needed as potentially expensive operation
			record.setResourceBundle(delegate.getResourceBundle());
		}
		delegate.log(record);
	}


	// --- Static Methods ---

	/**
	 * @param params the parameters to expand to get lazy values.
	 */
	private static void expand(Object[] params) {
		if (params == null) {
			return;
		}
		for (int i = 0; i < params.length; i++) {
			if (params[i] instanceof Supplier) {
				params[i] = ((Supplier<?>) params[i]).get();
			}
			if (params[i] instanceof Optional) {
				params[i] = ((Optional<?>) params[i]).orElse(null);
			}
		}
	}

	private static Optional<StackWalker.StackFrame> frame(int skip) {
		StackWalker walker = StackWalker.getInstance(RETAIN_CLASS_REFERENCE);
		return walker.walk(s -> s.skip(skip).findFirst());
	}
}
