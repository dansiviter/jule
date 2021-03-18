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
import static org.hamcrest.Matchers.*;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StringFormatter}.
 */
class StringFormatterTest {
	@Test
	void format() {
		var formatter = new StringFormatter();

		var record = new LogRecord(Level.ALL, "Hello %s!");
		record.setParameters(new Object[] { "world" });
		var actual = formatter.format(record);

		assertThat(actual, endsWith("Hello world!" + System.lineSeparator()));
	}
}
