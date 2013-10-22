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

package org.springframework.ldap.itest.odm;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.odm.core.OdmException;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateOdmWithNoDnAnnotationsITest extends AbstractLdapTemplateIntegrationTest {
    @Autowired
    private LdapTemplate tested;

    @Test
    public void testFindOne() {
        Person person = tested.findOne(query()
                .where("cn").is("Some Person3"), Person.class);

        assertNotNull(person);
        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

    @Test
    public void testFindByDn() {
        Person person = tested.findByDn(LdapUtils.newLdapName("cn=Some Person3,ou=company1,c=Sweden"), Person.class);

        assertNotNull(person);
        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

    @Test(expected = OdmException.class)
    public void testFindByDnThrowsExceptionOnInvalidEntry() {
        tested.findByDn(LdapUtils.newLdapName("ou=company1,c=Sweden"), Person.class);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void testFindOneThrowsEmptyResultIfNotFound() {
        tested.findOne(query()
                .where("cn").is("This cn does not exist"), Person.class);
    }

    @Test
    public void testFind() {
        List<Person> persons = tested.find(query()
                .where("cn").is("Some Person3"), Person.class);

        assertEquals(1, persons.size());
        Person person = persons.get(0);

        assertNotNull(person);
        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

    @Test
    public void testFindInCountry() {
        List<Person> persons = tested.find(query()
                .base("c=Sweden")
                .where("cn").isPresent(), Person.class);

        assertEquals(4, persons.size());
        Person person = persons.get(0);

        assertNotNull(person);
    }

    @Test
    public void testFindAll() {
        List<Person> result = tested.findAll(Person.class);
        assertEquals(5, result.size());
    }

    @Test
    public void testCreate() {
        Person person = new Person();
        person.setDn(LdapNameBuilder.newLdapName("ou=company1,c=Sweden")
                .add("cn", "New Person").build());
        person.setCommonName("New Person");
        person.setSurname("Person");
        person.setDesc(Arrays.asList("This is the description"));
        person.setTelephoneNumber("0123456");

        tested.create(person);

        assertEquals(6, tested.findAll(Person.class).size());

        person = tested.findOne(query()
                .where("cn").is("New Person"), Person.class);

        assertEquals("New Person", person.getCommonName());
        assertEquals("Person", person.getSurname());
        assertEquals("This is the description", person.getDesc().get(0));
        assertEquals("0123456", person.getTelephoneNumber());
    }

    @Test
    public void testUpdate() {
        Person person = tested.findOne(query()
                .where("cn").is("Some Person3"), Person.class);

        person.setDesc(Arrays.asList("New Description"));
        tested.update(person);

        person = tested.findOne(query()
                .where("cn").is("Some Person3"), Person.class);

        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("New Description", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

    @Test
    public void testDelete() {
        Person person = tested.findOne(query()
                .where("cn").is("Some Person3"), Person.class);

        tested.delete(person);

        try {
            tested.findOne(query().where("cn").is("Some Person3"), Person.class);
            fail("EmptyResultDataAccessException e");
        } catch (EmptyResultDataAccessException e) {
            assertTrue(true);
        }
    }

    /**
     * Test case for Jira LDAP-271.
     */
    @Test
    public void testLdap271() {
        Person person = tested.findOne(query()
                .where("cn").is("Some Person3"), Person.class);

        // Perform test
        person.setTelephoneNumber(null);
        tested.update(person);

        person = tested.findOne(query()
                .where("cn").is("Some Person3"), Person.class);
        assertNull("TelephoneNumber should be null", person.getTelephoneNumber());
    }
}
