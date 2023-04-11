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
package org.springframework.ldap.samples.plain.dao;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.samples.plain.domain.Person;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Abstract base class for PersonDao integration tests.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ContextConfiguration("/config/testContext.xml")
public class PersonDaoSampleIntegrationTests extends
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
			assertThat(result.getDescription()).isEqualTo("Another description");
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
		List result = personDao.getAllPersonNames();
		assertThat(result).hasSize(2);
		String first = (String) result.get(0);
		assertThat(first).isEqualTo("Some Person");
	}

	@Test
	public void testFindAll() {
		List result = personDao.findAll();
		assertThat(result).hasSize(2);
		Person first = (Person) result.get(0);
		assertThat(first.getFullName()).isEqualTo("Some Person");
	}

	@Test
	public void testFindByPrimaryKey() {
		Person result = personDao.findByPrimaryKey(
				"Sweden", "company1", "Some Person");
		assertThat(result).isEqualTo(person);
	}
}
