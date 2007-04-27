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

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Tests the bind and unbind methods of LdapTemplate. The test methods in this
 * class tests a little too much, but we need to clean up after binding, so the
 * most efficient way to test is to do it all in one test method. Also, the
 * methods in this class relies on that the lookup method works as it should -
 * that should be ok, since that is verified in a separate test class. NOTE: if
 * any of the tests in this class fails, it may be necessary to run the cleanup
 * script as described in README.txt under /src/iutest/.
 * 
 * @author Mattias Arthursson
 */
public class LdapTemplateBindUnbindITest extends
        AbstractLdapTemplateIntegrationTest {
    private LdapTemplate tested;

    private static String DN = "cn=Some Person4,ou=company1,c=Sweden";

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    public void testBindAndUnbind_Attributes_Plain() {
        Attributes attributes = setupAttributes();
        tested.bind(DN, null, attributes);
        verifyBoundCorrectData();
        tested.unbind(DN);
        verifyCleanup();
    }

    public void testBindAndUnbind_Attributes_DIstinguishedName() {
        Attributes attributes = setupAttributes();
        tested.bind(new DistinguishedName(DN), null, attributes);
        verifyBoundCorrectData();
        tested.unbind(new DistinguishedName(DN));
        verifyCleanup();
    }

    public void testBindAndUnbind_DirContextAdapter_Plain() {
        DirContextAdapter adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", "Some Person4");
        adapter.setAttributeValue("sn", "Person4");

        tested.bind(DN, adapter, null);
        verifyBoundCorrectData();
        tested.unbind(DN);
        verifyCleanup();
    }

    public void testBindAndUnbind_DirContextAdapter_DIstinguishedName() {
        DirContextAdapter adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", "Some Person4");
        adapter.setAttributeValue("sn", "Person4");

        tested.bind(new DistinguishedName(DN), adapter, null);
        verifyBoundCorrectData();
        tested.unbind(new DistinguishedName(DN));
        verifyCleanup();
    }

    private Attributes setupAttributes() {
        Attributes attributes = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        ocattr.add("top");
        ocattr.add("person");
        attributes.put(ocattr);
        attributes.put("cn", "Some Person4");
        attributes.put("sn", "Person4");
        return attributes;
    }

    private void verifyBoundCorrectData() {
        DirContextAdapter result = (DirContextAdapter) tested.lookup(DN);
        assertEquals("Some Person4", result.getStringAttribute("cn"));
        assertEquals("Person4", result.getStringAttribute("sn"));
    }

    private void verifyCleanup() {
        try {
            tested.lookup(DN);
            fail("NameNotFoundException expected");
        } catch (NameNotFoundException expected) {
            assertTrue(true);
        }
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
