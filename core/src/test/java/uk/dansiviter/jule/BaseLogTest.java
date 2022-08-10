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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.ArrayMatching.arrayContaining;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.dansiviter.jule.BaseLog;
import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message.Level;

/**
 * Tests for {@link BaseLog}.
 */
@ExtendWith(MockitoExtension.class)
class BaseLogTest {
	@Mock
	private Logger delegate;
	@Mock
	private Log log;

	private BaseLog baseLog;

	@BeforeEach
	void before() {
		this.baseLog = new BaseLog() {
			@Override
			public Logger delegate() {
				return delegate;
			}

			@Override
			public Log log() {
				return log;
			}
		};
	}

	@Test
	void delegate() {
		when(this.log.resourceBundleName()).thenReturn("");

		Logger logger = this.baseLog.delegate("BaseLogTest#delegate");

		assertThat(logger.getName(), equalTo("BaseLogTest#delegate"));
		assertThat(logger.getResourceBundleName(), nullValue());
		verify(this.log).resourceBundleName();
	}

	@Test
	void delegate_resourceBundle() {
		when(this.log.resourceBundleName()).thenReturn(BaseLogTest.class.getName());

		Logger logger = this.baseLog.delegate("BaseLogTest#resourceBundle");

		assertThat(logger.getName(), equalTo("BaseLogTest#resourceBundle"));
		assertThat(logger.getResourceBundleName(), equalTo(BaseLogTest.class.getName()));
		assertThat(logger.getResourceBundle(), notNullValue());

		verify(this.log).resourceBundleName();
	}

	@Test
	void isLoggable() {
		this.baseLog.isLoggable(Level.DEBUG);

		verify(this.delegate).isLoggable(Level.DEBUG.julLevel);
	}

	@Test
	void logp() {
		when(this.delegate.getResourceBundleName()).thenReturn("myBundle");

		this.baseLog.logp(Level.DEBUG,
			"hello",
			"world",
			(Supplier<String>) () -> "foo",  // test expansion
			(BooleanSupplier) () -> true,
			(IntSupplier) () -> 2,
			(LongSupplier) () -> 3L,
			(DoubleSupplier) () -> 2.3,
			Optional.of("bar"),
			Optional.empty(),
			OptionalInt.of(5),
			OptionalLong.of(6),
			OptionalDouble.of(3.2));

		var recordCaptor = ArgumentCaptor.forClass(LogRecord.class);
		verify(this.delegate).log(recordCaptor.capture());

		var record = recordCaptor.getValue();
		assertThat(record.getLevel(), equalTo(Level.DEBUG.julLevel));
		assertThat(record.getMessage(), equalTo("hello"));
		assertThat(record.getParameters(), arrayContaining("world", "foo", true, 2, 3L, 2.3, "bar", null, 5, 6L, 3.2));
		assertThat(record.getThrown(), nullValue());
		assertThat(record.getResourceBundleName(), equalTo("myBundle"));
	}

	@Test
	void logp_throwable() {
		this.baseLog.logp(Level.DEBUG, "hello", new IllegalStateException());

		var recordCaptor = ArgumentCaptor.forClass(LogRecord.class);
		verify(this.delegate).log(recordCaptor.capture());

		var record = recordCaptor.getValue();
		assertThat(record.getLevel(), equalTo(Level.DEBUG.julLevel));
		assertThat(record.getMessage(), equalTo("hello"));
		assertThat(record.getThrown(), isA(IllegalStateException.class));
	}

	@Test
	void logp_noParams() {
		this.baseLog.logp(Level.DEBUG, "hello");

		var recordCaptor = ArgumentCaptor.forClass(LogRecord.class);
		verify(this.delegate).log(recordCaptor.capture());

		var record = recordCaptor.getValue();
		assertThat(record.getLevel(), equalTo(Level.DEBUG.julLevel));
		assertThat(record.getMessage(), equalTo("hello"));
		assertThat(record.getParameters(), nullValue());
		assertThat(record.getThrown(), nullValue());
		assertThat(record.getResourceBundleName(), nullValue());
	}

	@Test
	void levels() {
		assertThat(Level.TRACE.julLevel, equalTo(java.util.logging.Level.FINER));
		assertThat(Level.DEBUG.julLevel, equalTo(java.util.logging.Level.FINE));
		assertThat(Level.INFO.julLevel, equalTo(java.util.logging.Level.INFO));
		assertThat(Level.WARN.julLevel, equalTo(java.util.logging.Level.WARNING));
		assertThat(Level.ERROR.julLevel, equalTo(java.util.logging.Level.SEVERE));
	}
}
