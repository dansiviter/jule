/*
 * Copyright 2022 Daniel Siviter
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
package uk.dansiviter.jule.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.dansiviter.jule.LogProducer;
import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message;
import uk.dansiviter.jule.annotations.Log.Type;
import uk.dansiviter.jule.annotations.Message.Level;

/**
 * Unit test for {@link LogProducer}. This has to be in a different project as
 * the generated classes are here.
 */
@ExtendWith(MockitoExtension.class)
class LogProducerTest {
	private static Collection<Handler> HANDLERS;

	private final MyJulLog log = LogProducer.log(MyJulLog.class);
	private final MySysLog sysLog = LogProducer.log(MySysLog.class);

	@BeforeAll
	public static void beforeAll() {
		var root = Logger.getLogger("");
		HANDLERS = Arrays.asList(root.getHandlers());
		HANDLERS.forEach(root::removeHandler);
	}

	@Test
	void equivalence() {
		assertSame(this.log, LogProducer.log(MyJulLog.class, LogProducerTest.class));
		assertSame(LogProducer.log(MyJulLog.class, "foo"), LogProducer.log(MyJulLog.class, "foo"));
		assertNotSame(this.log, LogProducer.log(MyJulLog.class, "foo"));
	}

	@Test
	void julLog(@Mock Handler handler) {
		System.getLogger("foo").isLoggable(System.Logger.Level.ERROR);
		Logger.getLogger("").addHandler(handler);

		assertNotNull(log);
		log.doLog();
		log.doLog("foo");
		log.doLog(5);
		log.doLog(new RuntimeException("Expected"));
		log.doLog(() -> 123L);
		log.trace(() -> { throw new RuntimeException("Not expected"); });
		log.doOnce();
		log.doOnce();
		log.doLog(Optional.of("foo"));
		log.doLog(Optional.empty());

		var recordCaptor = ArgumentCaptor.forClass(LogRecord.class);
		verify(handler, times(8)).publish(recordCaptor.capture());

		var records = recordCaptor.getAllValues().iterator();

		var record = records.next();
		assertThat(record.getLevel(), is(java.util.logging.Level.INFO));
		assertThat(record.getLoggerName(), is(getClass().getName()));
		assertThat(record.getMessage(), is("Hello world!"));
		assertThat(record.getParameters(), nullValue());
		assertThat(record.getThrown(), nullValue());
		assertThat(record.getSourceClassName(), is("uk.dansiviter.jule.processor.LogProducerTest"));
		assertThat(record.getSourceMethodName(), is("julLog"));

		record = records.next();
		assertThat(record.getLevel(), is(java.util.logging.Level.SEVERE));
		assertThat(record.getMessage(), is("Hello world! foo"));
		assertThat(record.getParameters(), nullValue());

		record = records.next();
		assertThat(record.getMessage(), is("Hello world! 5"));
		assertThat(record.getParameters(), nullValue());

		record = records.next();
		assertThat(record.getLevel(), is(java.util.logging.Level.SEVERE));
		assertThat(record.getMessage(), is("Hello world!"));
		assertThat(record.getThrown(), notNullValue());
		assertThat(record.getParameters(), nullValue());

		record = records.next();
		assertThat(record.getMessage(), is("Hello world! 123"));
		assertThat(record.getParameters(), nullValue());

		record = records.next();
		assertThat(record.getMessage(), is("Hello once!"));

		record = records.next();
		assertThat(record.getMessage(), is("Hello world! foo"));
		assertThat(record.getParameters(), nullValue());

		record = records.next();
		assertThat(record.getMessage(), is("Hello world! null"));
		assertThat(record.getParameters(), nullValue());

		verifyNoMoreInteractions(handler);
	}

	@Test
	void sysLog(@Mock Handler handler) {
		System.getLogger("foo").isLoggable(System.Logger.Level.ERROR);
		Logger.getLogger("").addHandler(handler);

		assertNotNull(sysLog);
		sysLog.doLog();

		var recordCaptor = ArgumentCaptor.forClass(LogRecord.class);
		verify(handler).publish(recordCaptor.capture());

		var records = recordCaptor.getAllValues().iterator();
		var record = records.next();
		assertThat(record.getLevel(), is(java.util.logging.Level.INFO));
		assertThat(record.getLoggerName(), is(getClass().getName()));
		assertThat(record.getMessage(), is("Hello world!"));
	}

	interface MySuperLog {
		@Message(value = "Hello world!", level = Level.ERROR)
		void doSuperLog();
	}

	@Log
	interface MyJulLog extends MySuperLog {
		@Message("Hello world!")
		void doLog();

		@Message(value = "Hello world! %s", level = Level.ERROR)
		void doLog(String foo);

		@Message("Hello world! %s")
		void doLog(LongSupplier foo);

		@Message("Hello world! %s")
		void doLog(int foo);

		@Message(value = "Hello world!", level = Level.ERROR)
		void doLog(Throwable t);

		@Message(value = "Hello world! %s", level = Level.ERROR)
		void doLog(String foo, Throwable t);

		@Message("Hello world!")
		void doLog(String... foo);

		@Message("Hello world! %s")
		void doLog(Optional<String> foo);

		@Message(value = "Hello world! %s", level = Level.TRACE)
		void trace(Supplier<Long> foo);

		@Message(value = "Hello once!", once = true)
		void doOnce();

		default void anotherMethods(String foo) {
			throw new IllegalArgumentException();
		}
	}

	@Log(type = Type.SYSTEM)
	interface MySysLog extends MySuperLog {
		@Message("Hello world!")
		void doLog();
	}

	@AfterAll
	public static void afterAll() {
		var root = Logger.getLogger("");
		HANDLERS.forEach(root::addHandler);
	}
}
