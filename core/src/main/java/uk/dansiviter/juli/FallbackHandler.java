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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.annotation.Nonnull;

/**
 * A handler that delegates to a handler, but if that errors it will log via the fallback handler.
 * <p>
 * <b>Configuration:</b>
 * Using the following {@code LogManager} configuration properties, where {@code <handler-name>} refers to the
 * fully-qualified class name of the handler:
 * <ul>
 * <li>   {@code &lt;handler-name&gt;.delegate}
 *        specifies the name of the delegate {@code Handler}
 *        (errors if not defined). </li>
 * <li>   {@code &lt;handler-name&gt;.fallback}
 *        specifies the name of a {@code Handler} class to use
 *        (defaults to {@link AsyncConsoleHandler}). </li>
 * </ul>
 */
public class FallbackHandler extends AbstractHandler {
	private Optional<Handler> delegate;
	private Handler fallback;

	/**
	 * Default constructor.
	 */
	public FallbackHandler() {
		this.delegate = property("delegate").map(FallbackHandler::<Handler>instance);
		this.fallback = property("fallback").map(FallbackHandler::<Handler>instance).orElseGet(AsyncConsoleHandler::new);
	}

	public Optional<Handler> getDelegate() {
		return delegate;
	}

	public void setDelegate(Optional<Handler> delegate) {
		this.delegate = requireNonNull(delegate);
	}

	public Handler getFallback() {
		return fallback;
	}

	public void setFallback(@Nonnull Handler fallback) {
		this.fallback = requireNonNull(fallback);
	}

	@Override
	public void publish(LogRecord record) {
		try {
			this.delegate.ifPresentOrElse(
				h -> h.publish(record),
				() -> this.fallback.publish(record));
		} catch (RuntimeException e) {
			getErrorManager().error(e.getMessage(), e, ErrorManager.WRITE_FAILURE);
			this.fallback.publish(record);
		}
	}

	@Override
	public void flush() {
		this.delegate.ifPresent(Handler::flush);
		this.fallback.flush();
	}

	@Override
	public void close() throws SecurityException {
		this.delegate.ifPresent(Handler::close);
		this.fallback.close();
	}
}
