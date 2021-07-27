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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link FallbackHandler}.
 */
@ExtendWith(MockitoExtension.class)
class FallbackHandlerTest {
	private LogManager manager;

	@BeforeEach
	void before() {
		manager = LogManager.getLogManager();
	}

	@Test
	void defaultState() {
		var handler = new FallbackHandler();

		assertThat(handler.getDelegate().isEmpty(), is(true));
		assertThat(handler.getFallback(), isA(AsyncConsoleHandler.class));
	}

	@Test
	void configuredState() throws IOException {
		var config = format(
			"%s.delegate=%s\n%s.fallback=%s\n",
			FallbackHandler.class.getName(), TestDelegate.class.getName(),
			FallbackHandler.class.getName(), TestFallback.class.getName());
		try (InputStream is = new ByteArrayInputStream(config.getBytes())) {
			this.manager.readConfiguration(is);
		}

		var handler = new FallbackHandler();

		assertThat(handler.getDelegate().get(), isA(TestDelegate.class));
		assertThat(handler.getFallback(), isA(TestFallback.class));
	}

	@Test
	void configuredState_badDelegate() throws IOException {
		var config = format(
			"$s.delegate=foo\n",
			FallbackHandler.class.getName());
		try (InputStream is = new ByteArrayInputStream(config.getBytes())) {
			this.manager.readConfiguration(is);
		}

		var handler = new FallbackHandler();

		assertThat(handler.getDelegate().isPresent(), is(false));
		assertThat(handler.getFallback(), isA(AsyncConsoleHandler.class));
	}

	@Test
	void publish(@Mock Handler delegate, @Mock Handler fallback) {
		var handler = new FallbackHandler();
		handler.setDelegate(Optional.of(delegate));
		handler.setFallback(fallback);

		var record = new LogRecord(Level.INFO, "Hello!");
		handler.publish(record);
		verify(delegate).publish(record);
		verifyNoInteractions(fallback);
	}

	@Test
	void publish_noDelegate(@Mock Handler fallback) {
		var handler = new FallbackHandler();
		handler.setFallback(fallback);

		var record = new LogRecord(Level.INFO, "Hello!");
		handler.publish(record);

		verify(fallback).publish(record);
	}

	@Test
	void publish_error(@Mock Handler delegate, @Mock Handler fallback, @Mock ErrorManager em) {
		doThrow(new RuntimeException()).when(delegate).publish(any());
		var handler = new FallbackHandler();
		handler.setErrorManager(em);
		handler.setDelegate(Optional.of(delegate));
		handler.setFallback(fallback);

		var record = new LogRecord(Level.INFO, "Hello!");
		handler.publish(record);
		verify(delegate).publish(record);
		verify(fallback).publish(record);
		verify(em).error(any(), ArgumentMatchers.isA(RuntimeException.class), eq(ErrorManager.WRITE_FAILURE));
	}

	@Test
	void flush(@Mock Handler delegate, @Mock Handler fallback) {
		var handler = new FallbackHandler();
		handler.setDelegate(Optional.of(delegate));
		handler.setFallback(fallback);

		handler.flush();
		verify(delegate).flush();
		verify(fallback).flush();
	}

	@Test
	void flush_noDelegate(@Mock Handler fallback) {
		var handler = new FallbackHandler();
		handler.setFallback(fallback);

		handler.flush();

		verify(fallback).flush();
	}

	@Test
	void close(@Mock Handler delegate, @Mock Handler fallback) {
		var handler = new FallbackHandler();
		handler.setDelegate(Optional.of(delegate));
		handler.setFallback(fallback);

		handler.close();
		verify(delegate).close();
		verify(fallback).close();
	}

	@Test
	void close_noDelegate(@Mock Handler fallback) {
		var handler = new FallbackHandler();
		handler.setFallback(fallback);

		handler.close();

		verify(fallback).close();
	}

	@AfterAll
	public static void afterAll() {
		LogManager.getLogManager().reset();
	}

	public static class TestDelegate extends AbstractHandler {
		@Override
		public void publish(LogRecord record) {
			// nothing to see here
		}
	}

	public static class TestFallback extends AbstractHandler {
		@Override
		public void publish(LogRecord record) {
			// nothing to see here
		}
	}
}
