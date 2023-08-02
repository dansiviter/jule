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
package uk.dansiviter.jule.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Designates the class as a logger wrapper.
 * <p>
 * Usage:
 * <pre>
 * &#064;Log
 * public interface MyLogger {
 *   &#064;Message("Hello")
 *   void hello();
 * }
 * </pre>
 *
 * @see Message
 */
@Target({ TYPE, FIELD })
@Retention(RUNTIME)
public @interface Logger {
	/**
	 * @return the name of the resource bundle.
	 * @see java.util.logging.Logger#setResourceBundle(java.util.ResourceBundle)
	 */
	String resourceBundleName() default "";

	/**
	 * @return the generated log type.
	 */
	Type type() default Type.JUL;

	/**
	 * @return the lifecycle management of the logger.
	 */
	Lifecycle lifecycle() default Lifecycle.DEFAULT;


	/**
	 * Type of underlying delegate logger.
	 */
	public enum Type {
		/**
		 * Uses {@link java.util.logging.Logger} as delegate.
		 */
		JUL,
		/**
		 * <strong>EXPERIMENTAL</strong> Uses {@link java.lang.System.Logger} as delegate.
		 */
		SYSTEM
	}

	/**
	 * Type of lifecycle for the logger.
	 */
	public enum Lifecycle {
		/**
		 * Logger can be accessed via {@code LogFactory#log(...)} methods.
		 */
		DEFAULT,
		/**
		 * In addition to {@link #DEFAULT} logger lifecycle will be managed by CDI as a {@code jakarta.enterprise.context.Dependent}
		 */
		CDI
	}
}
