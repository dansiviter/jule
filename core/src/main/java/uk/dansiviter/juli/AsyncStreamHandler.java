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

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.logging.ErrorManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.annotation.Nonnull;

/**
 * Async implementation of {@link StreamHandler} which simply delegates.
 */
public abstract class AsyncStreamHandler extends AsyncHandler<LogRecord> {
	/** Delegate {@code StreamHandler} */
	protected final StreamHandler delegate;

	/**
	 * Construct a asynchronous version of {@link StreamHandler} by delegating.
	 *
	 * @param delegate the delegate {@code StreamHandler}.
	 */
	protected AsyncStreamHandler(@Nonnull StreamHandler delegate) {
		this.delegate = Objects.requireNonNull(delegate);
		this.delegate.setLevel(getLevel());
		this.delegate.setFilter(getFilter());
		this.delegate.setFormatter(getFormatter());
		try {
			this.delegate.setEncoding(getEncoding());
		} catch (UnsupportedEncodingException e) {
			getErrorManager().error(e.getMessage(), e, ErrorManager.OPEN_FAILURE);
		}
	}

	@Override
	protected void doPublish(LogRecord record) {
		delegate.publish(record);
	}

	@Override
	public void flush() {
		this.delegate.flush();
	}

	@Override
	public void close() throws SecurityException {
		this.delegate.close();
	}
}
