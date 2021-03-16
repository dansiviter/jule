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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.dansiviter.juli.annotations.Log;
import uk.dansiviter.juli.annotations.Message;
import uk.dansiviter.juli.annotations.Message.Level;

/**
 * Unit test for {@link LogProducer}. This has to be in a different project as
 * the generated classes are here.
 */
@ExtendWith(MockitoExtension.class)
public class LogProducerTest {
	private final MyLog log = LogProducer.log(MyLog.class);

	@Test
	public void equivalence() {
		assertSame(this.log, LogProducer.log(MyLog.class, LogProducerTest.class));
		assertSame(LogProducer.log(MyLog.class, "foo"), LogProducer.log(MyLog.class, "foo"));
		assertNotSame(this.log, LogProducer.log(MyLog.class, "foo"));
	}

	@Test
	public void logInjected(@Mock Handler handler) {
		Logger.getLogger("").addHandler(handler);

		assertNotNull(log);
		log.doLog();
		log.doLog("foo");
		log.doLog(5);
		log.doLog(new RuntimeException("Expected"))
				.doLog(() -> 123L);  // chained call
		log.trace(() -> { throw new RuntimeException("Not expected"); });
		log.doOnce();
		log.doOnce();
		log.doLog(Optional.of("foo"));
		log.doLog(Optional.empty());

		var recordCaptor = ArgumentCaptor.forClass(LogRecord.class);
		verify(handler, times(8)).publish(recordCaptor.capture());

		var records = recordCaptor.getAllValues().iterator();

		var record = records.next();
		assertEquals(java.util.logging.Level.INFO, record.getLevel());
		assertEquals("uk.dansiviter.juli.LogProducerTest", record.getLoggerName());
		assertEquals("Hello world!", record.getMessage());
		assertEquals("uk.dansiviter.juli.LogProducerTest", record.getSourceClassName());
		assertEquals("logInjected", record.getSourceMethodName());
		assertNull(record.getParameters());
		assertNull(record.getThrown());

		record = records.next();
		assertEquals(java.util.logging.Level.SEVERE, record.getLevel());
		assertEquals("Hello world! {0}", record.getMessage());
		assertEquals(1, record.getParameters().length);
		assertNull(record.getThrown());

		record = records.next();
		assertEquals(java.util.logging.Level.INFO, record.getLevel());
		assertEquals("Hello world! {0}", record.getMessage());
		assertEquals(Integer.valueOf(5), record.getParameters()[0]);
		assertNull(record.getThrown());

		record = records.next();
		assertEquals(java.util.logging.Level.SEVERE, record.getLevel());
		assertEquals("Hello world!", record.getMessage());
		assertNotNull(record.getThrown());
		assertNull(record.getParameters());

		record = records.next();
		assertEquals(java.util.logging.Level.INFO, record.getLevel());
		assertEquals("Hello world! {0}", record.getMessage());
		assertEquals(1, record.getParameters().length);
		assertEquals(123L, record.getParameters()[0]);
		assertNull(record.getThrown());

		record = records.next();
		assertEquals(java.util.logging.Level.INFO, record.getLevel());
		assertEquals("Hello once!", record.getMessage());

		record = records.next();
		assertEquals(java.util.logging.Level.INFO, record.getLevel());
		assertEquals("Hello world! {0}", record.getMessage());
		assertEquals(1, record.getParameters().length);
		assertEquals("foo", record.getParameters()[0]);
		assertNull(record.getThrown());

		record = records.next();
		assertEquals(java.util.logging.Level.INFO, record.getLevel());
		assertEquals("Hello world! {0}", record.getMessage());
		assertEquals(1, record.getParameters().length);
		assertEquals(null, record.getParameters()[0]);
		assertNull(record.getThrown());

		verifyNoMoreInteractions(handler);
	}

	@Log
	public interface MyLog {
		@Message("Hello world!")
		public void doLog();

		@Message(value = "Hello world! {0}", level = Level.ERROR)
		public void doLog(String foo);

		@Message("Hello world! {0}")
		public void doLog(Supplier<Long> foo);

		@Message("Hello world! {0}")
		public void doLog(int foo);

		@Message(value = "Hello world!", level = Level.ERROR)
		public MyLog doLog(Throwable t);

		@Message(value = "Hello world! {0}", level = Level.ERROR)
		public void doLog(String foo, Throwable t);

		@Message("Hello world!")
		public void doLog(String... foo);

		@Message("Hello world! {0}")
		public void doLog(Optional<String> foo);

		@Message(value = "Hello world! {0}", level = Level.TRACE)
		public void trace(Supplier<Long> foo);

		@Message(value = "Hello once!", once = true)
		public MyLog doOnce();

		public default void anotherMethods(String foo) {
			throw new IllegalArgumentException();
		}
	}
}
