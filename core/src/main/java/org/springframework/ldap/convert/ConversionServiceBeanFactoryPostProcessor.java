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

package org.springframework.ldap.convert;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;

/**
 * A {@link BeanFactoryPostProcessor} to add Spring LDAP converters to the default
 * {@link ConversionService}.
 *
 * <p>
 * Note that this post processor will ignore user-defined {@link ConversionService} beans
 * named {@code conversionService}.
 *
 * <p>
 * In that case, you can apply {@link ConverterUtils#addDefaultConverters} to your
 * instance instead
 *
 * @author Josh Cummings
 * @since 3.3
 */
public final class ConversionServiceBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	private static final String CONVERSION_SERVICE_BEAN_NAME = "conversionService";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (hasUserDefinedConversionService(beanFactory)) {
			return;
		}
		ConversionService service = beanFactory.getConversionService();
		if (service instanceof ConverterRegistry registry) {
			ConverterUtils.addDefaultConverters(registry);
		}
	}

	private boolean hasUserDefinedConversionService(ConfigurableListableBeanFactory beanFactory) {
		return beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME)
				&& beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
	}

}
