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

import javax.naming.Name;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Tests the recursive modification methods (unbind and the protected delete
 * methods) of LdapTemplate.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class LdapTemplateRecursiveDeleteITest extends
        AbstractLdapTemplateIntegrationTest {
    private LdapTemplate tested;

    private static DistinguishedName DN = new DistinguishedName(
            "cn=Some Person5,ou=company1,c=Sweden");

    private DistinguishedName firstSubDn;

    private DistinguishedName secondSubDn;

    private DistinguishedName leafDn;

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        DirContextAdapter adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", "Some Person5");
        adapter.setAttributeValue("sn", "Person5");
        adapter.setAttributeValue("description", "Some description");
        tested.bind(DN, adapter, null);

        firstSubDn = new DistinguishedName("cn=subPerson");
        firstSubDn.prepend(DN);
        adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", "subPerson");
        adapter.setAttributeValue("sn", "subPerson");
        adapter.setAttributeValue("description",
                "Should be recursively deleted");
        tested.bind(firstSubDn, adapter, null);
        secondSubDn = new DistinguishedName("cn=subPerson2");
        secondSubDn.prepend(DN);
        adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", "subPerson2");
        adapter.setAttributeValue("sn", "subPerson2");
        adapter.setAttributeValue("description",
                "Should be recursively deleted");
        tested.bind(secondSubDn, adapter, null);

        leafDn = new DistinguishedName("cn=subSubPerson");
        leafDn.prepend(firstSubDn);
        adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", "subSubPerson");
        adapter.setAttributeValue("sn", "subSubPerson");
        adapter.setAttributeValue("description",
                "Should be recursively deleted");
        tested.bind(leafDn, adapter, null);
    }

    protected void onTearDown() throws Exception {
        try {
            tested.unbind(DN, true);
        } catch (NameNotFoundException ignore) {
            // ignore
        }
    }

    public void testRecursiveUnbind() {
        tested.unbind(DN, true);

        verifyDeleted(DN);
        verifyDeleted(firstSubDn);
        verifyDeleted(secondSubDn);
        verifyDeleted(leafDn);
    }

    public void testRecursiveUnbindOnLeaf() {
        tested.unbind(leafDn, true);
        verifyDeleted(leafDn);
    }

    private void verifyDeleted(Name dn) {
        try {
            tested.lookup(dn);
            fail("Expected entry '" + dn + "' to be non-existent");
        } catch (NameNotFoundException expected) {
            // expected
        }
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
