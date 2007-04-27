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
 * Tests the rename methods of LdapTemplate.
 * 
 * We rely on that the bind, unbind and lookup methods work as they should -
 * that should be ok, since that is verified in a separate test class. *
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplateRenameITest extends
        AbstractLdapTemplateIntegrationTest {
    private LdapTemplate tested;

    private static String DN = "cn=Some Person6,ou=company1,c=Sweden";

    private static String NEWDN = "cn=Some Person6,ou=company2,c=Sweden";

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        DirContextAdapter adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", "Some Person6");
        adapter.setAttributeValue("sn", "Person6");
        adapter.setAttributeValue("description", "Some description");

        tested.bind(DN, adapter, null);
    }

    protected void onTearDown() throws Exception {
        tested.unbind(NEWDN);
        tested.unbind(DN);
    }

    public void testRename() {
        tested.rename(DN, NEWDN);

        verifyDeleted(new DistinguishedName(DN));
        verifyBoundCorrectData();
    }

    public void testRename_DistinguishedName() throws Exception {
        Name oldDn = new DistinguishedName(DN);
        Name newDn = new DistinguishedName(NEWDN);
        tested.rename(oldDn, newDn);
        

        verifyDeleted(oldDn);
        verifyBoundCorrectData();
    }

    private void verifyDeleted(Name dn) {
        try {
            tested.lookup(dn);
            fail("Expected entry '" + dn + "' to be non-existent");
        } catch (NameNotFoundException expected) {
            // expected
        }
    }

    private void verifyBoundCorrectData() {
        DirContextAdapter result = (DirContextAdapter) tested.lookup(NEWDN);
        assertEquals("Some Person6", result.getStringAttribute("cn"));
        assertEquals("Person6", result.getStringAttribute("sn"));
        assertEquals("Some description", result
                .getStringAttribute("description"));
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
