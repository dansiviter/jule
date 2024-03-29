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

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.WeakHashMap;

import uk.dansiviter.jule.annotations.Logger;

/**
 * This class provides instances of the log wrappers.
 */
public enum LoggerFactory { ;
	private static final Map<String, ? super Object> LOGS = new WeakHashMap<>();
	public static final String SUFFIX = "Impl";

	/**
	 * Return an instance of the given type. This will attempt to walk the stack
	 * and find the calling class.
	 *
	 * @param <L> the log type.
	 * @param log the log class type.
	 * @return log instance. This may come from a cache of instances.
	 */
	public static <L> L log(Class<L> log) {
		return log(log, StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass().getName());
	}

	/**
	 * Return an instance of the given type and name of class.
	 *
	 * @param <L>  the log type.
	 * @param log  the log class type.
	 * @param name the log name.
	 * @return log instance. This may come from a cache of instances.
	 */
	public static <L> L log(Class<L> log, Class<?> name) {
		return log(log, name.getName());
	}

	/**
	 * Return an instance of the given type and name of class.
	 *
	 * @param <L>  the log type.
	 * @param logClass  the log class type.
	 * @param name the log name.
	 * @return log instance. This may come from a cache of instances.
	 */
	public static <L> L log(Class<L> logClass, String name) {
		if (!logClass.isAnnotationPresent(Logger.class)) {
			throw new IllegalArgumentException(format("@Logger annotation not present! [%s]", logClass.getName()));
		}
		var key = key(logClass, name);
		return logClass.cast(LOGS.computeIfAbsent(key, k -> create(logClass, name)));
	}

	private static Object create(Class<?> logClass, String name) {
		var className = logClass.getName().concat(SUFFIX);
		try {
			return Class.forName(className, true, logClass.getClassLoader())
				.getDeclaredConstructor(String.class)
				.newInstance(name);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(format("Unable to instantiate class! [%s]", className), e);
		}
	}

	/**
	 * Creates an interned key value for the logger instance. This is to aid usage in caching a value when using
	 * {@link WeakHashMap}.
	 *
	 * @param log the log class.
	 * @param name the log name.
	 * @return the key.
	 */
	private static String key(Class<?> log, String name) {
		return format("%s-%s", log.getName(), requireNonNull(name)).intern();
	}
}
