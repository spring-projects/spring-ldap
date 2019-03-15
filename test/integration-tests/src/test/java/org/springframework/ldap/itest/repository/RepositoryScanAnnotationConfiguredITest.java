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

package org.springframework.ldap.itest.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.odm.Person;
import org.springframework.ldap.itest.repositories.PersonRepository;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Name;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Spring LDAP automatic repository scan functionality enabled with
 * {@link org.springframework.ldap.repository.config.EnableLdapRepositories}.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/repositoryScanAnnotationTestContext.xml"})
public class RepositoryScanAnnotationConfiguredITest extends AbstractLdapTemplateIntegrationTest {
    private static final Name PERSON3_DN = LdapUtils.newLdapName("cn=Some Person3, ou=Company1, ou=Sweden");

    @Autowired
    private PersonRepository tested;

    @Test
    public void testExists() {
        assertThat(tested.exists(PERSON3_DN)).isTrue();
    }

    @Test
    public void testFindOneWithDn() {
        Person person = tested.findOne(PERSON3_DN);

        assertThat(person).isNotNull();
        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }
}
