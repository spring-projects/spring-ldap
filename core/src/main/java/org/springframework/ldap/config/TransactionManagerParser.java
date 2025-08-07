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

package org.springframework.ldap.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager;
import org.springframework.ldap.transaction.compensating.support.DefaultTempEntryRenamingStrategy;
import org.springframework.ldap.transaction.compensating.support.DifferentSubtreeTempEntryRenamingStrategy;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * @author Mattias Hellborg Arthursson
 */
public class TransactionManagerParser implements BeanDefinitionParser {

	private static final String ATT_CONTEXT_SOURCE_REF = "context-source-ref";

	private static final String ATT_DATA_SOURCE_REF = "data-source-ref";

	private static final String ATT_SESSION_FACTORY_REF = "session-factory-ref";

	private static final String ATT_TEMP_SUFFIX = "temp-suffix";

	private static final String ATT_SUBTREE_NODE = "subtree-node";

	private static final String DEFAULT_ID = "transactionManager";

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {

		String contextSourceRef = ParserUtils.getString(element, ATT_CONTEXT_SOURCE_REF,
				ContextSourceParser.DEFAULT_ID);
		String dataSourceRef = element.getAttribute(ATT_DATA_SOURCE_REF);
		String sessionFactoryRef = element.getAttribute(ATT_SESSION_FACTORY_REF);

		if (StringUtils.hasText(dataSourceRef) || StringUtils.hasText(sessionFactoryRef)) {
			throw new IllegalArgumentException(String.format(
					"ContextSourceAndHibernateTransactionManager and ContextSourceAndDataSourceTransactionManager are removed in Spring LDAP 4.0. Please remove your usage of data-source-ref and session-factory-ref.",
					ATT_DATA_SOURCE_REF, ATT_SESSION_FACTORY_REF));
		}

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ContextSourceTransactionManager.class);

		builder.addPropertyReference("contextSource", contextSourceRef);

		Element defaultStrategyChild = DomUtils.getChildElementByTagName(element, Elements.DEFAULT_RENAMING_STRATEGY);
		Element differentSubtreeChild = DomUtils.getChildElementByTagName(element,
				Elements.DIFFERENT_SUBTREE_RENAMING_STRATEGY);

		if (defaultStrategyChild != null) {
			builder.addPropertyValue("renamingStrategy", parseDefaultRenamingStrategy(defaultStrategyChild));
		}

		if (differentSubtreeChild != null) {
			builder.addPropertyValue("renamingStrategy", parseDifferentSubtreeRenamingStrategy(differentSubtreeChild));
		}

		String id = ParserUtils.getString(element, AbstractBeanDefinitionParser.ID_ATTRIBUTE, DEFAULT_ID);

		BeanDefinition beanDefinition = builder.getBeanDefinition();
		parserContext.registerBeanComponent(new BeanComponentDefinition(beanDefinition, id));

		return beanDefinition;
	}

	private BeanDefinition parseDifferentSubtreeRenamingStrategy(Element element) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
			.rootBeanDefinition(DifferentSubtreeTempEntryRenamingStrategy.class);

		String subtreeNode = element.getAttribute(ATT_SUBTREE_NODE);
		Assert.hasText(subtreeNode, ATT_SUBTREE_NODE + " must be specified");

		builder.addConstructorArgValue(subtreeNode);

		return builder.getBeanDefinition();
	}

	public BeanDefinition parseDefaultRenamingStrategy(Element element) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
			.rootBeanDefinition(DefaultTempEntryRenamingStrategy.class);

		builder.addPropertyValue("tempSuffix",
				ParserUtils.getString(element, ATT_TEMP_SUFFIX, DefaultTempEntryRenamingStrategy.DEFAULT_TEMP_SUFFIX));

		return builder.getBeanDefinition();
	}

}
