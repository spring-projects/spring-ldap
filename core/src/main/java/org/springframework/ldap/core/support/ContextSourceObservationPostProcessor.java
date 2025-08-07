/*
 * Copyright 2006-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.core.support;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.ldap.core.ContextSource;

/**
 * A {@link BeanPostProcessor} that makes any {@link ContextSource} bean observable by
 * Micrometer
 *
 * @author Josh Cummings
 * @since 3.3
 */
public final class ContextSourceObservationPostProcessor implements BeanPostProcessor {

	private final ObjectProvider<ObservationRegistry> observationRegistry;

	public ContextSourceObservationPostProcessor(ObjectProvider<ObservationRegistry> observationRegistry) {
		this.observationRegistry = observationRegistry;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ObservationContextSource) {
			return bean;
		}
		if (!(bean instanceof BaseLdapPathContextSource ldap)) {
			return bean;
		}
		ObservationRegistry observationRegistry = this.observationRegistry
			.getIfAvailable(() -> ObservationRegistry.NOOP);
		if (observationRegistry.isNoop()) {
			return bean;
		}
		return new ObservationContextSource(ldap, observationRegistry);
	}

}
