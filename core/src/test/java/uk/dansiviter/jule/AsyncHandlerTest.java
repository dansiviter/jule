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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.ErrorManager.WRITE_FAILURE;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static uk.dansiviter.jule.JulUtil.newInstance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test for {@link AsyncHandler}.
 */
@ExtendWith(MockitoExtension.class)
class AsyncHandlerTest {
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	private static final Logger LOG;

	static {
		LOG = Logger.getLogger(AsyncHandlerTest.class.getName());
		LOG.setUseParentHandlers(false);
	}

	private Handler handler;

	@Test
	void doPublish() {
		var handler = new TestHandler();
		LOG.addHandler(this.handler = handler);

		LOG.info("hello");
		LOG.fine("hello");

		EXECUTOR.submit(() -> LOG.info("world"));

		await().atMost(150, MILLISECONDS).untilAsserted(() -> {
			assertThat(handler.records, hasSize(2));
			assertThat(handler.records.get(0).getMessage(), equalTo("hello"));
			assertThat(handler.records.get(1).getMessage(), equalTo("world"));
		});
	}

	@Test
	void doPublish_error(@Mock ErrorManager em) {
		var handler = new FailingHandler();
		handler.setErrorManager(em);
		LOG.addHandler(this.handler = handler);

		LOG.info("hello0");
		LOG.info("hello1");

		verify(em, timeout(150).times(2)).error(any(), any(), eq(WRITE_FAILURE));
	}

	@Test
	void close() {
		var handler = new TestHandler();
		LOG.addHandler(this.handler = handler);
		assertThat(handler.isClosed(), equalTo(false));
		handler.close();

		assertThat(handler.isClosed(), equalTo(true));
		assertThrows(IllegalStateException.class, () -> LOG.info("hello"));
		assertThrows(IllegalStateException.class, () -> handler.close());
	}

	@Test
	void instance() {
		var filter = newInstance(NoopFilter.class.getName());
		assertThat(filter, notNullValue());
	}

	@AfterEach
	void after() {
		LOG.removeHandler(this.handler);
	}

	@AfterAll
	static void afterAll() throws InterruptedException {
		EXECUTOR.shutdown();
		EXECUTOR.awaitTermination(1, SECONDS);
	}

	private static class TestHandler extends AsyncHandler {
		private final List<LogRecord> records = new CopyOnWriteArrayList<>();

		@Override
		protected void doPublish(LogRecord r) {
			records.add(r);
		}
	}

	private static class FailingHandler extends AsyncHandler {
		@Override
		protected void doPublish(LogRecord r) {
			throw new RuntimeException();
		}
	}

	static class NoopFilter implements Filter {
		@Override
		public boolean isLoggable(LogRecord r) {
			return false;
		}
	}
}
