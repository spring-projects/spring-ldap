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

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.odm.Person;
import org.springframework.ldap.itest.repositories.PersonRepository;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Tests for Spring LDAP automatic repository scan functionality.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/repositoryScanTestContext.xml"})
public class RepositoryScanITest extends AbstractLdapTemplateIntegrationTest {
    private static final Name PERSON1_DN = LdapUtils.newLdapName("cn=Some Person, ou=Company1, ou=Sweden");
    private static final Name PERSON3_DN = LdapUtils.newLdapName("cn=Some Person3, ou=Company1, ou=Sweden");

    @Autowired
    private PersonRepository tested;

    @Test
    public void testExists() {
        assertTrue(tested.exists(PERSON3_DN));
    }

    @Test
    public void testFindOneWithDn() {
        Person person = tested.findOne(PERSON3_DN);

        assertNotNull(person);
        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

    @Test
    public void verifyThatFindOneWithNonexistingDnReturnsNull() {
        Person person = tested.findOne(LdapUtils.newLdapName("cn=unknown"));
        assertNull(person);
    }

    @Test
    public void testFindOneWithQuery() {
        Person person = tested.findOne(query().where("cn").is("Some Person3"));

        assertNotNull(person);
        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

    @Test
    public void testFindAll() {
        Iterable<Person> result = tested.findAll();
        assertEquals(5, countIterable(result));

        for (Person person : result) {
            if(StringUtils.equals(person.getCommonName(), "Some Person3")) {
                assertEquals("Some Person3", person.getCommonName());
                assertEquals("Person3", person.getSurname());
                assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
                assertEquals("+46 555-123654", person.getTelephoneNumber());

                // Done
                return;
            }
        }

        fail("Entry not found");
    }

    @Test
    public void testFindAllWithQuery() {
        Iterable<Person> persons = tested.findAll(query().where("cn").is("Some Person3"));

        assertNotNull(persons);
        Iterator<Person> iterator = persons.iterator();
        Person person = iterator.next();

        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testFindAllWithIterable() {
        Iterable<Person> persons = tested.findAll(Arrays.asList(PERSON1_DN, PERSON3_DN));
        Iterator<Person> iterator = persons.iterator();
        Person person = iterator.next();

        assertEquals("Some Person", person.getCommonName());
        assertEquals("Person", person.getSurname());
        assertEquals("Sweden, Company1, Some Person", person.getDesc().get(0));
        assertEquals("+46 555-123456", person.getTelephoneNumber());

        person = iterator.next();

        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());

        assertFalse(iterator.hasNext());
    }

    @Test
    public void verifyThatFindAllWithIterableFiltersNotFoundEntries() {
        Iterable<Person> persons = tested.findAll(Arrays.asList(LdapUtils.newLdapName("cn=unknown"), PERSON3_DN));
        Iterator<Person> iterator = persons.iterator();
        Person person = iterator.next();

        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());

        assertFalse(iterator.hasNext());
    }

    @Test
    public void testCount() {
        assertEquals(5, tested.count());
    }

    private int countIterable(Iterable<?> iterable) {
        int count = 0;

        for (Object o : iterable) {
            count++;
        }
        return count;
    }

    @Test
    public void testCreate() {
        Person person = new Person();
        LdapName dn = LdapNameBuilder.newInstance("ou=company1,ou=Sweden")
                .add("cn", "New Person").build();
        person.setDn(dn);
        person.setCommonName("New Person");
        person.setSurname("Person");
        person.setDesc(Arrays.asList("This is the description"));
        person.setTelephoneNumber("0123456");
        person.setNew();
        tested.save(person);

        assertEquals(6, tested.count());

        person = tested.findOne(dn);

        assertEquals("New Person", person.getCommonName());
        assertEquals("Person", person.getSurname());
        assertEquals("This is the description", person.getDesc().get(0));
        assertEquals("0123456", person.getTelephoneNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyThatCreateWithNoIdSetAndNotAbleToCalculateThrowsIllegalArgument() {
        Person person = new Person();
        person.setCommonName("New Person");
        person.setSurname("Person");
        person.setDesc(Arrays.asList("This is the description"));
        person.setTelephoneNumber("0123456");
        person.setNew();
        tested.save(person);
    }

    @Test
    public void testUpdate() {
        Person person = tested.findOne(query().where("cn").is("Some Person3"));

        person.setDesc(Arrays.asList("New Description"));
        tested.save(person);

        person = tested.findOne(query().where("cn").is("Some Person3"));

        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("New Description", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

    @Test
    public void testDelete() {
        Person person = tested.findOne(query().where("cn").is("Some Person3"));
        tested.delete(person);

        Person found = tested.findOne(query().where("cn").is("Some Person3"));
        assertNull(found);
    }

    @Test
    public void testDeleteWithIterable() {
        Iterable<Person> found = tested.findAll(Arrays.asList(PERSON1_DN, PERSON3_DN));
        tested.delete(found);

        assertEquals(3, tested.count());
    }

    @Test
    public void testFindByLastName() {
        Iterable<Person> found = tested.findByLastName("Person3");
        assertEquals(1, countIterable(found));
    }

    @Test
    public void testFindByUid() {
        Person person = tested.findByUid("some.person3");

        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

    @Test
    public void testFindByPhoneNumber() {
        Person person = tested.findByTelephoneNumber("+46 555-123654");

        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

}
