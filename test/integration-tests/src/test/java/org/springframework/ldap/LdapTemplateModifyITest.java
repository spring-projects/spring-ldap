/*
 * Copyright 2005-2008 the original author or authors.
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests the modification methods (rebind and modifyAttributes) of LdapTemplate.
 * It also illustrates the use of DirContextAdapter as a means of getting
 * ModificationItems, in order to avoid doing a full rebind and use
 * modifyAttributes() instead. We rely on that the bind, unbind and lookup
 * methods work as they should - that should be ok, since that is verified in a
 * separate test class. NOTE: if any of the tests in this class fails, it may be
 * necessary to run the cleanup script as described in README.txt under
 * /src/iutest/.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateModifyITest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	private static String PERSON4_DN = "cn=Some Person4,ou=company1,c=Sweden";

	private static String PERSON5_DN = "cn=Some Person5,ou=company1,c=Sweden";

	@Before
	public void prepareTestedInstance() throws Exception {
		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person4");
		adapter.setAttributeValue("sn", "Person4");
		adapter.setAttributeValue("description", "Some description");

		tested.bind(PERSON4_DN, adapter, null);

		adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass", new String[] { "top", "person" });
		adapter.setAttributeValue("cn", "Some Person5");
		adapter.setAttributeValue("sn", "Person5");
		adapter.setAttributeValues("description", new String[] { "qwe", "123", "rty", "uio" });

		tested.bind(PERSON5_DN, adapter, null);

	}

	@After
	public void cleanup() throws Exception {
		tested.unbind(PERSON4_DN);
		tested.unbind(PERSON5_DN);
	}

	@Test
	public void testRebind_Attributes_Plain() {
		Attributes attributes = setupAttributes();

		tested.rebind(PERSON4_DN, null, attributes);

		verifyBoundCorrectData();
	}

	@Test
	public void testRebind_Attributes_DistinguishedName() {
		Attributes attributes = setupAttributes();

		tested.rebind(new DistinguishedName(PERSON4_DN), null, attributes);

		verifyBoundCorrectData();
	}

	@Test
	public void testModifyAttributes_MultiValueReplace() {
		BasicAttribute attr = new BasicAttribute("description", "Some other description");
		attr.add("Another description");
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);

		tested.modifyAttributes(PERSON4_DN, mods);

		DirContextAdapter result = (DirContextAdapter) tested.lookup(PERSON4_DN);
		String[] attributes = result.getStringAttributes("description");
		assertEquals(2, attributes.length);
		assertEquals("Some other description", attributes[0]);
		assertEquals("Another description", attributes[1]);
	}

	@Test
	public void testModifyAttributes_MultiValueAdd() {
		BasicAttribute attr = new BasicAttribute("description", "Some other description");
		attr.add("Another description");
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);

		tested.modifyAttributes(PERSON4_DN, mods);

		DirContextAdapter result = (DirContextAdapter) tested.lookup(PERSON4_DN);
		String[] attributes = result.getStringAttributes("description");
		assertEquals(3, attributes.length);
		assertEquals("Some description", attributes[0]);
		assertEquals("Some other description", attributes[1]);
		assertEquals("Another description", attributes[2]);
	}

	@Test
	public void testModifyAttributes_AddAttributeValueWithExistingValue() {
		DirContextOperations ctx = tested.lookupContext("cn=ROLE_USER,ou=groups");
		ctx.addAttributeValue("uniqueMember", "cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se");
		tested.modifyAttributes(ctx);
		assertTrue(true);
	}

	@Test
	public void testModifyAttributes_MultiValueAddDuplicateToUnordered() {
		BasicAttribute attr = new BasicAttribute("description", "Some description");
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);

		try {
			tested.modifyAttributes(PERSON4_DN, mods);
			fail("AttributeInUseException expected");
		}
		catch (AttributeInUseException expected) {
			// expected
		}
	}

	// LDAP-188
	@Test
	public void testModifyAttributes_AddAttributeValueAsDistinguishedName() {
		try {
			// prepare test data
			String dn = "cn=ROLE_USER,ou=groups";
			String lowerCasedName = "cn=Some Person,ou=company1,c=Sweden,dc=jayway,dc=se";
			String upperCasedName = "CN=Some Person,OU=company1,C=Sweden,DC=jayway,DC=se";
			DirContextOperations ctx = tested.lookupContext(dn);
			ctx.removeAttributeValue("uniqueMember", lowerCasedName);
			ctx.addAttributeValue("uniqueMember", upperCasedName);
			tested.modifyAttributes(ctx);

			// without this, the member added above will not be removed and the test fails
			System.setProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY, DistinguishedName.KEY_CASE_FOLD_NONE);
			
			ctx = tested.lookupContext(dn);
			ctx.removeAttributeValue("uniqueMember", new DistinguishedName(upperCasedName).toCompactString());
			tested.modifyAttributes(ctx);

			// verify
			DirContextAdapter result = (DirContextAdapter) tested.lookup(dn);
			String[] attributes = result.getStringAttributes("uniqueMember");
			assertEquals(4, attributes.length);
			assertEquals("0", "cn=Some Person2,ou=company1,c=Sweden,dc=jayway,dc=se", attributes[0]);
			assertEquals("1", "cn=Some Person,ou=company1,c=Norway,dc=jayway,dc=se", attributes[1]);
			assertEquals("2", "cn=Some Person,ou=company2,c=Sweden,dc=jayway,dc=se", attributes[2]);
			assertEquals("3", "cn=Some Person3,ou=company1,c=Sweden,dc=jayway,dc=se", attributes[3]);
		}
		finally {
			System.clearProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY);
		}
	}

	/**
	 * Test written originally to verify that duplicates are allowed on ordered
	 * attributes, but had to be changed since Apache DS seems to disallow
	 * duplicates even for ordered attributes.
	 */
	@Test
	public void testModifyAttributes_MultiValueAddDuplicateToOrdered() {
		BasicAttribute attr = new BasicAttribute("description", "Some other description", true); // ordered
		attr.add("Another description");
		// Commented out duplicate to make test work for Apache DS
		// attr.add("Some description");
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);

		tested.modifyAttributes(PERSON4_DN, mods);

		DirContextAdapter result = (DirContextAdapter) tested.lookup(PERSON4_DN);
		String[] attributes = result.getStringAttributes("description");
		assertEquals(3, attributes.length);
		assertEquals("Some description", attributes[0]);
		assertEquals("Some other description", attributes[1]);
		assertEquals("Another description", attributes[2]);
	}

	@Test
	public void testModifyAttributes_Plain() {
		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("description",
				"Some other description"));

		tested.modifyAttributes(PERSON4_DN, new ModificationItem[] { item });

		verifyBoundCorrectData();
	}

	@Test
	public void testModifyAttributes_DistinguishedName() {
		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("description",
				"Some other description"));

		tested.modifyAttributes(new DistinguishedName(PERSON4_DN), new ModificationItem[] { item });

		verifyBoundCorrectData();
	}

	@Test
	public void testModifyAttributes_DirContextAdapter_MultiAttributes() {
		DirContextAdapter adapter = (DirContextAdapter) tested.lookup(PERSON5_DN);
		adapter.setAttributeValues("description", new String[] { "qwe", "123", "klytt", "kalle" });

		tested.modifyAttributes(PERSON5_DN, adapter.getModificationItems());

		// Verify
		adapter = (DirContextAdapter) tested.lookup(PERSON5_DN);
		String[] attributes = adapter.getStringAttributes("description");
		assertEquals(4, attributes.length);
		assertEquals("qwe", attributes[0]);
		assertEquals("123", attributes[1]);
		assertEquals("klytt", attributes[2]);
		assertEquals("kalle", attributes[3]);
	}

	/**
	 * Demonstrates how the DirContextAdapter can be used to automatically keep
	 * track of changes of the attributes and deliver ModificationItems to use
	 * in moifyAttributes().
	 */
	@Test
	public void testModifyAttributes_DirContextAdapter() throws Exception {
		DirContextAdapter adapter = (DirContextAdapter) tested.lookup(PERSON4_DN);

		adapter.setAttributeValue("description", "Some other description");

		ModificationItem[] modificationItems = adapter.getModificationItems();
		tested.modifyAttributes(PERSON4_DN, modificationItems);

		verifyBoundCorrectData();
	}

	private Attributes setupAttributes() {
		Attributes attributes = new BasicAttributes();
		BasicAttribute ocattr = new BasicAttribute("objectclass");
		ocattr.add("top");
		ocattr.add("person");
		attributes.put(ocattr);
		attributes.put("cn", "Some Person4");
		attributes.put("sn", "Person4");
		attributes.put("description", "Some other description");
		return attributes;
	}

	private void verifyBoundCorrectData() {
		DirContextAdapter result = (DirContextAdapter) tested.lookup(PERSON4_DN);
		assertEquals("Some Person4", result.getStringAttribute("cn"));
		assertEquals("Person4", result.getStringAttribute("sn"));
		assertEquals("Some other description", result.getStringAttribute("description"));
	}
}
