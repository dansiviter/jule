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

import java.io.UnsupportedEncodingException;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Async implementation of {@link StreamHandler} which simply delegates.
 */
public abstract class AsyncStreamHandler<H extends StreamHandler> extends AsyncHandler<LogRecord> {
	/** Delegate {@code StreamHandler} */
	protected final H delegate;

	/**
	 * Construct a asynchronous version of {@link StreamHandler} by delegating.
	 *
	 * @param delegate the delegate {@code StreamHandler}.
	 */
	protected AsyncStreamHandler(H delegate) {
		this.delegate = requireNonNull(delegate);
		this.delegate.setLevel(getLevel());
		this.delegate.setFilter(getFilter());
		this.delegate.setFormatter(new NoopFormatter());
		try {
			this.delegate.setEncoding(getEncoding());
		} catch (UnsupportedEncodingException e) {
			getErrorManager().error(e.getMessage(), e, ErrorManager.OPEN_FAILURE);
		}
	}

	@Override
	protected LogRecord transform(LogRecord r) {
		r.setMessage(getFormatter().format(r));  // format retaining thread context
		return super.transform(r);
	}

	@Override
	protected void doPublish(LogRecord r) {
		this.delegate.publish(r);
	}

	@Override
	public void flush() {
		this.delegate.flush();
	}

	@Override
	public void close() throws SecurityException {
		this.delegate.close();
	}


	// --- Inner Classes ---

	private static class NoopFormatter extends Formatter {
		@Override
		public String format(LogRecord r) {
			return r.getMessage();
		}
	}
}
