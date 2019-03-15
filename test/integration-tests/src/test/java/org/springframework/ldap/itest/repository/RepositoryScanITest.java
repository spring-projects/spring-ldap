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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
        assertThat(tested.exists(PERSON3_DN)).isTrue();
    }

    @Test
    public void testFindOneWithDn() {
        Person person = tested.findOne(PERSON3_DN);

        assertThat(person).isNotNull();
        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }

    @Test
    public void verifyThatFindOneWithNonexistingDnReturnsNull() {
        Person person = tested.findOne(LdapUtils.newLdapName("cn=unknown"));
        assertThat(person).isNull();
    }

    @Test
    public void testFindOneWithQuery() {
        Person person = tested.findOne(query().where("cn").is("Some Person3"));

        assertThat(person).isNotNull();
        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }

    @Test
    public void testFindAll() {
        Iterable<Person> result = tested.findAll();
        assertThat(countIterable(result)).isEqualTo(5);

        for (Person person : result) {
            if(StringUtils.equals(person.getCommonName(), "Some Person3")) {
                assertThat(person.getCommonName()).isEqualTo("Some Person3");
                assertThat(person.getSurname()).isEqualTo("Person3");
                assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
                assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");

                // Done
                return;
            }
        }

        fail("Entry not found");
    }

    @Test
    public void testFindAllWithQuery() {
        Iterable<Person> persons = tested.findAll(query().where("cn").is("Some Person3"));

        assertThat(persons).isNotNull();
        Iterator<Person> iterator = persons.iterator();
        Person person = iterator.next();

        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");

        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void testFindAllWithIterable() {
        Iterable<Person> persons = tested.findAll(Arrays.asList(PERSON1_DN, PERSON3_DN));
        Iterator<Person> iterator = persons.iterator();
        Person person = iterator.next();

        assertThat(person.getCommonName()).isEqualTo("Some Person");
        assertThat(person.getSurname()).isEqualTo("Person");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123456");

        person = iterator.next();

        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");

        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void verifyThatFindAllWithIterableFiltersNotFoundEntries() {
        Iterable<Person> persons = tested.findAll(Arrays.asList(LdapUtils.newLdapName("cn=unknown"), PERSON3_DN));
        Iterator<Person> iterator = persons.iterator();
        Person person = iterator.next();

        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");

        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void testCount() {
        assertThat(tested.count()).isEqualTo(5);
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

        assertThat(tested.count()).isEqualTo(6);

        person = tested.findOne(dn);

        assertThat(person.getCommonName()).isEqualTo("New Person");
        assertThat(person.getSurname()).isEqualTo("Person");
        assertThat(person.getDesc().get(0)).isEqualTo("This is the description");
        assertThat(person.getTelephoneNumber()).isEqualTo("0123456");
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

        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("New Description");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }

    @Test
    public void testDelete() {
        Person person = tested.findOne(query().where("cn").is("Some Person3"));
        tested.delete(person);

        Person found = tested.findOne(query().where("cn").is("Some Person3"));
        assertThat(found).isNull();
    }

    @Test
    public void testDeleteWithIterable() {
        Iterable<Person> found = tested.findAll(Arrays.asList(PERSON1_DN, PERSON3_DN));
        tested.delete(found);

        assertThat(tested.count()).isEqualTo(3);
    }

    @Test
    public void testFindByLastName() {
        Iterable<Person> found = tested.findByLastName("Person3");
        assertThat(countIterable(found)).isEqualTo(1);
    }

    @Test
    public void testFindByUid() {
        Person person = tested.findByUid("some.person3");

        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }

    @Test
    public void testFindByPhoneNumber() {
        Person person = tested.findByTelephoneNumber("+46 555-123654");

        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }

}
