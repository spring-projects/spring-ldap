/*
 * Copyright 2002-2025 the original author or authors.
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

package org.springframework.ldap.odm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Fallback;
import org.springframework.core.convert.ConversionService;
import org.springframework.ldap.convert.ConversionServiceBeanFactoryPostProcessor;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.odm.core.impl.DefaultObjectDirectoryMapper;

/**
 * Configuration class for {@link ObjectDirectoryMapper}
 *
 * @author Josh Cummings
 * @since 3.3
 */
@Configuration(proxyBeanMethods = false)
public class ObjectDirectoryMapperConfiguration {

	@Bean
	static ConversionServiceBeanFactoryPostProcessor conversionServiceBeanPostProcessor() {
		return new ConversionServiceBeanFactoryPostProcessor();
	}

	@Bean
	@Fallback
	ObjectDirectoryMapper objectDirectoryMapper(ConversionService conversionService) {
		DefaultObjectDirectoryMapper objectDirectoryMapper = new DefaultObjectDirectoryMapper();
		objectDirectoryMapper.setConversionService(conversionService);
		return objectDirectoryMapper;
	}

}
