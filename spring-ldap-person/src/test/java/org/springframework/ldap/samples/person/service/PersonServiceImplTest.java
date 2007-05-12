/*
 * Copyright 2005-2007 the original author or authors.
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
package org.springframework.ldap.samples.person.service;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.samples.person.dao.GroupDao;
import org.springframework.ldap.samples.person.dao.PersonDao;
import org.springframework.ldap.samples.person.domain.Person;
import org.springframework.ldap.samples.person.domain.SearchCriteria;

/**
 * Unit tests for the PersonServiceImpl class.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class PersonServiceImplTest extends TestCase {

    private MockControl personDaoControl;

    private PersonDao personDaoMock;

    private PersonServiceImpl tested;

    protected void setUp() throws Exception {
        super.setUp();
        personDaoControl = MockControl.createControl(PersonDao.class);
        personDaoMock = (PersonDao) personDaoControl.getMock();

        tested = new PersonServiceImpl();
        tested.setPersonDao(personDaoMock);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        personDaoControl = null;
        personDaoMock = null;

        tested = null;
    }

    protected void replay() {
        personDaoControl.replay();
    }

    protected void verify() {
        personDaoControl.verify();
    }

    public void testCreate() {
        Person person = setupPerson();

        personDaoMock.create(person);

        replay();

        tested.create("Sweden", "Some company", "Some Person", "Person",
                new String[] { "Some description" });

        verify();
    }

    private Person setupPerson() {
        Person person = new Person();
        person.setCountry("Sweden");
        person.setCompany("Some company");
        person.setFullName("Some Person");
        person.setLastName("Person");
        person.setDescription(new String[] { "Some description" });
        return person;
    }

    public void testUpdate() {
        Person person = setupPerson();

        personDaoMock.update(person);

        replay();

        tested.update(person);

        verify();
    }

    public void testDelete() {
        Person person = setupPerson();

        personDaoMock.update(person);

        replay();

        tested.update(person);

        verify();
    }

    public void testFindByPrimaryKey() {
        Person person = new Person();

        personDaoControl.expectAndReturn(personDaoMock.findByPrimaryKeyData(
                "Sweden", "Some company", "Some Person"), person);

        replay();

        Person result = tested.findByPrimaryKey("Sweden", "Some company",
                "Some Person");

        verify();

        assertSame(person, result);
    }

    public void testFindAll() {
        List expected = Collections.singletonList(null);

        personDaoControl.expectAndReturn(personDaoMock.findAll(), expected);

        replay();

        List actual = tested.findAll();

        verify();

        assertSame(expected, actual);
    }

    public void testFind() {
        List expected = Collections.singletonList(null);

        SearchCriteria criteria = new SearchCriteria();
        criteria.setName("some");
        personDaoControl
                .expectAndReturn(personDaoMock.find(criteria), expected);

        replay();

        List actual = tested.find(criteria);

        verify();

        assertSame(expected, actual);
    }
}
