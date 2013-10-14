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

package org.springframework.ldap.repository.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.ldap.repository.LdapRepositoryFactoryBean;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class LdapRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    private static final String ATT_LDAP_TEMPLATE_REF = "ldap-template-ref";

    @Override
    protected String getModulePrefix() {
        return "ldap";
    }

    @Override
    public String getRepositoryFactoryClassName() {
        return LdapRepositoryFactoryBean.class.getName();
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {
        Element element = config.getElement();
        String ldapTemplateRef = element.getAttribute(ATT_LDAP_TEMPLATE_REF);
        if(!StringUtils.hasText(ldapTemplateRef)) {
            ldapTemplateRef = "ldapTemplate";
        }

        builder.addPropertyReference("ldapOperations", ldapTemplateRef);
    }
}
