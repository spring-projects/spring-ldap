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

import javax.naming.Name;
import javax.naming.ldap.LdapName;

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

import static junit.framework.Assert.fail;

/**
 * Tests the recursive modification methods (unbind and the protected delete methods) of
 * LdapTemplate.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateRecursiveDeleteITests extends AbstractLdapTemplateIntegrationTests {

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
		this.tested.bind(DN, adapter, null);

		this.firstSubDn = LdapUtils.newLdapName("cn=subPerson");
		this.firstSubDn = LdapUtils.prepend(this.firstSubDn, DN);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "subPerson");
		adapter.setAttributeValue("sn", "subPerson");
		adapter.setAttributeValue("description", "Should be recursively deleted");
		this.tested.bind(this.firstSubDn, adapter, null);
		this.secondSubDn = LdapUtils.newLdapName("cn=subPerson2");
		this.secondSubDn = LdapUtils.prepend(this.secondSubDn, DN);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "subPerson2");
		adapter.setAttributeValue("sn", "subPerson2");
		adapter.setAttributeValue("description", "Should be recursively deleted");
		this.tested.bind(this.secondSubDn, adapter, null);

		this.leafDn = LdapUtils.newLdapName("cn=subSubPerson");
		this.leafDn = LdapUtils.prepend(this.leafDn, DN);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "subSubPerson");
		adapter.setAttributeValue("sn", "subSubPerson");
		adapter.setAttributeValue("description", "Should be recursively deleted");
		this.tested.bind(this.leafDn, adapter, null);
	}

	@After
	public void cleanup() throws Exception {
		try {
			this.tested.unbind(DN, true);
		}
		catch (NameNotFoundException ignore) {
			// ignore
		}
	}

	@Test
	@Category(NoAdTests.class)
	public void testRecursiveUnbind() {
		this.tested.unbind(DN, true);

		verifyDeleted(DN);
		verifyDeleted(this.firstSubDn);
		verifyDeleted(this.secondSubDn);
		verifyDeleted(this.leafDn);
	}

	@Test
	@Category(NoAdTests.class)
	public void testRecursiveUnbindOnLeaf() {
		this.tested.unbind(this.leafDn, true);
		verifyDeleted(this.leafDn);
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

}
