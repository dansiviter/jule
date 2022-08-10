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

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import static java.util.logging.Logger.getLogger;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message;

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
	 * Gets delegate logger instance.
	 *
	 * @param name the name of the log.
	 * @return the logger instance.
	 */
	default Logger delegate(String name) {
		var bundleName = log().resourceBundleName();
		return getLogger(name, bundleName.isBlank() ? null : bundleName);
	}

	/**
	 * Checks if this logger will log.
	 *
	 * @param level the log level.
	 * @return {@code true} if this will log.
	 */
	default boolean isLoggable(Message.Level level) {
		return delegate().isLoggable(level.julLevel);
	}

	/**
	 * Log a message.
	 *
	 * @param level the log level.
	 * @param msg the message.
	 * @param params the message parameters. If these are {@link Optional} or {@link Supplier} then they will be
	 * expanded.
	 */
	default void logp(Message.Level level, String msg, Object... params) {
		// isLoggable check will already be done
		expand(params);

		var delegate = delegate();
		var r = new LogRecord(level.julLevel, msg);
		r.setLoggerName(delegate.getName());
		if (params.length > 0) {
			if (params[params.length - 1] instanceof Throwable) {
				r.setThrown((Throwable) params[params.length - 1]);
				params = Arrays.copyOfRange(params, 0, params.length - 1);
			}
			if (params.length > 0) {
				r.setParameters(params);
			}
		}
		var frame = frame(3).orElseThrow();
		r.setSourceClassName(frame.getClassName());
		r.setSourceMethodName(frame.getMethodName());

		var resourceBundleName = delegate.getResourceBundleName();
		if (resourceBundleName != null) {
			r.setResourceBundleName(resourceBundleName);
			// only look up if needed as potentially expensive operation
			r.setResourceBundle(delegate.getResourceBundle());
		}
		delegate.log(r);
	}


	// --- Static Methods ---

	/**
	 * @param params the parameters to expand to get lazy values.
	 */
	private static void expand(Object[] params) {
		if (params.length == 0) {
			return;
		}
		for (var i = 0; i < params.length; i++) {
			if (params[i] instanceof Supplier) {
				params[i] = ((Supplier<?>) params[i]).get();
			}
			if (params[i] instanceof BooleanSupplier) {
				params[i] = ((BooleanSupplier) params[i]).getAsBoolean();
			}
			if (params[i] instanceof IntSupplier) {
				params[i] = ((IntSupplier) params[i]).getAsInt();
			}
			if (params[i] instanceof LongSupplier) {
				params[i] = ((LongSupplier) params[i]).getAsLong();
			}
			if (params[i] instanceof DoubleSupplier) {
				params[i] = ((DoubleSupplier) params[i]).getAsDouble();
			}
			if (params[i] instanceof Optional) {
				params[i] = ((Optional<?>) params[i]).orElse(null);
			}
			if (params[i] instanceof OptionalInt) {
				var optional = (OptionalInt) params[i];
				params[i] = optional.isPresent() ? optional.getAsInt() : null;
			}
			if (params[i] instanceof OptionalLong) {
				var optional = (OptionalLong) params[i];
				params[i] = optional.isPresent() ? optional.getAsLong() : null;
			}
			if (params[i] instanceof OptionalDouble) {
				var optional = (OptionalDouble) params[i];
				params[i] = optional.isPresent() ? optional.getAsDouble() : null;
			}
		}
	}

	private static Optional<StackWalker.StackFrame> frame(int skip) {
		var walker = StackWalker.getInstance(RETAIN_CLASS_REFERENCE);
		return walker.walk(s -> s.skip(skip).findFirst());
	}
}
