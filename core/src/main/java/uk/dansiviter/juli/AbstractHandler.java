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
package uk.dansiviter.juli;

import static java.lang.String.format;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import javax.annotation.Nonnull;

/**
 * Base {@link Handler} implementation.
 */
public abstract class AbstractHandler extends Handler {
	protected final LogManager manager;

  protected AbstractHandler() {
		this.manager = Objects.requireNonNull(LogManager.getLogManager());
	}

	@Override
	public void flush() {
		// nothing to see here
	}

	@Override
	public void close() throws SecurityException {
		// nothing to see here
	}

 /**
	 * Extracts the {@link LogManager#getProperty(String)}.
	 *
	 * @param manager the manager instance.
	 * @param name the name of the property.
	 * @return the value as an {@link Optional}.
	 */
	protected Optional<String> property(@Nonnull String name) {
		return Optional.ofNullable(manager.getProperty(getClass().getName() + "." + name));
	}


	// --- Static Methods ---

	/**
	 * Creates an instance of the class given by it's name using no-args constructor.
	 *
	 * @param <T> the type.
	 * @param name the class name
	 * @return an instance of the class.
	 * @throws IllegalArgumentException if the class cannot be created.
	 */
	@SuppressWarnings("unchecked")
	protected static @Nonnull <T> T instance(@Nonnull String name) {
		try {
			Class<?> concreteCls = Class.forName(name);
			return (T) concreteCls.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(format("Unable to create! [%s]", name), e);
		}
	}
}
