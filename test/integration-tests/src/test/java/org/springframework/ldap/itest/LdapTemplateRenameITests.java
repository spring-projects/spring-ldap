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

package org.springframework.ldap.itest;

import javax.naming.Name;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests the rename methods of LdapTemplate.
 *
 * We rely on that the bind, unbind and lookup methods work as they should - that should
 * be ok, since that is verified in a separate test class. *
 *
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateRenameITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapTemplate tested;

	private static String DN = "cn=Some Person6,ou=company1,ou=Sweden";

	private static String NEWDN = "cn=Some Person6,ou=company2,ou=Sweden";

	@Before
	public void prepareTestedInstance() throws Exception {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person6");
		adapter.setAttributeValue("sn", "Person6");
		adapter.setAttributeValue("description", "Some description");

		this.tested.bind(DN, adapter, null);
	}

	@After
	public void cleanup() throws Exception {
		this.tested.unbind(NEWDN);
		this.tested.unbind(DN);
	}

	@Test
	public void testRename() {
		this.tested.rename(DN, NEWDN);

		verifyDeleted(LdapUtils.newLdapName(DN));
		verifyBoundCorrectData();
	}

	@Test
	public void testRename_LdapName() throws Exception {
		Name oldDn = LdapUtils.newLdapName(DN);
		Name newDn = LdapUtils.newLdapName(NEWDN);
		this.tested.rename(oldDn, newDn);

		verifyDeleted(oldDn);
		verifyBoundCorrectData();
	}

	private void verifyDeleted(Name dn) {
		try {
			this.tested.lookup(dn);
			fail("Expected entry '" + dn + "' to be non-existent");
		}
		catch (NameNotFoundException expected) {
			// expected
		}
	}

	private void verifyBoundCorrectData() {
		DirContextAdapter result = (DirContextAdapter) this.tested.lookup(NEWDN);
		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person6");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person6");
		assertThat(result.getStringAttribute("description")).isEqualTo("Some description");
	}

}
