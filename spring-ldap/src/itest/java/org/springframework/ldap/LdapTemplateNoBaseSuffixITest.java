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

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;

/**
 * Tests to verify that not setting a base suffix on the ContextSource (as
 * defined in ldapTemplateNoBaseSuffixTestContext.xml) works as expected.
 * 
 * NOTE: This test will not work under Java 1.4.1 or earlier.
 * 
 * @author Mattias Arthursson
 */
public class LdapTemplateNoBaseSuffixITest extends
        AbstractLdapTemplateIntegrationTest {
    private LdapTemplate tested;

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateNoBaseSuffixTestContext.xml" };
    }

    /**
     * This method depends on a DirObjectFactory ({@link org.springframework.ldap.core.support.DefaultDirObjectFactory})
     * being set in the ContextSource.
     */
    public void testLookup_Plain() {
        DirContextAdapter result = (DirContextAdapter) tested
                .lookup("cn=Some Person2, ou=company1, c=Sweden, dc=jayway, dc=se");

        assertEquals("Some Person2", result.getStringAttribute("cn"));
        assertEquals("Person2", result.getStringAttribute("sn"));
        assertEquals("Sweden, Company1, Some Person2", result
                .getStringAttribute("description"));
        assertEquals(
                "cn=Some Person2, ou=company1, c=Sweden, dc=jayway, dc=se",
                result.getDn().toString());
        assertEquals(
                "cn=Some Person2, ou=company1, c=Sweden, dc=jayway, dc=se",
                result.getNameInNamespace());
    }

    public void testSearch_Plain() {
        CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();

        tested.search("dc=jayway, dc=se", "(objectclass=person)", handler);
        assertEquals(5, handler.getNoOfRows());
    }

    public void testBindAndUnbind_Plain() {
        DirContextAdapter adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", "Some Person4");
        adapter.setAttributeValue("sn", "Person4");
        tested.bind("cn=Some Person4, ou=company1, c=Sweden, dc=jayway, dc=se",
                adapter, null);

        DirContextAdapter result = (DirContextAdapter) tested
                .lookup("cn=Some Person4, ou=company1, c=Sweden, dc=jayway, dc=se");

        assertEquals("Some Person4", result.getStringAttribute("cn"));
        assertEquals("Person4", result.getStringAttribute("sn"));
        assertEquals(
                "cn=Some Person4, ou=company1, c=Sweden, dc=jayway, dc=se",
                result.getDn().toString());

        tested
                .unbind("cn=Some Person4, ou=company1, c=Sweden, dc=jayway, dc=se");
        try {
            tested
                    .lookup("cn=Some Person4, ou=company1, c=Sweden, dc=jayway, dc=se");
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
