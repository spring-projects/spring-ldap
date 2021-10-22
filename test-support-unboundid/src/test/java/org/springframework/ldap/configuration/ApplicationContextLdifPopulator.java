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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.unboundid.LdifPopulator;


/**
 * Generated Java based configuration
 * 
 */
@Configuration
public class ApplicationContextLdifPopulator {


    @Bean("ldifPopulator")
    @DependsOn({
        "embeddedLdapServer"
    })
    public LdifPopulator ldifPopulator(
        @Qualifier("contextSource")
        LdapContextSource contextSource) {
        LdifPopulator bean = new LdifPopulator();
        bean.setContextSource(contextSource);
        bean.setResource(new ClassPathResource("setup_data.ldif"));
        bean.setClean(true);
        bean.setBase("dc=jayway,dc=se");
        bean.setDefaultBase("dc=jayway,dc=se");
        return bean;
    }

}
