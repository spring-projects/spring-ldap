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
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTests;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateOdmWithDnAnnotationsITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapTemplate tested;

	@Test
	public void testFindOne() {
		PersonWithDnAnnotations person = tested.findOne(query().where("cn").is("Some Person3"),
				PersonWithDnAnnotations.class);

		assertThat(person).isNotNull();
		assertThat(person.getCommonName()).isEqualTo("Some Person3");
		assertThat(person.getSurname()).isEqualTo("Person3");
		assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
		assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");

		// Automatically calculated
		assertThat(person.getCompany()).isEqualTo("company1");
		assertThat(person.getCountry()).isEqualTo("Sweden");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	@Test
	public void testFindByDn() {
		PersonWithDnAnnotations person = tested.findByDn(LdapUtils.newLdapName("cn=Some Person3,ou=company1,ou=Sweden"),
				PersonWithDnAnnotations.class);

		assertThat(person).isNotNull();
		assertThat(person.getCommonName()).isEqualTo("Some Person3");
		assertThat(person.getSurname()).isEqualTo("Person3");
		assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
		assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");

		// Automatically calculated
		assertThat(person.getCompany()).isEqualTo("company1");
		assertThat(person.getCountry()).isEqualTo("Sweden");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	@Test
	public void testFindInCountry() {
		List<PersonWithDnAnnotations> persons = tested.find(query().base("ou=Sweden").where("cn").isPresent(),
				PersonWithDnAnnotations.class);

		assertThat(persons).hasSize(4);

		PersonWithDnAnnotations person = findPerson(persons, "Some Person3");

		// Automatically calculated
		assertThat(person.getCompany()).isEqualTo("company1");
		assertThat(person.getCountry()).isEqualTo("Sweden");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	@Test
	public void testFindForStreamInCountry() {
		List<PersonWithDnAnnotations> persons = tested
				.findForStream(query().base("ou=Sweden").where("cn").isPresent(), PersonWithDnAnnotations.class)
				.collect(Collectors.toList());

		assertThat(persons).hasSize(4);

		PersonWithDnAnnotations person = findPerson(persons, "Some Person3");

		// Automatically calculated
		assertThat(person.getCompany()).isEqualTo("company1");
		assertThat(person.getCountry()).isEqualTo("Sweden");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	private PersonWithDnAnnotations findPerson(List<PersonWithDnAnnotations> persons, String cn) {
		for (PersonWithDnAnnotations person : persons) {
			if (person.getCommonName().equals(cn)) {
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

		assertThat(tested.findAll(PersonWithDnAnnotations.class)).hasSize(6);

		person = tested.findByDn(LdapUtils.newLdapName("cn=New Person,ou=company1,ou=Sweden"),
				PersonWithDnAnnotations.class);

		assertThat(person.getCommonName()).isEqualTo("New Person");
		assertThat(person.getSurname()).isEqualTo("Person");
		assertThat(person.getDesc().get(0)).isEqualTo("This is the description");
		assertThat(person.getTelephoneNumber()).isEqualTo("0123456");

		// Automatically calculated
		assertThat(person.getCompany()).isEqualTo("company1");
		assertThat(person.getCountry()).isEqualTo("Sweden");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
	}

	@Test
	public void testUpdate() {
		PersonWithDnAnnotations person = tested.findOne(query().where("cn").is("Some Person3"),
				PersonWithDnAnnotations.class);

		person.setDesc(Arrays.asList("New Description"));
		String entryUuid = person.getEntryUuid();
		assertThat(entryUuid).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
		tested.update(person);

		person = tested.findByDn(LdapUtils.newLdapName("cn=Some Person3, ou=company1, ou=Sweden"),
				PersonWithDnAnnotations.class);

		assertThat(person.getCommonName()).isEqualTo("Some Person3");
		assertThat(person.getSurname()).isEqualTo("Person3");
		assertThat(person.getDesc().get(0)).isEqualTo("New Description");
		assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
		assertThat(person.getEntryUuid()).isEqualTo(entryUuid);
	}

	@Test
	public void testUpdateWithChangedDn() {
		PersonWithDnAnnotations person = tested.findOne(query().where("cn").is("Some Person3"),
				PersonWithDnAnnotations.class);

		// This should make the entry move
		person.setCountry("Norway");
		String entryUuid = person.getEntryUuid();
		assertThat(entryUuid).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
		tested.update(person);

		person = tested.findByDn(LdapUtils.newLdapName("cn=Some Person3, ou=company1, ou=Norway"),
				PersonWithDnAnnotations.class);

		assertThat(person.getCommonName()).isEqualTo("Some Person3");
		assertThat(person.getSurname()).isEqualTo("Person3");
		assertThat(person.getCountry()).isEqualTo("Norway");
		assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
		assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
		assertThat(person.getEntryUuid()).describedAs("The operational attribute 'entryUUID' was not set").isNotEmpty();
		assertThat(person.getEntryUuid()).isNotEqualTo(entryUuid);
	}

}
