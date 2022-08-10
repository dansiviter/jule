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

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogManager;

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
	public void close() {
		// nothing to see here
	}

  /**
	 * Extracts the {@link LogManager#getProperty(String)}.
	 *
	 * @param name the name of the property.
	 * @return the value as an {@link Optional}.
	 */
	protected Optional<String> property(String name) {
		return JulUtil.property(manager, getClass(), name);
	}
}
