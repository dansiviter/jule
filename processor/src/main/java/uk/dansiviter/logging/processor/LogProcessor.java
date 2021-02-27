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
package uk.dansiviter.logging.processor;

import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.NOTE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import uk.dansiviter.logging.BaseLog;
import uk.dansiviter.logging.LogProducer;
import uk.dansiviter.logging.annotations.Log;
import uk.dansiviter.logging.annotations.Message;
import uk.dansiviter.logging.annotations.Message.Level;

/**
 *
 */
@SupportedAnnotationTypes("uk.dansiviter.logging.annotations.Log")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class LogProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		annotations.forEach(annotation -> roundEnv.getElementsAnnotatedWith(annotation)
				.forEach(element -> process(annotation, element)));
		return true;
	}

	private void process(@Nonnull TypeElement annotation, @Nonnull Element element) {
		var type = (TypeElement) element;
		var pkg = pkg(type);
		var className = className(type);
		var concreteName = className + "$log";

		processingEnv.getMessager().printMessage(NOTE, "Generating class for: " + className, element);

		MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(String.class, "name")
				.addStatement("this.log = $T.class.getAnnotation($T.class)", element.asType(), Log.class)
				.addStatement("this.key = $T.key($T.class, name)", LogProducer.class, element.asType())
				.addStatement("this.delegate = getLogger(name)")
				.build();
		MethodSpec delegateMethod = MethodSpec.methodBuilder("delegate")
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC, FINAL)
				.returns(Logger.class)
				.addStatement("return this.delegate")
				.addJavadoc("@returns the delegate logger.")
				.build();
		MethodSpec logMethod = MethodSpec.methodBuilder("log")
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC, FINAL)
				.returns(Log.class)
				.addStatement("return this.log")
				.addJavadoc("@returns the annotation instance.")
				.build();

		TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(concreteName)
				.addModifiers(PUBLIC, FINAL)
				.addJavadoc("Auto-generated by <a href=\"https://gitlab.com/dansiviter/logging\">uk.dansiviter.logging</a>.")
				.addAnnotation(ThreadSafe.class)
				.addSuperinterface(BaseLog.class)
				.addSuperinterface(element.asType())
				.addMethod(constructor)
				.addField(Log.class, "log", PRIVATE, FINAL)
				.addMethod(logMethod)
				.addField(Logger.class, "delegate", PRIVATE, FINAL)
				.addMethod(delegateMethod)
				.addField(String.class, "key", PUBLIC, FINAL);  // purposefully public

		type.getEnclosedElements().stream()
				.filter(e -> e.getKind() == ElementKind.METHOD)
				.filter(e -> e.getAnnotation(Message.class) != null)
				.forEach(e -> processMethod(typeBuilder, (ExecutableElement) e));

		JavaFile javaFile = JavaFile.builder(pkg.getQualifiedName().toString(), typeBuilder.build()).build();

		try {
			javaFile.writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
		}
	}

	private void processMethod(@Nonnull TypeSpec.Builder builder, @Nonnull ExecutableElement e) {
		Message message = e.getAnnotation(Message.class);

		MethodSpec.Builder method = MethodSpec.methodBuilder(e.getSimpleName().toString())
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC)
				.returns(ClassName.get(e.getReturnType()));

		e.getParameters().forEach(p -> method.addParameter(ClassName.get(p.asType()), p.getSimpleName().toString()));

		var returnThis = builder.superinterfaces.contains(ClassName.get(e.getReturnType()));

		if (message.once()) {
			var onceField = "ONCE__".concat(e.getSimpleName().toString());
			FieldSpec onceSpec = FieldSpec.builder(AtomicBoolean.class, onceField, PRIVATE, STATIC, FINAL)
					.initializer("new $T()", AtomicBoolean.class)
				    .build();
			builder.addField(onceSpec);
			method.beginControlFlow("if ($N.getAndSet(true))", onceField)
					.addStatement(returnThis ? "return this" : "return")
					.endControlFlow();
		}

		StringBuilder statement = new StringBuilder("logp($T.$N, \"$N\"");
		for (VariableElement ve : e.getParameters()) {
			statement.append(", ").append(ve.getSimpleName());
		}
		statement.append(')');

		method.addStatement(statement.toString(), Level.class, message.level().name(), message.value());

		if (returnThis) {
			method.addStatement("return this");
		}

		builder.addMethod(method.build());
	}

	private static PackageElement pkg(@Nonnull TypeElement typeElement) {
		Element e = typeElement;
		while (!(e instanceof PackageElement)) {
			e = e.getEnclosingElement();
			if (e == null) {
				return null;
			}
		}
		return (PackageElement) e;
	}

	private static String className(@Nonnull TypeElement typeElement) {
		var types = new ArrayList<CharSequence>();

		Element e = typeElement;
		while (e instanceof TypeElement) {
			types.add(0, ((TypeElement) e).getSimpleName());
			e = e.getEnclosingElement();
		}

		return String.join("$", types);
	}

	public static String modifiers(@Nonnull ExecutableElement e) {
		return e.getModifiers().stream().filter(m -> m != Modifier.ABSTRACT).map(m -> m.name().toLowerCase())
				.collect(Collectors.joining(" "));
	}

	public static String params(@Nonnull ExecutableElement e) {
		if (e.getParameters().isEmpty()) {
			return "";
		}
		return e.getParameters().stream().map(p -> p.asType() + " " + p.getSimpleName())
				.collect(Collectors.joining(", "));
	}
}
