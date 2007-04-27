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

package org.springframework.ldap;

import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Tests the attributes mapper search method.
 * 
 * @author Mattias Arthursson
 */
public class LdapTemplateAttributesMapperITest extends
        AbstractLdapTemplateIntegrationTest {
    private LdapTemplate tested;

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    public void testSearch_AttributeMapper() throws Exception {
        AttributesMapper mapper = new PersonAttributesMapper();
        List result = tested.search("ou=company1,c=Sweden",
                "(&(objectclass=person)(sn=Person2))", mapper);

        assertEquals(1, result.size());
        Person person = (Person) result.get(0);
        assertEquals("Some Person2", person.getFullname());
        assertEquals("Person2", person.getLastname());
        assertEquals("Sweden, Company1, Some Person2", person.getDescription());
    }

    /**
     * Demonstrates how to retrieve all values of a multi-value attribute.
     * 
     * @see LdapTemplateContextMapperITest#testSearch_ContextMapper_MultiValue()
     */
    public void testSearch_AttributesMapper_MultiValue() throws Exception {
        AttributesMapper mapper = new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes) throws NamingException {
                LinkedList list = new LinkedList();
                NamingEnumeration enumeration = attributes.get("uniqueMember").getAll();
                while (enumeration.hasMoreElements()) {
                    String value = (String) enumeration.nextElement();
                    list.add(value);
                }
                String[] members = (String[]) list.toArray(new String[0]);
                return members;
            }
        };
        List result = tested.search("ou=groups",
                "(objectclass=groupOfUniqueNames)", mapper);

        assertEquals(2, result.size());
        assertEquals(1, ((String[]) result.get(0)).length);
        assertEquals(5, ((String[]) result.get(1)).length);
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
