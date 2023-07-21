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
	private static final Optional<Class<?>> LEVEL;
	private static final Optional<Method> LOGP;
	private static final Optional<Method> LOGP_THROWN;

	private static final Map<Level, Object> LEVEL_MAP = new ConcurrentHashMap<>();

	static {
		Class<?> platformLogger;
		Class<?> bridge;
		Class<?> level;
		Method logp;
		Method logpThrown;
		try {
			platformLogger = Class.forName("sun.util.logging.PlatformLogger");
			bridge = Class.forName("sun.util.logging.PlatformLogger$Bridge");
			level = Class.forName("sun.util.logging.PlatformLogger$Level");
			logp = bridge.getDeclaredMethod("logp", level, String.class, String.class, Supplier.class);
			logpThrown = bridge.getDeclaredMethod("logp", level, String.class, String.class, Throwable.class, Supplier.class);
		} catch (ReflectiveOperationException e) {
			platformLogger = null;
			bridge = null;
			level = null;
			logp = null;
			logpThrown = null;
		}
		PLATFORM_LOGGER = Optional.of(platformLogger);
		BRIDGE = Optional.ofNullable(bridge);
		LEVEL = Optional.ofNullable(level);
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
