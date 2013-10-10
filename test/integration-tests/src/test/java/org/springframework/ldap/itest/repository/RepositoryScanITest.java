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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.odm.Person;
import org.springframework.ldap.itest.repositories.PersonRepository;
import org.springframework.ldap.odm.core.OdmException;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Tests for Spring LDAP automatic repository scan functionality.
 * 
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = {"/conf/repositoryScanTestContext.xml"})
public class RepositoryScanITest extends AbstractLdapTemplateIntegrationTest {
    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private PersonRepository tested;


    @Test
    public void testFindOne() {
        Person person = tested.findOne(LdapUtils.newLdapName("cn=Some Person3, ou=Company1, c=Sweden"));

        assertNotNull(person);
        Assert.assertEquals("Some Person3", person.getCommonName());
        Assert.assertEquals("Person3", person.getSurname());
        Assert.assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        Assert.assertEquals("+46 555-123654", person.getTelephoneNumber());
    }

//    @Test
//    public void testFindByDn() {
//        Person person = tested.findByDn(LdapUtils.newLdapName("cn=Some Person3,ou=company1,c=Sweden"), Person.class);
//
//        assertNotNull(person);
//        Assert.assertEquals("Some Person3", person.getCommonName());
//        Assert.assertEquals("Person3", person.getSurname());
//        Assert.assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
//        Assert.assertEquals("+46 555-123654", person.getTelephoneNumber());
//    }
//
//    @Test(expected = OdmException.class)
//    public void testFindByDnThrowsExceptionOnInvalidEntry() {
//        tested.findByDn(LdapUtils.newLdapName("ou=company1,c=Sweden"), Person.class);
//    }
//
//    @Test(expected = EmptyResultDataAccessException.class)
//    public void testFindOneThrowsEmptyResultIfNotFound() {
//        tested.findOne(query()
//                .where("cn").is("This cn does not exist"), Person.class);
//    }
//
//    @Test
//    public void testFind() {
//        List<Person> persons = tested.find(query()
//                .where("cn").is("Some Person3"), Person.class);
//
//        Assert.assertEquals(1, persons.size());
//        Person person = persons.get(0);
//
//        assertNotNull(person);
//        Assert.assertEquals("Some Person3", person.getCommonName());
//        Assert.assertEquals("Person3", person.getSurname());
//        Assert.assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
//        Assert.assertEquals("+46 555-123654", person.getTelephoneNumber());
//    }
//
//    @Test
//    public void testFindInCountry() {
//        List<Person> persons = tested.find(query()
//                .base("c=Sweden")
//                .where("cn").isPresent(), Person.class);
//
//        Assert.assertEquals(4, persons.size());
//        Person person = persons.get(0);
//
//        assertNotNull(person);
//    }
//
//    @Test
//    public void testFindAll() {
//        List<Person> result = tested.findAll(Person.class);
//        Assert.assertEquals(5, result.size());
//    }
//
//    @Test
//    public void testCreate() {
//        Person person = new Person();
//        person.setDn(LdapNameBuilder.newLdapName("ou=company1,c=Sweden")
//                .add("cn", "New Person").build());
//        person.setCommonName("New Person");
//        person.setSurname("Person");
//        person.setDesc(Arrays.asList("This is the description"));
//        person.setTelephoneNumber("0123456");
//
//        tested.create(person);
//
//        Assert.assertEquals(6, tested.findAll(Person.class).size());
//
//        person = tested.findOne(query()
//                .where("cn").is("New Person"), Person.class);
//
//        Assert.assertEquals("New Person", person.getCommonName());
//        Assert.assertEquals("Person", person.getSurname());
//        Assert.assertEquals("This is the description", person.getDesc().get(0));
//        Assert.assertEquals("0123456", person.getTelephoneNumber());
//    }
//
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
//        Assert.assertEquals("Some Person3", person.getCommonName());
//        Assert.assertEquals("Person3", person.getSurname());
//        Assert.assertEquals("New Description", person.getDesc().get(0));
//        Assert.assertEquals("+46 555-123654", person.getTelephoneNumber());
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
//            Assert.assertTrue(true);
//        }
//    }
}
