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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.odm.PersonWithDnAnnotations;
import org.springframework.ldap.itest.repositories.PersonWithDnAnnotationsRepository;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Tests for Spring LDAP automatic repository scan functionality.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/repositoryScanTestContext.xml"})
public class RepositoryScanWithDnAnnotationsITest extends AbstractLdapTemplateIntegrationTest {
    @Autowired
    private PersonWithDnAnnotationsRepository tested;

    @Test
    public void verifyThatCreateWithNoIdSetWillAutomaticallyCalculateDn() {
        PersonWithDnAnnotations person = new PersonWithDnAnnotations();
        person.setCommonName("New Person");
        person.setSurname("Person");
        person.setDesc(Arrays.asList("This is the description"));
        person.setTelephoneNumber("0123456");
        person.setCountry("Sweden");
        person.setCompany("company1");

        assertThat(person.getDn()).isNull();

        tested.save(person);

        assertThat(person.getDn()).isNotNull();

        assertThat(tested.count()).isEqualTo(6);

        person = tested.findOne(query().where("cn").is("New Person"));

        assertThat(person.getCommonName()).isEqualTo("New Person");
        assertThat(person.getSurname()).isEqualTo("Person");
        assertThat(person.getDesc().get(0)).isEqualTo("This is the description");
        assertThat(person.getTelephoneNumber()).isEqualTo("0123456");
    }

    @Test
    public void verifyThatMovedEntryGetsUpdatedId() {
        PersonWithDnAnnotations found = tested.findOne(query().where("cn").is("Some Person3"));
        assertThat(found).isNotNull();

        assertThat(found.getDn()).isEqualTo(LdapUtils.newLdapName("cn=Some Person3,ou=company1,ou=Sweden"));

        found.setCompany("company2");

        tested.save(found);

        assertThat(found.getDn()).isEqualTo(LdapUtils.newLdapName("cn=Some Person3,ou=company2,ou=Sweden"));
    }
}
