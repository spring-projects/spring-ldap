/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ldap.transaction.compensating.support.DefaultTempEntryRenamingStrategy;

/**
 * @author Mattias Hellborg Arthursson
 */
public class DefaultRenamingStrategyParser implements BeanDefinitionParser {

	private static final String ATT_TEMP_SUFFIX = "temp-suffix";

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
			.rootBeanDefinition(DefaultTempEntryRenamingStrategy.class);

		builder.addPropertyValue("tempSuffix",
				ParserUtils.getString(element, ATT_TEMP_SUFFIX, DefaultTempEntryRenamingStrategy.DEFAULT_TEMP_SUFFIX));

		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		parserContext.getContainingBeanDefinition()
			.getPropertyValues()
			.addPropertyValue("renamingStrategy", beanDefinition);

		return beanDefinition;
	}

}
