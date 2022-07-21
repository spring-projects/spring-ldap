/*
 * Copyright 2005-2013 the original author or authors.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.Name;
import javax.naming.ldap.LdapName;

import static junit.framework.Assert.fail;

/**
 * Tests the recursive modification methods (unbind and the protected delete
 * methods) of LdapTemplate.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateRecursiveDeleteITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	private static LdapName DN = LdapUtils.newLdapName("cn=Some Person5,ou=company1,ou=Sweden");

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
		tested.bind(DN, adapter, null);

		firstSubDn = LdapUtils.newLdapName("cn=subPerson");
		firstSubDn = LdapUtils.prepend(firstSubDn, DN);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "subPerson");
		adapter.setAttributeValue("sn", "subPerson");
		adapter.setAttributeValue("description", "Should be recursively deleted");
		tested.bind(firstSubDn, adapter, null);
		secondSubDn = LdapUtils.newLdapName("cn=subPerson2");
		secondSubDn = LdapUtils.prepend(secondSubDn, DN);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "subPerson2");
		adapter.setAttributeValue("sn", "subPerson2");
		adapter.setAttributeValue("description", "Should be recursively deleted");
		tested.bind(secondSubDn, adapter, null);

		leafDn = LdapUtils.newLdapName("cn=subSubPerson");
		leafDn = LdapUtils.prepend(leafDn, DN);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "subSubPerson");
		adapter.setAttributeValue("sn", "subSubPerson");
		adapter.setAttributeValue("description", "Should be recursively deleted");
		tested.bind(leafDn, adapter, null);
	}

	@After
	public void cleanup() throws Exception {
		try {
			tested.unbind(DN, true);
		}
		catch (NameNotFoundException ignore) {
			// ignore
		}
	}

	@Test
	@Category(NoAdTest.class)
	public void testRecursiveUnbind() {
		tested.unbind(DN, true);

		verifyDeleted(DN);
		verifyDeleted(firstSubDn);
		verifyDeleted(secondSubDn);
		verifyDeleted(leafDn);
	}

	@Test
	@Category(NoAdTest.class)
	public void testRecursiveUnbindOnLeaf() {
		tested.unbind(leafDn, true);
		verifyDeleted(leafDn);
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
}
