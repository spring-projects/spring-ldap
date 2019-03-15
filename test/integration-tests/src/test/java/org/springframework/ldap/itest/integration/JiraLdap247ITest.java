/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.ldap.itest.integration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.LdapGroupDao;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for https://jira.springsource.org/browse/LDAP-247.
 * Thanks to Jürgen Failenschmid for spotting the problem and providing the code for testing this.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldap-247-testContext.xml"})
public class JiraLdap247ITest extends AbstractLdapTemplateIntegrationTest {

    @Autowired
    private LdapGroupDao ldapGroupDao;

    @Test
    public void verifyThatBasePathIsProperlyPopulated() {
        assertThat(ldapGroupDao).isNotNull();

        // The base path should be automatically populated by BaseLdapPathBeanPostProcessor,
        // but it doesn't unless it implements Ordered, which caused the assertion below to fail.
        assertThat(ldapGroupDao.getBasePath()).isNotNull();
    }
}
