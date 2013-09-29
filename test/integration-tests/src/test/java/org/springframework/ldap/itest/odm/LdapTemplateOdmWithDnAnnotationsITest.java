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
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateOdmWithDnAnnotationsITest extends AbstractLdapTemplateIntegrationTest {
    @Autowired
    private LdapTemplate tested;

    @Test
    public void testFindOne() {
        PersonWithDnAnnotations person = tested.findOne(query()
                .where("cn").is("Some Person3"), PersonWithDnAnnotations.class);

        assertNotNull(person);
        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());

        // Automatically calculated
        assertEquals("company1", person.getCompany());
        assertEquals("Sweden", person.getCountry());
    }

    @Test
    public void testFindByDn() {
        PersonWithDnAnnotations person = tested.findByDn(LdapUtils.newLdapName("cn=Some Person3,ou=company1,c=Sweden"),
                PersonWithDnAnnotations.class);

        assertNotNull(person);
        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());

        // Automatically calculated
        assertEquals("company1", person.getCompany());
        assertEquals("Sweden", person.getCountry());
    }

    @Test
    public void testFindInCountry() {
        List<PersonWithDnAnnotations> persons = tested.find(query()
                .base("c=Sweden")
                .where("cn").isPresent(), PersonWithDnAnnotations.class);

        assertEquals(4, persons.size());

        PersonWithDnAnnotations person = findPerson(persons, "Some Person3");

        // Automatically calculated
        assertEquals("company1", person.getCompany());
        assertEquals("Sweden", person.getCountry());
    }

    private PersonWithDnAnnotations findPerson(List<PersonWithDnAnnotations> persons, String cn) {
        for (PersonWithDnAnnotations person : persons) {
            if(person.getCommonName().equals(cn)) {
                return person;
            }
        }

        fail(String.format("Person with cn %s not found", cn));
        // we'll never get here
        return null;
    }

    @Test
    public void testCreateWithCalculatedDn() {
        PersonWithDnAnnotations person = new PersonWithDnAnnotations();

        // Don't explicitly set DN.
        person.setCommonName("New Person");
        person.setSurname("Person");
        person.setDesc(Arrays.asList("This is the description"));
        person.setTelephoneNumber("0123456");
        person.setCompany("company1");
        person.setCountry("Sweden");

        tested.create(person);

        assertEquals(6, tested.findAll(PersonWithDnAnnotations.class).size());

        person = tested.findByDn(LdapUtils.newLdapName("cn=New Person,ou=company1,c=Sweden"),
                PersonWithDnAnnotations.class);

        assertEquals("New Person", person.getCommonName());
        assertEquals("Person", person.getSurname());
        assertEquals("This is the description", person.getDesc().get(0));
        assertEquals("0123456", person.getTelephoneNumber());

        // Automatically calculated
        assertEquals("company1", person.getCompany());
        assertEquals("Sweden", person.getCountry());
    }

//    @Test
//    public void testUpdate() {
//        Person person = tested.findOne(query()
//                .where("cn").is("Some Person3"), Person.class);
//
//        person.setDesc(Arrays.asList("New Description"));
//        tested.update(person);
//
//        person = tested.findOne(query()
//                .where("cn").is("Some Person3"), Person.class);
//
//        assertEquals("Some Person3", person.getCommonName());
//        assertEquals("Person3", person.getSurname());
//        assertEquals("New Description", person.getDesc().get(0));
//        assertEquals("+46 555-123654", person.getTelephoneNumber());
//    }
//
//    @Test
//    public void testDelete() {
//        Person person = tested.findOne(query()
//                .where("cn").is("Some Person3"), Person.class);
//
//        tested.delete(person);
//
//        try {
//            tested.findOne(query().where("cn").is("Some Person3"), Person.class);
//            fail("EmptyResultDataAccessException e");
//        } catch (EmptyResultDataAccessException e) {
//            assertTrue(true);
//        }
//    }
}
