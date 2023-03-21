/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ldap.itest.odm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.odm.core.OdmException;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateOdmWithNoDnAnnotationsITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	@Test
	public void testFindOne() {
		Person person = tested.findOne(query().where("cn").is("Some Person3"), Person.class);

		assertThat(person).isNotNull();
		assertThat(person.getCommonName()).isEqualTo("Some Person3");
		assertThat(person.getSurname()).isEqualTo("Person3");
		assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
		assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	@Test
	public void testFindByDn() {
		Person person = tested.findByDn(LdapUtils.newLdapName("cn=Some Person3,ou=company1,ou=Sweden"), Person.class);

		assertThat(person).isNotNull();
		assertThat(person.getCommonName()).isEqualTo("Some Person3");
		assertThat(person.getSurname()).isEqualTo("Person3");
		assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
		assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	@Test(expected = OdmException.class)
	public void testFindByDnThrowsExceptionOnInvalidEntry() {
		tested.findByDn(LdapUtils.newLdapName("ou=company1,ou=Sweden"), Person.class);
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void testFindOneThrowsEmptyResultIfNotFound() {
		tested.findOne(query().where("cn").is("This cn does not exist"), Person.class);
	}

	@Test
	public void testFind() {
		List<Person> persons = tested.find(query().where("cn").is("Some Person3"), Person.class);

		assertThat(persons).hasSize(1);
		Person person = persons.get(0);

		assertThat(person).isNotNull();
		assertThat(person.getCommonName()).isEqualTo("Some Person3");
		assertThat(person.getSurname()).isEqualTo("Person3");
		assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
		assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	@Test
	public void testFindForStream() {
		List<Person> persons = tested.findForStream(query().where("cn").is("Some Person3"), Person.class)
				.collect(Collectors.toList());

		assertThat(persons).hasSize(1);
		Person person = persons.get(0);

		assertThat(person).isNotNull();
		assertThat(person.getCommonName()).isEqualTo("Some Person3");
		assertThat(person.getSurname()).isEqualTo("Person3");
		assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
		assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	@Test
	public void testFindInCountry() {
		List<Person> persons = tested.find(query().base("ou=Sweden").where("cn").isPresent(), Person.class);

		assertThat(persons).hasSize(4);
		Person person = persons.get(0);

		assertThat(person).isNotNull();
	}

	@Test
	public void testFindForStreamInCountry() {
		List<Person> persons = tested.findForStream(query().base("ou=Sweden").where("cn").isPresent(), Person.class)
				.collect(Collectors.toList());

		assertThat(persons).hasSize(4);
		Person person = persons.get(0);

		assertThat(person).isNotNull();
	}

	@Test
	public void testFindAll() {
		List<Person> result = tested.findAll(Person.class);
		assertThat(result).hasSize(5);
	}

	@Test
	public void testCreate() {
		Person person = new Person();
		person.setDn(LdapNameBuilder.newInstance("ou=company1,ou=Sweden").add("cn", "New Person").build());
		person.setCommonName("New Person");
		person.setSurname("Person");
		person.setDesc(Arrays.asList("This is the description"));
		person.setTelephoneNumber("0123456");

		tested.create(person);

		assertThat(tested.findAll(Person.class)).hasSize(6);

		person = tested.findOne(query().where("cn").is("New Person"), Person.class);

		assertThat(person.getCommonName()).isEqualTo("New Person");
		assertThat(person.getSurname()).isEqualTo("Person");
		assertThat(person.getDesc().get(0)).isEqualTo("This is the description");
		assertThat(person.getTelephoneNumber()).isEqualTo("0123456");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	@Test
	public void testUpdate() {
		Person person = tested.findOne(query().where("cn").is("Some Person3"), Person.class);

		person.setDesc(Arrays.asList("New Description"));
		String entryUuid = person.getEntryUuid();
		assertThat(entryUuid).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
		tested.update(person);

		person = tested.findOne(query().where("cn").is("Some Person3"), Person.class);

		assertThat(person.getCommonName()).isEqualTo("Some Person3");
		assertThat(person.getSurname()).isEqualTo("Person3");
		assertThat(person.getDesc().get(0)).isEqualTo("New Description");
		assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
		assertThat(person.getEntryUuid()).isEqualTo(entryUuid);
	}

	@Test
	public void testDelete() {
		Person person = tested.findOne(query().where("cn").is("Some Person3"), Person.class);

		tested.delete(person);

		try {
			tested.findOne(query().where("cn").is("Some Person3"), Person.class);
			fail("EmptyResultDataAccessException e");
		}
		catch (EmptyResultDataAccessException e) {
			assertThat(true).isTrue();
		}
	}

	/**
	 * Test case for Jira LDAP-271.
	 */
	@Test
	public void testLdap271() {
		Person person = tested.findOne(query().where("cn").is("Some Person3"), Person.class);

		// Perform test
		person.setTelephoneNumber(null);
		tested.update(person);

		person = tested.findOne(query().where("cn").is("Some Person3"), Person.class);
		assertThat(person.getTelephoneNumber()).as("TelephoneNumber should be null").isNull();
	}

}
