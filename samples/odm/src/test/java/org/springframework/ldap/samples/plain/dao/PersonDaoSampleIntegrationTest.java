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
package org.springframework.ldap.samples.plain.dao;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.samples.plain.domain.Person;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Abstract base class for PersonDao integration tests.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ContextConfiguration("/config/testContext.xml")
public class PersonDaoSampleIntegrationTest extends
        AbstractJUnit4SpringContextTests {

    protected Person person;

    @Autowired
    private PersonDao personDao;

    @Before
    public void preparePerson() throws Exception {
        person = new Person();
        person.setCountry("Sweden");
        person.setCompany("company1");
        person.setFullName("Some Person");
        person.setLastName("Person");
        person
                .setDescription("Sweden, Company1, Some Person");
        person.setPhone("+46 555-123456");
    }

    /**
     * Having a single test method test create, update and delete is not exactly
     * the ideal way of testing, since they depend on each other. A better way
     * would be to separate the tests and load a test fixture before each
     * operation, in order to guarantee the expected state every time. See the
     * ldaptemplate-person sample for the correct way to do this.
     */
    @Test
    public void testCreateUpdateDelete() {
        try {
            person.setFullName("Another Person");
            personDao.create(person);
            personDao.findByPrimaryKey(
                    "Sweden", "company1",
                    "Another Person");
            // if we got here, create succeeded

            person.setDescription("Another description");
            personDao.update(person);
            Person result = personDao
                    .findByPrimaryKey(
                            "Sweden", "company1",
                            "Another Person");
            assertEquals(
                    "Another description", result
                    .getDescription());
        } finally {
            personDao.delete(person);
            try {
                personDao.findByPrimaryKey(
                        "Sweden", "company1",
                        "Another Person");
                fail("NameNotFoundException (when using Spring LDAP) or RuntimeException (when using traditional) expected");
            } catch (NameNotFoundException expected) {
                // expected
            } catch (RuntimeException expected) {
                // expected
            }
        }
    }

    @Test
    public void testGetAllPersonNames() {
        List<String> result = personDao.getAllPersonNames();
        assertEquals(2, result.size());
        String first = result.get(0);
        assertEquals("Some Person", first);
    }

    @Test
    public void testFindAll() {
        List<Person> result = personDao.findAll();
        assertEquals(2, result.size());
        Person first = result.get(0);
        assertEquals("Some Person", first
                .getFullName());
    }

    @Test
    public void testFindByPrimaryKey() {
        Person result = personDao.findByPrimaryKey(
                "Sweden", "company1", "Some Person");

        assertEquals("Sweden", result.getCountry());
        assertEquals("company1", result.getCompany());
        assertEquals("Sweden, Company1, Some Person", result.getDescription());
        assertEquals("+46 555-123456", result.getPhone());
        assertEquals("Some Person", result.getFullName());
        assertEquals("Person", result.getLastName());
    }
}
