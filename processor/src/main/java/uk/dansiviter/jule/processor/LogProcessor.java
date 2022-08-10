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

import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
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
import javax.lang.model.type.TypeMirror;

import jakarta.json.Json;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import uk.dansiviter.jule.BaseLog;
import uk.dansiviter.jule.LogProducer;
import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message;
import uk.dansiviter.jule.annotations.Message.Level;

/**
 * Processes {@link Log} annotations.
 */
@SupportedAnnotationTypes("uk.dansiviter.jule.annotations.Log")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class LogProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		annotations.forEach(a -> roundEnv.getElementsAnnotatedWith(a).forEach(e -> process((TypeElement) e)));
		return true;
	}

	private void process(TypeElement element) {
		var pkg = this.processingEnv.getElementUtils().getPackageOf(element);
		var type = element.asType();
		var className = className(element);
		var concreteName = className.concat(LogProducer.SUFFIX);
		createConcrete(className, element, type, concreteName, pkg);
	}

	private void createConcrete(
		String className,
		TypeElement type,
		TypeMirror typeMirror,
		String concreteName,
		PackageElement pkg)
	{
		processingEnv.getMessager().printMessage(
			NOTE,
			format("Generating class for: %s.%s", pkg.getQualifiedName(), className),
			type);

		var constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(String.class, "name")
				.addParameter(String.class, "key")
				.addStatement("this.log = $T.class.getAnnotation($T.class)", typeMirror, Log.class)
				.addStatement("this.key = key")
				.addStatement("this.delegate = delegate(name)")
				.build();
		var delegateMethod = MethodSpec.methodBuilder("delegate")
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC, FINAL)
				.returns(Logger.class)
				.addStatement("return this.delegate")
				.addJavadoc("@returns the delegate logger.")
				.build();
		var logMethod = MethodSpec.methodBuilder("log")
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC, FINAL)
				.returns(Log.class)
				.addStatement("return this.log")
				.addJavadoc("@returns the annotation instance.")
				.build();

		var typeBuilder = TypeSpec.classBuilder(concreteName)
				.addModifiers(PUBLIC, FINAL)
				.addAnnotation(AnnotationSpec
					.builder(Generated.class)
					.addMember("value", format("\"%s\"", getClass().getName()))
					.addMember("comments", "\"https://jule.dansiviter.uk/\"")
					.build())
				.addSuperinterface(BaseLog.class)
				.addSuperinterface(typeMirror)
				.addMethod(constructor)
				.addField(Log.class, "log", PRIVATE, FINAL)
				.addMethod(delegateMethod)
				.addField(String.class, "key", PUBLIC, FINAL)  // purposefully public
				.addMethod(logMethod)
				.addField(Logger.class, "delegate", PRIVATE, FINAL);

		methods(type).forEach(m -> processMethod(typeBuilder, m));

		var javaFile = JavaFile.builder(pkg.getQualifiedName().toString(), typeBuilder.build()).build();

		try {
			javaFile.writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(ERROR, e.getMessage(), type);
			return;
		}

		var resourceFileName = format("META-INF/native-image/%s/%s/native-image.json", pkg.getQualifiedName(), className);
		try {
				// would be nice to have SOURCE_OUTPUT but for some reason Maven isn't processing it
				var resourceFile = processingEnv.getFiler().createResource(CLASS_OUTPUT, "", resourceFileName);
				try (var out = Json.createWriter(resourceFile.openOutputStream())) {
					var array = Json.createArrayBuilder().add(
						Json.createObjectBuilder()
							.add("name", format("%s.%s", pkg.getQualifiedName(), concreteName))
							.add("allDeclaredConstructors", true)
							.add("allPublicMethods", true));
					out.writeArray(array.build());
				}
		} catch (IOException e) {
				processingEnv.getMessager().printMessage(ERROR, e.getMessage(), type);
		}
	}

	private Stream<? extends ExecutableElement> methods(TypeElement type) {
		var methods = type.getEnclosedElements().stream()
				.filter(e -> e.getKind() == ElementKind.METHOD && e.getAnnotation(Message.class) != null)
				.map(ExecutableElement.class::cast);

		var interfaceMethods = type.getInterfaces().stream()
				.map(processingEnv.getTypeUtils()::asElement)
				.map(TypeElement.class::cast)
				.flatMap(this::methods);

		return Stream.concat(methods, interfaceMethods);
	}

	private void processMethod(TypeSpec.Builder builder, ExecutableElement e) {
		var message = e.getAnnotation(Message.class);

		if (message.value().isEmpty()) {
			processingEnv.getMessager().printMessage(ERROR, "Message cannot be empty!", e);
			return;
		}

		var method = MethodSpec.methodBuilder(e.getSimpleName().toString())
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC)
				.returns(TypeName.get(e.getReturnType()));

		e.getParameters().forEach(p -> method.addParameter(TypeName.get(p.asType()), p.getSimpleName().toString()));

		method.beginControlFlow("if (!isLoggable($T.$N))", Level.class, message.level().name())
				.addStatement("return")
				.endControlFlow();

		if (message.once()) {
			var onceField = "ONCE__".concat(e.getSimpleName().toString());
			var onceSpec = FieldSpec.builder(AtomicBoolean.class, onceField, PRIVATE, STATIC, FINAL)
					.initializer("new $T()", AtomicBoolean.class)
					.build();
			builder.addField(onceSpec);
			method.beginControlFlow("if ($N.getAndSet(true))", onceField)
					.addStatement("return")
					.endControlFlow();
		}

		var statement = new StringBuilder("logp($T.$N, \"$N\"");
		for (VariableElement ve : e.getParameters()) {
			statement.append(", ").append(ve.getSimpleName());
		}
		statement.append(')');

		method.addStatement(statement.toString(), Level.class, message.level().name(), message.value());

		builder.addMethod(method.build());
	}

	private static String className(TypeElement typeElement) {
		var types = new ArrayList<CharSequence>();

		Element e = typeElement;
		while (e instanceof TypeElement) {
			types.add(0, ((TypeElement) e).getSimpleName());
			e = e.getEnclosingElement();
		}

		return String.join("$", types);
	}
}
