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
package uk.dansiviter.jule.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import java.time.Instant;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LoggerProcessor}
 */
class LoggerProcessorTest {
	@Test
	void process() {
		var instant = Instant.parse("2023-02-01T01:02:03.000004Z");
		Compilation compilation = javac()
			.withProcessors(new LoggerProcessor(() -> instant))
			.compile(JavaFileObjects.forResource("uk/dansiviter/jule/processor/Good.java"));
		assertThat(compilation).succeeded();
		assertThat(compilation).hadNoteContaining("Generating class for: uk.dansiviter.jule.processor.Good");
		assertThat(compilation)
			.generatedSourceFile("uk/dansiviter/jule/processor/GoodImpl")
			.hasSourceEquivalentTo(JavaFileObjects.forResource("uk/dansiviter/jule/processor/GoodImpl.java"));
	}

	@Test
	void process_cdi() {
		var instant = Instant.parse("2023-02-01T01:02:03.000004Z");
		Compilation compilation = javac()
			.withProcessors(new LoggerProcessor(() -> instant))
			.compile(JavaFileObjects.forResource("uk/dansiviter/jule/processor/GoodCdi.java"));
		assertThat(compilation).succeeded();
		assertThat(compilation).hadNoteContaining("Generating class for: uk.dansiviter.jule.processor.GoodCdi");
		assertThat(compilation)
			.generatedSourceFile("uk/dansiviter/jule/processor/GoodCdiImpl")
			.hasSourceEquivalentTo(JavaFileObjects.forResource("uk/dansiviter/jule/processor/GoodCdiImpl.java"));
	}

	@Test
	void process_bad() {
		Compilation compilation =
     javac()
         .withProcessors(new LoggerProcessor())
						.compile(JavaFileObjects.forResource("uk/dansiviter/jule/processor/Bad.java"));
		assertThat(compilation).failed();
		assertThat(compilation).hadErrorContaining("Message cannot be empty!");
	}
}
