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
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool.factory.MutablePoolingContextSource;
import org.springframework.ldap.pool.validation.DefaultDirContextValidator;


/**
 * Generated Java based configuration
 * 
 */
@Configuration
public class LdapTemplateTestContext {


    @Bean("poolingContextSource")
    public MutablePoolingContextSource poolingContextSource(
        @Qualifier("contextSource")
        LdapContextSource contextSource) {
        MutablePoolingContextSource bean = new MutablePoolingContextSource();
        bean.setContextSource(contextSource);
        bean.setMaxTotal(1);
        bean.setDirContextValidator(new DefaultDirContextValidator());
        bean.setTestOnBorrow(true);
        bean.setTestWhileIdle(true);
        bean.setMaxActive(1);
        return bean;
    }

    @Bean
    public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertyPlaceholderConfigurer bean = new PropertyPlaceholderConfigurer();
        bean.setLocation(new ClassPathResource("/conf/ldap.properties"));
        bean.setSystemPropertiesModeName("SYSTEM_PROPERTIES_MODE_OVERRIDE");
        return bean;
    }

}
