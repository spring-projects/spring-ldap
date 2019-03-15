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

package org.springframework.ldap.itest.odm;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.repository.support.QueryDslLdapQuery;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateQueryDslLdapQueryITest extends AbstractLdapTemplateIntegrationTest {
    @Autowired
    private LdapTemplate tested;
    private QPerson qperson;
    private QueryDslLdapQuery<Person> query;

    @Before
    public void prepareTestedInstance() {
        qperson = QPerson.person;
        query = new QueryDslLdapQuery<Person>(tested, qperson);
    }

    @Test
    public void testUniqueResult() {
        Person person = query.where(qperson.commonName.eq("Some Person3")).uniqueResult();

        assertThat(person).isNotNull();
        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }

    @Test
    public void testList() {
        List<Person> persons = query.where(qperson.commonName.eq("Some Person3")).list();

        Person person = persons.get(0);
        assertThat(person.getCommonName()).isEqualTo("Some Person3");
        assertThat(person.getSurname()).isEqualTo("Person3");
        assertThat(person.getDesc().get(0)).isEqualTo("Sweden, Company1, Some Person3");
        assertThat(person.getTelephoneNumber()).isEqualTo("+46 555-123654");
    }
}
