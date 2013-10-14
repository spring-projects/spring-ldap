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
import org.springframework.ldap.itest.odm.Person;
import org.springframework.ldap.itest.repositories.PersonRepository;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Spring LDAP automatic repository scan functionality enabled with
 * {@link org.springframework.ldap.repository.config.EnableLdapRepositories}.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/repositoryScanAnnotationTestContext.xml"})
public class RepositoryScanAnnotationConfiguredITest extends AbstractLdapTemplateIntegrationTest {
    private static final Name PERSON3_DN = LdapUtils.newLdapName("cn=Some Person3, ou=Company1, c=Sweden");

    @Autowired
    private PersonRepository tested;

    @Test
    public void testExists() {
        assertTrue(tested.exists(PERSON3_DN));
    }

    @Test
    public void testFindOneWithDn() {
        Person person = tested.findOne(PERSON3_DN);

        assertNotNull(person);
        assertEquals("Some Person3", person.getCommonName());
        assertEquals("Person3", person.getSurname());
        assertEquals("Sweden, Company1, Some Person3", person.getDesc().get(0));
        assertEquals("+46 555-123654", person.getTelephoneNumber());
    }
}
