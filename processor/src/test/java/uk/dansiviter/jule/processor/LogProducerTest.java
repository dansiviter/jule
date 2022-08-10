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
package uk.dansiviter.jule.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
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
import uk.dansiviter.jule.annotations.Message.Level;

/**
 * Unit test for {@link LogProducer}. This has to be in a different project as
 * the generated classes are here.
 */
@ExtendWith(MockitoExtension.class)
class LogProducerTest {
	private static Collection<Handler> HANDLERS;

	private final MyLog log = LogProducer.log(MyLog.class);

	@BeforeAll
	public static void beforeAll() {
		var root = Logger.getLogger("");
		HANDLERS = Arrays.asList(root.getHandlers());
		HANDLERS.forEach(root::removeHandler);
	}

	@Test
	void equivalence() {
		assertSame(this.log, LogProducer.log(MyLog.class, LogProducerTest.class));
		assertSame(LogProducer.log(MyLog.class, "foo"), LogProducer.log(MyLog.class, "foo"));
		assertNotSame(this.log, LogProducer.log(MyLog.class, "foo"));
	}

	@Test
	void logInjected(@Mock Handler handler) {
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
		assertThat(java.util.logging.Level.INFO, is(record.getLevel()));
		assertThat(getClass().getName(), is(record.getLoggerName()));
		assertThat("Hello world!", is(record.getMessage()));
		assertThat(getClass().getName(), is(record.getSourceClassName()));
		assertThat("logInjected", is(record.getSourceMethodName()));
		assertThat(record.getParameters(), nullValue());
		assertThat(record.getThrown(), nullValue());

		record = records.next();
		assertThat(java.util.logging.Level.SEVERE, is(record.getLevel()));
		assertThat("Hello world! {0}", is(record.getMessage()));
		assertThat(record.getParameters(), arrayContaining("foo"));

		record = records.next();
		assertThat("Hello world! {0}", is(record.getMessage()));
		assertThat(record.getParameters(), arrayContaining(Integer.valueOf(5)));

		record = records.next();
		assertThat(java.util.logging.Level.SEVERE, is(record.getLevel()));
		assertThat("Hello world!", is(record.getMessage()));
		assertThat(record.getThrown(), notNullValue());
		assertThat(record.getParameters(), nullValue());

		record = records.next();
		assertThat("Hello world! {0}", is(record.getMessage()));
		assertThat(record.getParameters(), arrayContaining(123L));

		record = records.next();
		assertThat("Hello once!", is(record.getMessage()));

		record = records.next();
		assertThat("Hello world! {0}", is(record.getMessage()));
		assertThat(record.getParameters(), arrayContaining("foo"));

		record = records.next();
		assertThat("Hello world! {0}", is(record.getMessage()));
		assertThat(record.getParameters(), arrayContaining((Object) null));

		verifyNoMoreInteractions(handler);
	}

	interface MySuperLog {
		@Message("Hello world!")
		void doSuperLog();
	}

	@Log
	interface MyLog extends MySuperLog {
		@Message("Hello world!")
		void doLog();

		@Message(value = "Hello world! {0}", level = Level.ERROR)
		void doLog(String foo);

		@Message("Hello world! {0}")
		void doLog(LongSupplier foo);

		@Message("Hello world! {0}")
		void doLog(int foo);

		@Message(value = "Hello world!", level = Level.ERROR)
		void doLog(Throwable t);

		@Message(value = "Hello world! {0}", level = Level.ERROR)
		void doLog(String foo, Throwable t);

		@Message("Hello world!")
		void doLog(String... foo);

		@Message("Hello world! {0}")
		void doLog(Optional<String> foo);

		@Message(value = "Hello world! {0}", level = Level.TRACE)
		void trace(Supplier<Long> foo);

		@Message(value = "Hello once!", once = true)
		void doOnce();

		default void anotherMethods(String foo) {
			throw new IllegalArgumentException();
		}
	}

	@AfterAll
	public static void afterAll() {
		var root = Logger.getLogger("");
		HANDLERS.forEach(root::addHandler);
	}
}
