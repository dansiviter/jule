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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.dansiviter.jule.AsyncStreamHandler;

/**
 * Unit test for {@link AsyncStreamHandler}.
 */
@ExtendWith(MockitoExtension.class)
class AsyncStreamHandlerTest {
	@Test
	void doPublish(@Mock StreamHandler delegate) {
		var handler = new AsyncStreamHandler<StreamHandler>(delegate) { };

		var record = new LogRecord(Level.INFO, "hello");
		handler.doPublish(record);
		handler.flush();

		verify(delegate, timeout(500)).publish(record);
	}

	@Test
	void transform(@Mock StreamHandler delegate) {
		var handler = new AsyncStreamHandler<>(delegate) { };

		var record = new LogRecord(Level.INFO, "Hello {0}");
		record.setParameters(new Object[] { "world" });
		record = handler.transform(record);

		assertThat(record.getMessage(), endsWith("Hello world" + System.lineSeparator()));
	}

	@Test
	void flush(@Mock StreamHandler delegate) {
		var handler = new AsyncStreamHandler<>(delegate) { };

		handler.flush();

		verify(delegate).flush();
	}

	@Test
	void close(@Mock StreamHandler delegate) {
		var handler = new AsyncStreamHandler<>(delegate) { };

		handler.close();

		verify(delegate).close();
	}
}
