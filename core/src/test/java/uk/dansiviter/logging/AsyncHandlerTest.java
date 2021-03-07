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
package uk.dansiviter.logging;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link AsyncHandler}.
 */
public class AsyncHandlerTest {
    private final Logger log = Logger.getLogger("TEST");

    @Test
    public void doPublish() {
        TestHandler handler = new TestHandler();
        log.addHandler(handler);

        log.info("hello");

        new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // nothing to see here
            }
            log.info("world");
        }).start();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
            () -> {
                assertThat(handler.records, Matchers.hasSize(2));
                assertThat(handler.records.get(0).getMessage(), Matchers.equalTo("hello"));
                assertThat(handler.records.get(1).getMessage(), Matchers.equalTo("world"));
            });
    }

    @Test
    public void close() {
        TestHandler handler = new TestHandler();
        log.addHandler(handler);

        handler.close();

        assertThrows(IllegalStateException.class, () -> log.info("hello"));
    }

    @AfterEach
    public void after() {
        // cleanup as potentially artifacts that live after each test run
        for (Handler h : this.log.getHandlers()) {
            this.log.removeHandler(h);
        }
    }

    private static class TestHandler extends AsyncHandler {
        private final List<LogRecord> records = new ArrayList<>();

        public TestHandler() {
            super(Optional.empty());
        }

        @Override
        protected void doPublish(LogRecord record) {
            records.add(record);
        }
    }
}
