/*
 * Copyright 2005-2023 the original author or authors.
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
import javax.naming.ldap.LdapName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests {@code LdapClient}'s recursive modification methods (unbind and the protected delete
 * methods).
 * 
 * @author Josh Cummings
 */
@ContextConfiguration(locations = {"/conf/ldapClientTestContext.xml"})
public class DefaultLdapClientRecursiveDeleteITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapClient tested;

	private static final LdapName DN = LdapUtils.newLdapName("cn=Some Person5,ou=company1,ou=Sweden");

	private LdapName firstSubDn;

	private LdapName secondSubDn;

	private LdapName leafDn;

	@Before
	public void prepareTestedInstance() throws Exception {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person5");
		adapter.setAttributeValue("sn", "Person5");
		adapter.setAttributeValue("description", "Some description");
		tested.bind(DN).object(adapter).execute();

		firstSubDn = LdapUtils.newLdapName("cn=subPerson");
		firstSubDn = LdapUtils.prepend(firstSubDn, DN);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "subPerson");
		adapter.setAttributeValue("sn", "subPerson");
		adapter.setAttributeValue("description", "Should be recursively deleted");
		tested.bind(firstSubDn).object(adapter).execute();
		secondSubDn = LdapUtils.newLdapName("cn=subPerson2");
		secondSubDn = LdapUtils.prepend(secondSubDn, DN);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "subPerson2");
		adapter.setAttributeValue("sn", "subPerson2");
		adapter.setAttributeValue("description", "Should be recursively deleted");
		tested.bind(secondSubDn).object(adapter).execute();

		leafDn = LdapUtils.newLdapName("cn=subSubPerson");
		leafDn = LdapUtils.prepend(leafDn, DN);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "subSubPerson");
		adapter.setAttributeValue("sn", "subSubPerson");
		adapter.setAttributeValue("description", "Should be recursively deleted");
		tested.bind(leafDn).object(adapter).execute();
	}

	@After
	public void cleanup() throws Exception {
		try {
			tested.unbind(DN).recursive(true).execute();
		}
		catch (NameNotFoundException ignore) {
			// ignore
		}
	}

	@Test
	@Category(NoAdTest.class)
	public void testRecursiveUnbind() {
		tested.unbind(DN).recursive(true).execute();

		verifyDeleted(DN);
		verifyDeleted(firstSubDn);
		verifyDeleted(secondSubDn);
		verifyDeleted(leafDn);
	}

	@Test
	@Category(NoAdTest.class)
	public void testRecursiveUnbindOnLeaf() {
		tested.unbind(leafDn).recursive(true).execute();
		verifyDeleted(leafDn);
	}

	private void verifyDeleted(Name dn) {
		assertThatExceptionOfType(NameNotFoundException.class)
				.describedAs("Expected entry '" + dn + "' to be non-existent")
				.isThrownBy(() -> tested.list(dn).toList(NameClassPair::getName));
	}
}
