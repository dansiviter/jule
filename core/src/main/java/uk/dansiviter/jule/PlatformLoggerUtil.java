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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public enum PlatformLoggerUtil { ;
	private static final Optional<Class<?>> PLATFORM_LOGGER;
	private static final Optional<Class<?>> BRIDGE;
	private static final Optional<Method> LOGP;
	private static final Optional<Method> LOGP_THROWN;

	private static final Map<Level, Object> LEVEL_MAP = new ConcurrentHashMap<>();

	static {
		Class<?> platformLogger;
		Class<?> bridge;
		Method logp;
		Method logpThrown;
		try {
			platformLogger = Class.forName("sun.util.logging.PlatformLogger");
			bridge = Class.forName("sun.util.logging.PlatformLogger$Bridge");
			var level = Class.forName("sun.util.logging.PlatformLogger$Level");
			logp = bridge.getDeclaredMethod("logp", level, String.class, String.class, Supplier.class);
			logpThrown = bridge.getDeclaredMethod("logp", level, String.class, String.class, Throwable.class, Supplier.class);
		} catch (ReflectiveOperationException e) {
			platformLogger = null;
			bridge = null;
			logp = null;
			logpThrown = null;
		}
		PLATFORM_LOGGER = Optional.of(platformLogger);
		BRIDGE = Optional.ofNullable(bridge);
		LOGP = Optional.ofNullable(logp);
		LOGP_THROWN = Optional.ofNullable(logpThrown);
	}

	static void logp(Logger logger, Level level, String sourceClass, String sourceMethod, Supplier<String> msg) {
		if (BRIDGE.isPresent() && BRIDGE.get().isAssignableFrom(logger.getClass())) {
			LOGP.ifPresent(m -> invoke(m, logger, toLevel(level), sourceClass, sourceMethod, msg));
		} else {
			logger.log(level, msg);
		}
	}

	static void logp(Logger logger, Level level, String sourceClass, String sourceMethod, Supplier<String> msg, Throwable thrown) {
		if (BRIDGE.isPresent() && logger.getClass().isAssignableFrom(BRIDGE.get())) {
			LOGP_THROWN.ifPresent(m -> invoke(m, logger, toLevel(level), sourceClass, sourceMethod, thrown, msg));
		} else {
			logger.log(level, msg, thrown);
		}
	}

	private static void invoke(Method method, Logger logger, Object... params) {
		try {
			method.invoke(logger, params);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Object toLevel(Level level) {
		return LEVEL_MAP.computeIfAbsent(level, l -> PLATFORM_LOGGER.map(c -> toLevel(c, level)).orElseThrow());
	}

	private static Object toLevel(Class<?> platformLoggerClass, Level level) {
		try {
			var method = platformLoggerClass.getMethod("toPlatformLevel", Level.class);
			return method.invoke(null, level);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
