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

package org.springframework.ldap.itest.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.odm.Person;
import org.springframework.ldap.itest.odm.QPerson;
import org.springframework.ldap.itest.repositories.PersonQueryDslRepository;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

        assertNotNull(found);
        assertEquals("Some Person3", found.getCommonName());
        assertEquals("Person3", found.getSurname());
        assertEquals("Sweden, Company1, Some Person3", found.getDesc().get(0));
        assertEquals("+46 555-123654", found.getTelephoneNumber());
    }

    @Test
    public void testFindAllWithPredicate() {
        QPerson person = QPerson.person;

        Iterable<Person> foundPersons = tested.findAll(person.commonName.eq("Some Person3"));

        Person found = foundPersons.iterator().next();
        assertEquals("Some Person3", found.getCommonName());
        assertEquals("Person3", found.getSurname());
        assertEquals("Sweden, Company1, Some Person3", found.getDesc().get(0));
        assertEquals("+46 555-123654", found.getTelephoneNumber());
    }

    @Test
    public void testCountWithPredicate() {
        QPerson person = QPerson.person;

        long count = tested.count(person.commonName.eq("Some Person3"));
        assertEquals(1, count);
    }
}
