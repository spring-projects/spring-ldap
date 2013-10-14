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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.odm.PersonWithDnAnnotations;
import org.springframework.ldap.itest.repositories.PersonWithDnAnnotationsRepository;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

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

        tested.save(person);

        assertEquals(6, tested.count());

        person = tested.findOne(LdapQueryBuilder.query().where("cn").is("New Person"));

        assertEquals("New Person", person.getCommonName());
        assertEquals("Person", person.getSurname());
        assertEquals("This is the description", person.getDesc().get(0));
        assertEquals("0123456", person.getTelephoneNumber());
    }
}
