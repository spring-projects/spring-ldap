/*
 * Copyright 2006-present the original author or authors.
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
import javax.naming.NameClassPair;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.LdapDataEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests {@link LdapClient}'s rename methods.
 *
 * @author Josh Cummings
 */
@ContextConfiguration(locations = { "/conf/ldapClientTestContext.xml" })
public class DefaultLdapClientRenameITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapClient tested;

	private static final String DN = "cn=Some Person6,ou=company1,ou=Sweden";

	private static final String NEWDN = "cn=Some Person6,ou=company2,ou=Sweden";

	@Before
	public void prepareTestedInstance() throws Exception {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person6");
		adapter.setAttributeValue("sn", "Person6");
		adapter.setAttributeValue("description", "Some description");

		this.tested.bind(DN).object(adapter).execute();
	}

	@After
	public void cleanup() throws Exception {
		this.tested.unbind(NEWDN).execute();
		this.tested.unbind(DN).execute();
	}

	@Test
	public void testRename() {
		this.tested.modify(DN).name(NEWDN).execute();
		verifyDeleted(LdapUtils.newLdapName(DN));
		verifyBoundCorrectData();
	}

	@Test
	public void testRename_LdapName() {
		Name oldDn = LdapUtils.newLdapName(DN);
		Name newDn = LdapUtils.newLdapName(NEWDN);
		this.tested.modify(oldDn).name(newDn).execute();
		verifyDeleted(oldDn);
		verifyBoundCorrectData();
	}

	private void verifyDeleted(Name dn) {
		try {
			this.tested.list(dn).toList(NameClassPair::getName);
			fail("Expected entry '" + dn + "' to be non-existent");
		}
		catch (NameNotFoundException expected) {
			// expected
		}
	}

	private void verifyBoundCorrectData() {
		LdapDataEntry result = this.tested.search().name(NEWDN).toEntry();
		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person6");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person6");
		assertThat(result.getStringAttribute("description")).isEqualTo("Some description");
	}

}
