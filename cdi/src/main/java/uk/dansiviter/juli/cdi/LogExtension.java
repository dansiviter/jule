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
package uk.dansiviter.juli.cdi;

import static uk.dansiviter.juli.LogProducer.log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import uk.dansiviter.juli.annotations.Log;

/**
 * Defines the CDI extension to inject log instances.
 */
public class LogExtension implements Extension {
	private final Set<Class<?>> found = new LinkedHashSet<>();

	/**
	 *
	 * @param pip injection point event.
	 */
	public void findBody(@Observes ProcessInjectionPoint<?, ?> pip) {
		var rawType = rawType(pip.getInjectionPoint().getType());
		if (rawType.isAnnotationPresent(Log.class)) {
			found.add(rawType);
		}
	}

	/**
	 *
	 * @param abd the after bean discovery event.
	 */
	public void addBeans(@Observes AfterBeanDiscovery abd) {
		this.found.forEach(v -> createBean(abd, v));
		this.found.clear();
	}

	/**
	 *
	 * @param abd the after bean discovery event.
	 * @param rawType the raw bean type.
	 */
	private void createBean(
			AfterBeanDiscovery abd,
			Class<?> rawType)
	{
		abd.addBean()
				.name(rawType.getName())
				.beanClass(rawType)
				.addType(rawType)
				.produceWith(i -> produce(i, rawType));
	}


	// --- Static Methods ---

	private static Class<?> rawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() instanceof Class<?>) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		throw new IllegalStateException("Unable to decipher bean type! [" + type + "]");
	}

	private static Object produce(Instance<Object> i, Class<?> rawType) {
		var ip = i.select(InjectionPoint.class).get();
		return log(rawType, ip.getMember().getDeclaringClass());
	}
}
