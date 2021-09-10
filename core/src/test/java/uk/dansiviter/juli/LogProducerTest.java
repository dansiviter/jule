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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import uk.dansiviter.juli.annotations.Log;

/**
 * Tests for {@link LogProducer}.
 * <p>
 * Note, it's not possible to test much of the functionality in this project. See tests in {@code ../processor} module.
 */
class LogProducerTest {
	@Test
	void log() {
		var log = (MyLog$impl) LogProducer.log(MyLog.class);
		assertThat("uk.dansiviter.juli.LogProducerTest", equalTo(log.name));
	}

	@Test
	void log_class() {
		var log = (MyLog$impl) LogProducer.log(MyLog.class, String.class);
		assertThat("java.lang.String", equalTo(log.name));
	}

	@Test
	void log_name() {
		var log = (MyLog$impl) LogProducer.log(MyLog.class, "foo");
		assertThat("foo", equalTo(log.name));
	}

	@Test
	void log_noAnnotation() {
		var e = assertThrows(IllegalArgumentException.class, () -> LogProducer.log(NoAnnotation.class));
		assertThat("@Log annotation not present! [uk.dansiviter.juli.LogProducerTest$NoAnnotation]", equalTo(e.getMessage()));
	}

	@Test
	void log_classNotFound() {
		var e = assertThrows(IllegalStateException.class, () -> LogProducer.log(NoImplemenatation.class));
		assertThat("Unable to instantiate class! [uk.dansiviter.juli.LogProducerTest$NoImplemenatation$impl]", equalTo(e.getMessage()));
	}


	// --- Internal Classes ---

	@Log
	interface MyLog { }

	/**
	 * This would normally be auto-generated.
	 */
	public static class MyLog$impl implements MyLog {
		final String name;
		final String key;
		MyLog$impl(String name, String key) {
			this.name = name;
			this.key = key;
		}
	}

	@Log
	interface NoImplemenatation { }

	interface NoAnnotation { }
}
