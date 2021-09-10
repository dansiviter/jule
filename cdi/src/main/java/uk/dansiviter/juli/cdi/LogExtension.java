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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import uk.dansiviter.juli.annotations.Log;

/**
 * Defines the CDI extension to inject log instances.
 */
public class LogExtension implements Extension {
	private final Map<Class<?>, Set<InjectionPoint>> found = new HashMap<>();

	/**
	 *
	 * @param pip injection point event.
	 */
	public void findBody(@Observes ProcessInjectionPoint<?, ?> pip) {
		var rawType = rawType(pip.getInjectionPoint().getType());
		if (rawType.isAnnotationPresent(Log.class)) {
			found.compute(rawType, (k, v) -> {
				if (v == null) {
					v = new HashSet<>();
				}
				v.add(pip.getInjectionPoint());
				return v;
			});
		}
	}

	/**
	 *
	 * @param abd the after bean discovery event.
	 */
	public void addBeans(@Observes AfterBeanDiscovery abd) {
		this.found.forEach((k, v) -> createBean(abd, k, v));
		this.found.clear();
	}

	/**
	 *
	 * @param abd the after bean discovery event.
	 * @param rawType the raw bean type.
	 * @param ips the injection points.
	 */
	private void createBean(
		AfterBeanDiscovery abd,
		Class<?> rawType,
		Set<InjectionPoint> ips)
	{
		abd.addBean()
				.name(rawType.getName())
				.beanClass(rawType)
				.addType(rawType)
				.produceWith(i -> {
					var ip = i.select(InjectionPoint.class).get();
					var member = ip.getMember();
					return log(rawType, member.getDeclaringClass());
				});
	}


	// --- Static Methods ---

	/**
	 *
	 * @param type the input type.
	 * @return the found raw type.asy
	 * @throws IllegalStateException if unable to find raw type.
	 */
	private static Class<?> rawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() instanceof Class<?>) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		throw new IllegalStateException("Unable to decipher bean type! [" + type + "]");
	}
}
