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
package uk.dansiviter.jule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.System.Logger;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message.Level;

/**
 * Tests for {@link BaseSystemLog}.
 */
@ExtendWith(MockitoExtension.class)
class BaseSystemLogTest {
	@Mock
	private Logger delegate;
	@Mock
	private Log log;

	private BaseSystemLog baseLog;

	@BeforeEach
	void before() {
		this.baseLog = new BaseSystemLog() {
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
		verify(this.log).resourceBundleName();
	}

	@Test
	void delegate_resourceBundle() {
		when(this.log.resourceBundleName()).thenReturn(BaseJulLogTest.class.getName());

		Logger logger = this.baseLog.delegate("BaseLogTest#resourceBundle");

		assertThat(logger.getName(), equalTo("BaseLogTest#resourceBundle"));

		verify(this.log).resourceBundleName();
	}

	@Test
	void isLoggable() {
		this.baseLog.isLoggable(Level.DEBUG);

		verify(this.delegate).isLoggable(java.lang.System.Logger.Level.DEBUG);
	}

	@Test
	void log() {
		this.baseLog.logp(Level.DEBUG,
			"hello %s %s %s %d %d %,.2f %s %s %d %d %d %,.2f",
			"world",
			(Supplier<String>) () -> "foo",  // test expansion
			(BooleanSupplier) () -> true,
			(IntSupplier) () -> 2,
			(LongSupplier) () -> 3L,
			(DoubleSupplier) () -> 2.3,
			Optional.of("bar"),
			Optional.empty(),
			OptionalInt.of(5),
			OptionalInt.empty(),
			OptionalLong.of(6),
			OptionalDouble.of(3.2));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Supplier<String>> msgCaptor = ArgumentCaptor.forClass(Supplier.class);

		verify(this.delegate).log(
			eq(java.lang.System.Logger.Level.DEBUG),
			msgCaptor.capture());
		assertThat(msgCaptor.getValue().get(), equalTo("hello world foo true 2 3 2.30 bar null 5 null 6 3.20"));
	}

	@Test
	void log_throwable() {
		this.baseLog.logp(Level.DEBUG, "hello", new IllegalStateException());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Supplier<String>> msgCaptor = ArgumentCaptor.forClass(Supplier.class);

		verify(this.delegate).log(
			eq(java.lang.System.Logger.Level.DEBUG),
			msgCaptor.capture(),
			isA(IllegalStateException.class)
			);
		assertThat(msgCaptor.getValue().get(), equalTo("hello"));
  }
}
