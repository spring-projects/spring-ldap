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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import static org.springframework.ldap.config.ParserUtils.getBoolean;
import static org.springframework.ldap.config.ParserUtils.getInt;
import static org.springframework.ldap.config.ParserUtils.getString;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapTemplateParser implements BeanDefinitionParser {
	private static final String ATT_COUNT_LIMIT = "count-limit";
	private static final String ATT_TIME_LIMIT = "time-limit";
	private static final String ATT_SEARCH_SCOPE = "search-scope";
	private static final String ATT_IGNORE_PARTIAL_RESULT = "ignore-partial-result";
	private static final String ATT_IGNORE_NAME_NOT_FOUND = "ignore-name-not-found";
	private static final String ATT_ODM_REF = "odm-ref";
	private static final String ATT_CONTEXT_SOURCE_REF = "context-source-ref";

	private static final String DEFAULT_ID = "ldapTemplate";
	private static final int DEFAULT_COUNT_LIMIT = 0;
	private static final int DEFAULT_TIME_LIMIT = 0;

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(LdapTemplate.class);

		String contextSourceRef = getString(element, ATT_CONTEXT_SOURCE_REF, ContextSourceParser.DEFAULT_ID);
		builder.addPropertyReference("contextSource", contextSourceRef);
		builder.addPropertyValue("defaultCountLimit", getInt(element, ATT_COUNT_LIMIT, DEFAULT_COUNT_LIMIT));
		builder.addPropertyValue("defaultTimeLimit", getInt(element, ATT_TIME_LIMIT, DEFAULT_TIME_LIMIT));

		String searchScope = getString(element, ATT_SEARCH_SCOPE, SearchScope.SUBTREE.toString());
		builder.addPropertyValue("defaultSearchScope", SearchScope.valueOf(searchScope).getId());
		builder.addPropertyValue("ignorePartialResultException", getBoolean(element, ATT_IGNORE_PARTIAL_RESULT, false));
		builder.addPropertyValue("ignoreNameNotFoundException", getBoolean(element, ATT_IGNORE_NAME_NOT_FOUND, false));

		String odmRef = element.getAttribute(ATT_ODM_REF);
		if(StringUtils.hasText(odmRef)) {
			builder.addPropertyReference("objectDirectoryMapper", odmRef);
		}

		String id = getString(element, AbstractBeanDefinitionParser.ID_ATTRIBUTE, DEFAULT_ID);

		BeanDefinition beanDefinition = builder.getBeanDefinition();
		parserContext.registerBeanComponent(new BeanComponentDefinition(beanDefinition, id));

		return beanDefinition;
	}
}
