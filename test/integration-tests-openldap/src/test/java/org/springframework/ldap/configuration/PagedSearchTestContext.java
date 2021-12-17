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
package org.springframework.ldap.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;


/**
 * Generated Java based configuration
 * 
 */
@Configuration
public class PagedSearchTestContext {

    @Value("${itest.openldap.url}")
    private String itestOpenldapUrl;
    @Value("${itest.openldap.user.dn}")
    private String itestOpenldapUserDn;
    @Value("${itest.openldap.user.password}")
    private String itestOpenldapUserPassword;
    @Value("${itest.openldap.base}")
    private String itestOpenldapBase;

    @Bean("contextSource")
    public LdapContextSource contextSource() {
        LdapContextSource bean = new LdapContextSource();
        bean.setUrl(itestOpenldapUrl);
        bean.setUserDn(itestOpenldapUserDn);
        bean.setPassword(itestOpenldapUserPassword);
        bean.setBase(itestOpenldapBase);
        bean.setPooled(false);
        return bean;
    }

    @Bean
    public LdapTemplate ldapTemplate(
        @Qualifier("contextSource")
        LdapContextSource contextSource) {
        LdapTemplate bean = new LdapTemplate();
        bean.setContextSource(contextSource);
        return bean;
    }

}
