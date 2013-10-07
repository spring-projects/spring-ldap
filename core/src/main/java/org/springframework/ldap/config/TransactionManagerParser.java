/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager;
import org.springframework.ldap.transaction.compensating.support.DefaultTempEntryRenamingStrategy;
import org.springframework.ldap.transaction.compensating.support.DifferentSubtreeTempEntryRenamingStrategy;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.springframework.ldap.config.ParserUtils.NAMESPACE;
import static org.springframework.ldap.config.ParserUtils.getString;

/**
 * @author Mattias Hellborg Arthursson
 */
public class TransactionManagerParser implements BeanDefinitionParser {
    private final static String ATT_CONTEXT_SOURCE_REF = "context-source-ref";
    private final static String ATT_DATA_SOURCE_REF = "data-source-ref";
    private final static String ATT_SESSION_FACTORY_REF = "session-factory-ref";

    private final static String ATT_TEMP_SUFFIX = "temp-suffix";
    private final static String ATT_SUBTREE_NODE = "subtree-node";

    private final static String DEFAULT_ID = "transactionManager";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        String contextSourceRef = getString(element, ATT_CONTEXT_SOURCE_REF, ContextSourceParser.DEFAULT_ID);
        String dataSourceRef = element.getAttribute(ATT_DATA_SOURCE_REF);
        String sessionFactoryRef = element.getAttribute(ATT_SESSION_FACTORY_REF);

        if(StringUtils.hasText(dataSourceRef) && StringUtils.hasText(sessionFactoryRef)) {
            throw new IllegalArgumentException(
                    String.format("Only one of %s and %s can be specified",
                            ATT_DATA_SOURCE_REF, ATT_SESSION_FACTORY_REF));
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ContextSourceTransactionManager.class);
        builder.addPropertyReference("contextSource", contextSourceRef);

        NodeList defaultStrategyChildren =
                element.getElementsByTagNameNS(NAMESPACE, Elements.DEFAULT_RENAMING_STRATEGY);
        NodeList differentSubtreeChildren =
                element.getElementsByTagNameNS(NAMESPACE, Elements.DIFFERENT_SUBTREE_RENAMING_STRATEGY);

        if(defaultStrategyChildren.getLength() == 1) {
            builder.addPropertyValue("renamingStrategy", parseDefaultRenamingStrategy((Element) defaultStrategyChildren.item(0)));
        }

        if(differentSubtreeChildren.getLength() == 1) {
            builder.addPropertyValue("renamingStrategy", parseDifferentSubtreeRenamingStrategy((Element) differentSubtreeChildren.item(0)));
        }

        String id = getString(element, AbstractBeanDefinitionParser.ID_ATTRIBUTE, DEFAULT_ID);

        BeanDefinition beanDefinition = builder.getBeanDefinition();
        parserContext.registerBeanComponent(new BeanComponentDefinition(beanDefinition, id));

        return beanDefinition;
    }

    private BeanDefinition parseDifferentSubtreeRenamingStrategy(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(DifferentSubtreeTempEntryRenamingStrategy.class);

        String subtreeNode = element.getAttribute(ATT_SUBTREE_NODE);
        Assert.hasText(subtreeNode, ATT_SUBTREE_NODE + " must be specified");

        builder.addConstructorArgValue(subtreeNode);

        return builder.getBeanDefinition();
    }

    public BeanDefinition parseDefaultRenamingStrategy(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(DefaultTempEntryRenamingStrategy.class);

        builder.addPropertyValue("tempSuffix",
                getString(element, ATT_TEMP_SUFFIX,
                        DefaultTempEntryRenamingStrategy.DEFAULT_TEMP_SUFFIX));

        return builder.getBeanDefinition();
    }

}
