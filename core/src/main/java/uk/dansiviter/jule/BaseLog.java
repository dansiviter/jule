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

import static java.lang.String.format;

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

import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message;

/**
 * Defines the base implementation of the logger interface.
 */
public interface BaseLog<L> {
	/**
	 * @return the delegate logger.
	 */
	L delegate();

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
	L delegate(String name);

	/**
	 * Checks if this logger will log.
	 *
	 * @param level the log level.
	 * @return {@code true} if this will log.
	 */
	boolean isLoggable(Message.Level level);

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
		BaseLog.expand(params);

		Throwable thrown = null;
		if (params.length > 0) {
			if (params[params.length - 1] instanceof Throwable) {
				thrown = (Throwable) params[params.length - 1];
				params = Arrays.copyOfRange(params, 0, params.length - 1);
			}
		}

		var finalParams = params;
		log(level, () -> format(msg, finalParams), thrown);
	}

	void log(Message.Level level, Supplier<String> msg, Throwable thrown);


	// --- Static Methods ---

	/**
	 * @param params the parameters to expand to get lazy values.
	 */
	public static void expand(Object[] params) {
		if (params.length == 0) {
			return;
		}
		for (var i = 0; i < params.length; i++) {
			params[i] = expand(params[i]);
		}
	}

	/**
	 * @param param the parameter to expand to get lazy values.
	 * @return the expanded value or the input value if unable to expand.
	 */
	private static Object expand(Object param) {
		if (param instanceof Supplier) {
			return ((Supplier<?>) param).get();
		} else if (param instanceof BooleanSupplier) {
			return ((BooleanSupplier) param).getAsBoolean();
		} else if (param instanceof IntSupplier) {
			return ((IntSupplier) param).getAsInt();
		} else if (param instanceof LongSupplier) {
			return ((LongSupplier) param).getAsLong();
		} else if (param instanceof DoubleSupplier) {
			return ((DoubleSupplier) param).getAsDouble();
		} else if (param instanceof Optional) {
			return ((Optional<?>) param).orElse(null);
		} else if (param instanceof OptionalInt) {
			var optional = (OptionalInt) param;
			return optional.isPresent() ? optional.getAsInt() : null;
		} else if (param instanceof OptionalLong) {
			var optional = (OptionalLong) param;
			return optional.isPresent() ? optional.getAsLong() : null;
		} else if (param instanceof OptionalDouble) {
			var optional = (OptionalDouble) param;
			return optional.isPresent() ? optional.getAsDouble() : null;
		}
		return param;
	}
}
