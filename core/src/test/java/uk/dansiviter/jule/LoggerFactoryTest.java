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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import uk.dansiviter.jule.annotations.Logger;

/**
 * Tests for {@link LoggerFactory}.
 * <p>
 * Note, it's not possible to test much of the functionality in this project. See tests in {@code ../processor} module.
 */
class LoggerFactoryTest {
	@Test
	void log() {
		var log = (MyLogImpl) LoggerFactory.log(MyLog.class);
		assertThat("uk.dansiviter.jule.LoggerFactoryTest", equalTo(log.name));
	}

	@Test
	void log_class() {
		var log = (MyLogImpl) LoggerFactory.log(MyLog.class, String.class);
		assertThat("java.lang.String", equalTo(log.name));
	}

	@Test
	void log_name() {
		var log = (MyLogImpl) LoggerFactory.log(MyLog.class, "foo");
		assertThat("foo", equalTo(log.name));
	}

	@Test
	void log_noAnnotation() {
		var e = assertThrows(IllegalArgumentException.class, () -> LoggerFactory.log(NoAnnotation.class));
		assertThat("@Logger annotation not present! [uk.dansiviter.jule.LoggerFactoryTest$NoAnnotation]", equalTo(e.getMessage()));
	}

	@Test
	void log_classNotFound() {
		var e = assertThrows(IllegalStateException.class, () -> LoggerFactory.log(NoImplemenatation.class));
		assertThat("Unable to instantiate class! [uk.dansiviter.jule.LoggerFactoryTest$NoImplemenatationImpl]", equalTo(e.getMessage()));
	}


	// --- Internal Classes ---

	@Logger
	interface MyLog { }

	/**
	 * This would normally be auto-generated.
	 */
	public static class MyLogImpl implements MyLog {
		final String name;
		MyLogImpl(String name) {
			this.name = name;
		}
	}

	@Logger
	interface NoImplemenatation { }

	interface NoAnnotation { }
}
