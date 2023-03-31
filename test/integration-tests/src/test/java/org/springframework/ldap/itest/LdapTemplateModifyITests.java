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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.AttributeInUseException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests the modification methods (rebind and modifyAttributes) of LdapTemplate. It also
 * illustrates the use of DirContextAdapter as a means of getting ModificationItems, in
 * order to avoid doing a full rebind and use modifyAttributes() instead. We rely on that
 * the bind, unbind and lookup methods work as they should - that should be ok, since that
 * is verified in a separate test class. NOTE: if any of the tests in this class fails, it
 * may be necessary to run the cleanup script as described in README.txt under
 * /src/iutest/.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateModifyITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapTemplate tested;

	private static String PERSON4_DN = "cn=Some Person4,ou=company1,ou=Sweden";

	private static String PERSON5_DN = "cn=Some Person5,ou=company1,ou=Sweden";

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
	public void testRebind_Attributes_LdapName() {
		Attributes attributes = setupAttributes();

		tested.rebind(LdapUtils.newLdapName(PERSON4_DN), null, attributes);

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
		List<String> attributes = Arrays.asList(result.getStringAttributes("description"));
		assertThat(attributes).hasSize(2);
		assertThat(attributes.contains("Some other description")).isTrue();
		assertThat(attributes.contains("Another description")).isTrue();
	}

	@Test
	public void testModifyAttributes_MultiValueAdd() {
		BasicAttribute attr = new BasicAttribute("description", "Some other description");
		attr.add("Another description");
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);

		tested.modifyAttributes(PERSON4_DN, mods);

		DirContextAdapter result = (DirContextAdapter) tested.lookup(PERSON4_DN);
		List<String> attributes = Arrays.asList(result.getStringAttributes("description"));
		assertThat(attributes).hasSize(3);
		assertThat(attributes.contains("Some other description")).isTrue();
		assertThat(attributes.contains("Another description")).isTrue();
		assertThat(attributes.contains("Some description")).isTrue();
	}

	@Test
	public void testModifyAttributes_AddAttributeValueWithExistingValue() {
		DirContextOperations ctx = tested.lookupContext("cn=ROLE_USER,ou=groups");
		ctx.addAttributeValue("uniqueMember", "cn=Some Person,ou=company1,ou=Norway," + base);
		tested.modifyAttributes(ctx);
		assertThat(true).isTrue();
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

	/**
	 * Test written originally to verify that duplicates are allowed on ordered
	 * attributes, but had to be changed since Apache DS seems to disallow duplicates even
	 * for ordered attributes.
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
		List<String> attributes = Arrays.asList(result.getStringAttributes("description"));
		assertThat(attributes).hasSize(3);
		assertThat(attributes.contains("Some other description")).isTrue();
		assertThat(attributes.contains("Another description")).isTrue();
		assertThat(attributes.contains("Some description")).isTrue();
	}

	@Test
	public void testModifyAttributes_Plain() {
		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("description", "Some other description"));

		tested.modifyAttributes(PERSON4_DN, new ModificationItem[] { item });

		verifyBoundCorrectData();
	}

	@Test
	public void testModifyAttributes_LdapName() {
		ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("description", "Some other description"));

		tested.modifyAttributes(LdapUtils.newLdapName(PERSON4_DN), new ModificationItem[] { item });

		verifyBoundCorrectData();
	}

	@Test
	public void testModifyAttributes_DirContextAdapter_MultiAttributes() {
		DirContextAdapter adapter = (DirContextAdapter) tested.lookup(PERSON5_DN);
		adapter.setAttributeValues("description", new String[] { "qwe", "123", "klytt", "kalle" });

		tested.modifyAttributes(PERSON5_DN, adapter.getModificationItems());

		// Verify
		adapter = (DirContextAdapter) tested.lookup(PERSON5_DN);
		List<String> attributes = Arrays.asList(adapter.getStringAttributes("description"));
		assertThat(attributes).hasSize(4);
		assertThat(attributes.contains("qwe")).isTrue();
		assertThat(attributes.contains("123")).isTrue();
		assertThat(attributes.contains("klytt")).isTrue();
		assertThat(attributes.contains("kalle")).isTrue();
	}

	/**
	 * Demonstrates how the DirContextAdapter can be used to automatically keep track of
	 * changes of the attributes and deliver ModificationItems to use in
	 * moifyAttributes().
	 */
	@Test
	public void testModifyAttributes_DirContextAdapter() throws Exception {
		DirContextAdapter adapter = (DirContextAdapter) tested.lookup(PERSON4_DN);

		adapter.setAttributeValue("description", "Some other description");

		ModificationItem[] modificationItems = adapter.getModificationItems();
		tested.modifyAttributes(PERSON4_DN, modificationItems);

		verifyBoundCorrectData();
	}

	@Test
	public void verifyCompleteReplacementOfUniqueMemberAttribute_Ldap119Workaround() {
		DirContextOperations ctx = tested.lookupContext("cn=ROLE_USER,ou=groups");
		ctx.setAttributeValues("uniqueMember", new String[] { "cn=Some Person,ou=company1,ou=Norway," + base }, true);
		ctx.getModificationItems();

		tested.modifyAttributes(ctx);
	}

	/**
	 * This test originally failed on ApacheDS complaining that the uniqueMember attribute
	 * was emptied.
	 */
	@Test
	public void verifyCompleteReplacementOfUniqueMemberAttribute_Ldap119() {
		DirContextOperations ctx = tested.lookupContext("cn=ROLE_USER,ou=groups");
		ctx.setAttributeValues("uniqueMember", new String[] { "cn=Some Person,ou=company1,ou=Norway," + base });
		ctx.getModificationItems();

		tested.modifyAttributes(ctx);
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
		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person4");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person4");
		assertThat(result.getStringAttribute("description")).isEqualTo("Some other description");
	}

}
