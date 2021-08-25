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
package uk.dansiviter.juli.processor;


import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import com.squareup.javapoet.TypeName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.dansiviter.juli.annotations.Message;
import uk.dansiviter.juli.annotations.Message.Level;

/**
 * Tests for {@link LogProcessor}
 */
@ExtendWith(MockitoExtension.class)
class LogProcessorTest {
	@Mock
	private ProcessingEnvironment processingEnv;

	@InjectMocks
	private LogProcessor processor;

	@Test
	void process_none(
		@Mock TypeElement annotation,
		@Mock RoundEnvironment roundEnv)
	{
		when(roundEnv.getElementsAnnotatedWith(annotation)).thenReturn(emptySet());

		this.processor.process(singleton(annotation), roundEnv);
	}

	@Test // horribly flaky test!
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void process(
		@Mock TypeElement annotation,
		@Mock RoundEnvironment roundEnv,
		@Mock TypeElement typeElement,
		@Mock Elements elements,
		@Mock PackageElement pkgElement,
		@Mock Message message,
		@Mock Messager messager,
		@Mock TypeMirror typeMirror,
		@Mock ExecutableElement exeElement,
		@Mock TypeMirror methodTypeMirror,
		@Mock Filer filer,
		@Mock JavaFileObject javaFileObject,
		@Mock TypeMirror pkgTypeMirror)
	throws IOException
	{
		when(roundEnv.getElementsAnnotatedWith(annotation)).thenReturn((Set) singleton(typeElement));
		when(processingEnv.getElementUtils()).thenReturn(elements);
		when(elements.getPackageOf(typeElement)).thenReturn(pkgElement);
		when(typeElement.getSimpleName()).thenReturn(new NameImpl("Foo"));
		when(processingEnv.getMessager()).thenReturn(messager);
		when(typeElement.asType()).thenReturn(typeMirror);
		when(typeMirror.accept(any(), any())).thenReturn(TypeName.get(LogProcessorTest.class));

		when(typeElement.getEnclosedElements()).thenReturn((List) singletonList(exeElement));
		when(exeElement.getKind()).thenReturn(ElementKind.METHOD);
		when(exeElement.getAnnotation(Message.class)).thenReturn(message);
		when(exeElement.getSimpleName()).thenReturn(new NameImpl("foo"));
		when(methodTypeMirror.accept(any(), any())).thenReturn(TypeName.VOID);
		when(exeElement.getReturnType()).thenReturn(methodTypeMirror);
		when(message.level()).thenReturn(Level.INFO);
		when(message.value()).thenReturn("hello");

		when(pkgElement.getQualifiedName()).thenReturn(new NameImpl("com.acme"));

		when(processingEnv.getFiler()).thenReturn(filer);
		when(filer.createSourceFile(any(), any())).thenReturn(javaFileObject);
		var writer = new StringWriter();
		when(javaFileObject.openWriter()).thenReturn(writer);

		this.processor.process(singleton(annotation), roundEnv);

		// although I don't like this, we are in the business of generating code
		var expected = readFromInputStream(getClass().getResourceAsStream("LogProcessorTest.txt"));

		assertThat(writer.toString(), equalTo(expected));
	}

	private static String readFromInputStream(InputStream inputStream)
  throws IOException {
    var resultStringBuilder = new StringBuilder();
    try (var buf = new BufferedReader(new InputStreamReader(inputStream))) {
        String line;
        while ((line = buf.readLine()) != null) {
            resultStringBuilder.append(line).append("\n");
        }
    }
  	return resultStringBuilder.toString();
	}

	private static class NameImpl implements Name {
		private final String delegate;

		NameImpl(String delegate) {
			this.delegate = delegate;
		}

		@Override
		public int length() {
			return this.delegate.length();
		}

		@Override
		public char charAt(int index) {
			return this.delegate.charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return this.delegate.subSequence(start, end);
		}

		@Override
		public boolean contentEquals(CharSequence cs) {
			return this.delegate.contentEquals(cs);
		}

		@Override
		public String toString() {
			return this.delegate;
		}
	}
}
