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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import uk.dansiviter.juli.annotations.Log;

/**
 *
 */
public class LogExtension implements Extension {
	private final List<InjectionPoint> found = new ArrayList<>();

	/**
	 *
	 * @param pip injection point event.
	 */
	public void findBody(@Observes ProcessInjectionPoint<?, ?> pip) {
		if (getRawType(pip.getInjectionPoint().getType()).isAnnotationPresent(Log.class)) {
			found.add(pip.getInjectionPoint());
		}
	}

	/**
	 *
	 * @param abd after bean discovery event.
	 */
	public void addBeans(@Observes AfterBeanDiscovery abd) {
		this.found.forEach(ip -> createBean(abd, ip));
		this.found.clear();
	}

	/**
	 *
	 * @param abd the bean discovery event.
	 * @param ip the injection point.
	 * @return the created bean instance.
	 */
	private void createBean(
			@Nonnull AfterBeanDiscovery abd,
			@Nonnull InjectionPoint ip)
	{
		final var type = ip.getType();
		final var rawType = getRawType(type);
		abd.addBean()
				.name(ip.getMember().getName())
				.beanClass(rawType)
				.types(type, Object.class)
				.qualifiers(ip.getQualifiers())
				.createWith(cc -> log(rawType, ip.getMember().getDeclaringClass()));
	}

	/**
	 *
	 * @param type the input type.
	 * @return the found raw type.
	 * @throws IllegalStateException if unable to find raw type.
	 */
	private static Class<?> getRawType(@Nonnull Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() instanceof Class<?>) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		throw new IllegalStateException("Unable to decipher bean type! [" + type + "]");
	}
}
