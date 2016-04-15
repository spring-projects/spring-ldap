/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.ldap.itest.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.odm.Person;
import org.springframework.ldap.itest.odm.QPerson;
import org.springframework.ldap.itest.repositories.PersonQueryDslRepository;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Spring LDAP automatic repository scan functionality.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/repositoryScanTestContext.xml"})
public class RepositoryScanQueryDslITest extends AbstractLdapTemplateIntegrationTest {

    @Autowired
    private PersonQueryDslRepository tested;

    @Test
    public void testFindOneWithPredicate() {
        QPerson person = QPerson.person;

        Person found = tested.findOne(person.commonName.eq("Some Person3"));

        assertThat(found).isNotNull();
        assertThat(found.getCommonName()).isEqualTo("Some Person3");
        assertThat(found.getSurname()).isEqualTo("Person3");
        assertThat(found.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(found.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }

    @Test
    public void testFindAllWithPredicate() {
        QPerson person = QPerson.person;

        Iterable<Person> foundPersons = tested.findAll(person.commonName.eq("Some Person3"));

        Person found = foundPersons.iterator().next();
        assertThat(found.getCommonName()).isEqualTo("Some Person3");
        assertThat(found.getSurname()).isEqualTo("Person3");
        assertThat(found.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(found.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }

    @Test
    public void testCountWithPredicate() {
        QPerson person = QPerson.person;

        long count = tested.count(person.commonName.eq("Some Person3"));
        assertThat(count).isEqualTo(1);
    }
}
