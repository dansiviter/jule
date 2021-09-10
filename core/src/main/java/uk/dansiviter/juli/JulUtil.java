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

import java.util.Optional;
import java.util.logging.LogManager;

/**
 * {@code java.util.logging} utilities.
 */
public enum JulUtil { ;
	/**
	 * Extracts the {@link LogManager#getProperty(String)}.
	 *
	 * @param manager the log manager.
	 * @param cls the handler class.
	 * @param name the name of the property.
	 * @return the value as an {@link Optional}.
	 */
	public static Optional<String> property(LogManager manager, Class<?> cls, String name) {
		return Optional.ofNullable(manager.getProperty(format("%s.%s", cls.getName(), name)));
	}
}
