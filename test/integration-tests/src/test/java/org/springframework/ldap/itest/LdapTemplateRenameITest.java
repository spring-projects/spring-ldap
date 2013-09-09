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

package org.springframework.ldap.itest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Name;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Tests the rename methods of LdapTemplate.
 * 
 * We rely on that the bind, unbind and lookup methods work as they should -
 * that should be ok, since that is verified in a separate test class. *
 * 
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateRenameITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	private static String DN = "cn=Some Person6,ou=company1,c=Sweden";

	private static String NEWDN = "cn=Some Person6,ou=company2,c=Sweden";

	@Before
	public void prepareTestedInstance() throws Exception {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person6");
		adapter.setAttributeValue("sn", "Person6");
		adapter.setAttributeValue("description", "Some description");

		tested.bind(DN, adapter, null);
	}

	@After
	public void cleanup() throws Exception {
		tested.unbind(NEWDN);
		tested.unbind(DN);
	}

	@Test
	public void testRename() {
		tested.rename(DN, NEWDN);

		verifyDeleted(LdapUtils.newLdapName(DN));
		verifyBoundCorrectData();
	}

	@Test
	public void testRename_LdapName() throws Exception {
		Name oldDn = LdapUtils.newLdapName(DN);
		Name newDn = LdapUtils.newLdapName(NEWDN);
		tested.rename(oldDn, newDn);

		verifyDeleted(oldDn);
		verifyBoundCorrectData();
	}

	private void verifyDeleted(Name dn) {
		try {
			tested.lookup(dn);
			fail("Expected entry '" + dn + "' to be non-existent");
		}
		catch (NameNotFoundException expected) {
			// expected
		}
	}

	private void verifyBoundCorrectData() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup(NEWDN);
		assertEquals("Some Person6", result.getStringAttribute("cn"));
		assertEquals("Person6", result.getStringAttribute("sn"));
		assertEquals("Some description", result.getStringAttribute("description"));
	}
}
