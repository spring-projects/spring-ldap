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

package org.springframework.ldap.core;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;
import java.util.Iterator;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests the DirContextAdapter class.
 *
 * @author Andreas Ronge
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class DirContextAdapterTest {

	private static final LdapName BASE_NAME = LdapUtils.newLdapName("dc=jayway,dc=se");

	private static final LdapName DUMMY_NAME = LdapUtils.newLdapName("c=SE,dc=jayway,dc=se");

	private DirContextAdapter tested;

	@Before
	public void setUp() throws Exception {
		tested = new DirContextAdapter();
	}

	@Test
	public void testSetUpdateMode() throws Exception {
		assertThat(tested.isUpdateMode()).isFalse();
		tested.setUpdateMode(true);
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setUpdateMode(false);
		assertThat(tested.isUpdateMode()).isFalse();
	}

	@Test
	public void testGetModificationItems() throws Exception {
		ModificationItem[] items = tested.getModificationItems();
		assertThat(items.length).isEqualTo(0);
		tested.setUpdateMode(true);
		assertThat(items.length).isEqualTo(0);
	}

	@Test
	public void testAlwaysReplace() throws Exception {
		ModificationItem[] items = tested.getModificationItems();
		assertThat(items.length).isEqualTo(0);
		tested.setUpdateMode(true);
		assertThat(items.length).isEqualTo(0);
	}

	@Test
	public void testGetStringAttributeWhenAttributeDoesNotExist() throws Exception {
		String s = tested.getStringAttribute("does not exist");
		assertThat(s).isNull();
	}

	@Test
	public void testGetStringAttributeWhenAttributeDoesExistButWithNoValue() throws Exception {
		final Attributes attrs = new BasicAttributes();
		attrs.put(new BasicAttribute("abc"));
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(attrs, null);
			}

		}
		tested = new TestableDirContextAdapter();
		String s = tested.getStringAttribute("abc");
		assertThat(s).isNull();
	}

	@Test
	public void testAttributeExistsWhenAttributeDoesExistButWithNoValue() throws Exception {
		final Attributes attrs = new BasicAttributes();
		attrs.put(new BasicAttribute("abc"));
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(attrs, null);
			}

		}
		tested = new TestableDirContextAdapter();
		boolean result = tested.attributeExists("abc");
		assertThat(result).isEqualTo(true);
	}

	@Test
	public void testAttributeExistsWhenAttributeDoesNotExist() throws Exception {
		boolean result = tested.attributeExists("does not exist");
		assertThat(result).isEqualTo(false);
	}

	@Test
	public void testGetStringAttributeWhenAttributeExists() throws Exception {
		final Attributes attrs = new BasicAttributes();
		attrs.put(new BasicAttribute("abc", "def"));
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(attrs, null);
			}

		}
		tested = new TestableDirContextAdapter();
		String s = tested.getStringAttribute("abc");
		assertThat(s).isEqualTo("def");
	}

	@Test
	public void testGetStringAttributesWhenMultiValueAttributeExists() throws Exception {
		final Attributes attrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("234");
		attrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(attrs, null);
			}

		}
		tested = new TestableDirContextAdapter();
		String s[] = tested.getStringAttributes("abc");
		assertThat(s[0]).isEqualTo("123");
		assertThat(s[1]).isEqualTo("234");
		assertThat(s.length).isEqualTo(2);
	}

	@Test
	public void testGetStringAttributesExistsWithInvalidType() throws Exception {
		final Attributes attrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add(new Object());
		attrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(attrs, null);
			}

		}
		tested = new TestableDirContextAdapter();
		try {
			tested.getStringAttributes("abc");
			fail("ClassCastException expected");
		}
		catch (IllegalArgumentException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testGetStringAttributesExistsEmpty() throws Exception {
		final Attributes attrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		attrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(attrs, null);
			}

		}
		tested = new TestableDirContextAdapter();
		String s[] = tested.getStringAttributes("abc");
		assertThat(s).isNotNull();
		assertThat(s.length).isEqualTo(0);
	}

	@Test
	public void testGetStringAttributesNotExists() throws Exception {
		String s[] = tested.getStringAttributes("abc");
		assertThat(s).isNull();
	}

	@Test
	public void testGetAttributesSortedStringSetExists() throws Exception {
		final Attributes attrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("234");
		attrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(attrs, null);
			}

		}
		tested = new TestableDirContextAdapter();
		SortedSet s = tested.getAttributeSortedStringSet("abc");
		assertThat(s).isNotNull();
		assertThat(s).hasSize(2);
		Iterator it = s.iterator();
		assertThat(it.next()).isEqualTo("123");
		assertThat(it.next()).isEqualTo("234");
	}

	@Test
	public void testGetAttributesSortedStringSetNotExists() throws Exception {
		final Attributes attrs = new BasicAttributes();
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(attrs, null);
			}

		}
		tested = new TestableDirContextAdapter();
		SortedSet s = tested.getAttributeSortedStringSet("abc");
		assertThat(s).isNull();
	}

	@Test
	public void testAddAttributeValue() throws NamingException {
		// Perform test
		tested.addAttributeValue("abc", "123");

		Attributes attrs = tested.getAttributes();
		Attribute attr = attrs.get("abc");
		assertThat((String) attr.get()).isEqualTo("123");
	}

	@Test
	public void testAddAttributeValueAttributeWithOtherValueExists() throws NamingException {
		tested.setAttribute(new BasicAttribute("abc", "321"));

		// Perform test
		tested.addAttributeValue("abc", "123");

		Attributes attrs = tested.getAttributes();
		Attribute attr = attrs.get("abc");
		assertThat((String) attr.get(0)).isEqualTo("321");
		assertThat((String) attr.get(1)).isEqualTo("123");
	}

	@Test
	public void testAddAttributeValueAttributeWithSameValueExists() throws NamingException {
		tested.setAttribute(new BasicAttribute("abc", "123"));

		// Perform test
		tested.addAttributeValue("abc", "123");

		Attributes attrs = tested.getAttributes();
		Attribute attr = attrs.get("abc");
		assertThat(attr.size()).isEqualTo(1);
		assertThat((String) attr.get(0)).isEqualTo("123");
	}

	@Test
	public void testAddAttributeValueInUpdateMode() throws NamingException {
		tested.setUpdateMode(true);
		tested.addAttributeValue("abc", "123");

		// Perform test
		Attributes attrs = tested.getAttributes();
		assertThat(attrs.get("abc")).isNull();

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		Attribute attribute = modificationItems[0].getAttribute();
		assertThat(attribute.getID()).isEqualTo("abc");
		assertThat(attribute.get()).isEqualTo("123");
	}

	@Test
	public void testAddAttributeValueInUpdateModeAttributeWhenOtherValueExistsInOrigAttrs() throws NamingException {

		tested.setAttribute(new BasicAttribute("abc", "321"));
		tested.setUpdateMode(true);

		// Perform test
		tested.addAttributeValue("abc", "123");

		Attributes attrs = tested.getAttributes();
		assertThat(attrs.get("abc")).isNotNull();

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		Attribute attribute = modificationItems[0].getAttribute();
		assertThat(attribute.size()).isEqualTo(1);
		assertThat(attribute.getID()).isEqualTo("abc");
		assertThat(attribute.get()).isEqualTo("123");
	}

	@Test
	public void testGetModificationItemsOnAddAttributeValueInUpdateModeAttributeWhenSameValueExistsInOrigAttrs()
			throws NamingException {

		tested.setAttribute(new BasicAttribute("abc", "123"));
		tested.setUpdateMode(true);

		// Perform test
		tested.addAttributeValue("abc", "123");

		Attributes attrs = tested.getAttributes();
		assertThat(attrs.get("abc")).isNotNull();

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(0);
	}

	@Test
	public void testAddAttributeValueInUpdateModeAttributeWithOtherValueExistsInUpdAttrs() throws NamingException {
		tested.setUpdateMode(true);
		tested.setAttributeValue("abc", "321");

		// Perform test
		tested.addAttributeValue("abc", "123");

		Attributes attrs = tested.getAttributes();
		assertThat(attrs.get("abc")).isNull();

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		Attribute attribute = modificationItems[0].getAttribute();
		assertThat(attribute.getID()).isEqualTo("abc");
		assertThat(attribute.get(0)).isEqualTo("321");
		assertThat(attribute.get(1)).isEqualTo("123");
	}

	@Test
	public void testAddAttributeValueInUpdateModeAttributeWithSameValueExistsInUpdAttrs() throws NamingException {
		tested.setUpdateMode(true);
		tested.setAttributeValue("abc", "123");

		// Perform test
		tested.addAttributeValue("abc", "123");

		Attributes attrs = tested.getAttributes();
		assertThat(attrs.get("abc")).isNull();

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		Attribute attribute = modificationItems[0].getAttribute();
		assertThat(attribute.size()).isEqualTo(1);
		assertThat(attribute.getID()).isEqualTo("abc");
		assertThat(attribute.get()).isEqualTo("123");
	}

	@Test
	public void testNewLdapNameWithString() throws NamingException {
		tested.addAttributeValue("member", LdapUtils.newLdapName("CN=test,DC=root"));
		tested.addAttributeValue("member2", LdapUtils.newLdapName("CN=test2,DC=root"));

		Attributes attrs = tested.getAttributes();
		assertThat(attrs.get("member").get()).isEqualTo(LdapUtils.newLdapName("CN=test,DC=root"));
		assertThat(attrs.get("member2").get()).isEqualTo(LdapUtils.newLdapName("CN=test2,DC=root"));
	}

	@Test
	public void testNewLdapNameWithLdapName() throws NamingException {
		tested.addAttributeValue("member", "CN=test,DC=root");
		tested.addAttributeValue("member2", LdapUtils.newLdapName("CN=test2,DC=root"));

		Attributes attrs = tested.getAttributes();
		assertThat(attrs.get("member").get()).isEqualTo("CN=test,DC=root");
		assertThat(attrs.get("member2").get()).isEqualTo(LdapUtils.newLdapName("CN=test2,DC=root"));
	}

	@Test
	public void testRemoveAttributeValueAttributeDoesntExist() {
		// Perform test
		tested.removeAttributeValue("abc", "123");

		Attributes attributes = tested.getAttributes();
		assertThat(attributes.get("abc")).isNull();
	}

	@Test
	public void testRemoveAttributeValueAttributeWithOtherValueExists() throws NamingException {
		tested.setAttribute(new BasicAttribute("abc", "321"));

		// Perform test
		tested.removeAttributeValue("abc", "123");

		Attributes attributes = tested.getAttributes();
		Attribute attr = attributes.get("abc");
		assertThat(attr).isNotNull();
		assertThat(attr.size()).isEqualTo(1);
		assertThat(attr.get()).isEqualTo("321");
	}

	@Test
	public void testRemoveAttributeValueAttributeWithSameValueExists() {
		tested.setAttribute(new BasicAttribute("abc", "123"));

		// Perform test
		tested.removeAttributeValue("abc", "123");

		Attributes attributes = tested.getAttributes();
		Attribute attr = attributes.get("abc");
		assertThat(attr).isNull();
	}

	@Test
	public void testRemoveAttributeValueAttributeWithOtherAndSameValueExists() throws NamingException {
		BasicAttribute basicAttribute = new BasicAttribute("abc");
		basicAttribute.add("123");
		basicAttribute.add("321");
		tested.setAttribute(basicAttribute);

		// Perform test
		tested.removeAttributeValue("abc", "123");

		Attributes attributes = tested.getAttributes();
		Attribute attr = attributes.get("abc");
		assertThat(attr).isNotNull();
		assertThat(attr.size()).isEqualTo(1);
		assertThat(attr.get()).isEqualTo("321");
	}

	@Test
	public void testRemoveAttributeValueInUpdateMode() {
		tested.setUpdateMode(true);

		// Perform test
		tested.removeAttributeValue("abc", "123");

		assertThat(tested.getAttributes().get("abc")).isNull();

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(0);
	}

	@Test
	public void testRemoveAttributeValueInUpdateModeSameValueExistsInUpdatedAttrs() {
		tested.setUpdateMode(true);
		tested.setAttributeValue("abc", "123");

		// Perform test
		tested.removeAttributeValue("abc", "123");

		assertThat(tested.getAttributes().get("abc")).isNull();

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(0);
	}

	@Test
	public void testRemoveAttributeValueInUpdateModeOtherValueExistsInUpdatedAttrs() throws NamingException {
		tested.setUpdateMode(true);
		tested.setAttributeValue("abc", "321");

		// Perform test
		tested.removeAttributeValue("abc", "123");

		assertThat(tested.getAttributes().get("abc")).isNull();

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		Attribute modificationAttribute = modificationItems[0].getAttribute();
		assertThat(modificationAttribute.getID()).isEqualTo("abc");
		assertThat(modificationAttribute.size()).isEqualTo(1);
		assertThat(modificationAttribute.get()).isEqualTo("321");
	}

	@Test
	public void testRemoveAttributeValueInUpdateModeOtherAndSameValueExistsInUpdatedAttrs() throws NamingException {
		tested.setUpdateMode(true);
		tested.setAttributeValues("abc", new String[] { "321", "123" });

		// Perform test
		tested.removeAttributeValue("abc", "123");

		assertThat(tested.getAttributes().get("abc")).isNull();

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		Attribute modificationAttribute = modificationItems[0].getAttribute();
		assertThat(modificationAttribute.getID()).isEqualTo("abc");
		assertThat(modificationAttribute.size()).isEqualTo(1);
	}

	@Test
	public void testRemoveAttributeValueInUpdateModeSameValueExistsInOrigAttrs() {
		tested.setAttribute(new BasicAttribute("abc", "123"));
		tested.setUpdateMode(true);

		// Perform test
		tested.removeAttributeValue("abc", "123");

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		Attribute modificationAttribute = modificationItems[0].getAttribute();
		assertThat(modificationAttribute.getID()).isEqualTo("abc");
		assertThat(modificationAttribute.size()).isEqualTo(0);
		assertThat(modificationItems[0].getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
	}

	@Test
	public void testRemoveAttributeValueInUpdateModeSameAndOtherValueExistsInOrigAttrs() throws NamingException {
		BasicAttribute basicAttribute = new BasicAttribute("abc");
		basicAttribute.add("123");
		basicAttribute.add("321");
		tested.setAttribute(basicAttribute);
		tested.setUpdateMode(true);

		// Perform test
		tested.removeAttributeValue("abc", "123");

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		Attribute modificationAttribute = modificationItems[0].getAttribute();
		assertThat(modificationAttribute.getID()).isEqualTo("abc");
		assertThat(modificationAttribute.size()).isEqualTo(1);
		assertThat(modificationAttribute.get()).isEqualTo("123");
		assertThat(modificationItems[0].getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
	}

	@Test
	public void testSetStringAttribute() throws Exception {
		assertThat(tested.isUpdateMode()).isFalse();
		tested.setAttributeValue("abc", "123");
		Attributes attrs = tested.getAttributes();
		Attribute attr = attrs.get("abc");
		assertThat((String) attr.get()).isEqualTo("123");
	}

	@Test
	public void testSetStringAttributeNull() throws Exception {
		assertThat(tested.isUpdateMode()).isFalse();
		tested.setAttributeValue("abc", null);
		Attributes attrs = tested.getAttributes();
		Attribute attr = attrs.get("abc");
		assertThat(attr).isNull();
	}

	@Test
	public void testAddAttribute() throws Exception {
		tested.setUpdateMode(true);
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValue("abc", "123");
		Attributes attrs = tested.getAttributes();
		Attribute attr = attrs.get("abc");
		assertThat(attr).isNull();

		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
		assertThat(mods[0].getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		attr = mods[0].getAttribute();
		assertThat((String) attr.get()).isEqualTo("123");

		String[] modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(1);
		assertThat(modNames[0]).isEqualTo("abc");

		tested.update();
		mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(0);
		modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(0);
		attrs = tested.getAttributes();
		attr = attrs.get("abc");
		assertThat((String) attr.get()).isEqualTo("123");
	}

	// LDAP-304
	@Test
	public void testModifyNull() throws Exception {
		tested.setAttributeValue("memberDN", null);
		tested.setUpdateMode(true);
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValue("memberDN", new LdapName("ou=test"));

		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
	}

	@Test
	public void testGetDn() throws Exception {
		DirContextAdapter tested = new DirContextAdapter(DUMMY_NAME);
		Name result = tested.getDn();
		assertThat(result).isEqualTo(DUMMY_NAME);
	}

	@Test
	public void testGetDn_BasePath() {
		DirContextAdapter tested = new DirContextAdapter(null, DUMMY_NAME, BASE_NAME);
		Name result = tested.getDn();
		assertThat(result).isEqualTo(DUMMY_NAME);
	}

	@Test
	public void testGetNameInNamespace() {
		DirContextAdapter tested = new DirContextAdapter(DUMMY_NAME);
		String result = tested.getNameInNamespace();
		assertThat(result).isEqualTo(DUMMY_NAME.toString());
	}

	@Test
	public void testGetNameInNamespace_BasePath() {
		DirContextAdapter tested = new DirContextAdapter(null, LdapUtils.newLdapName("c=SE"), BASE_NAME);
		String result = tested.getNameInNamespace();
		assertThat(result).isEqualTo(DUMMY_NAME.toString());
	}

	@Test
	public void testAddMultiAttributes() throws Exception {
		tested.setUpdateMode(true);
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("abc", new String[] { "123", "456" });
		Attributes attrs = tested.getAttributes();
		Attribute attr = attrs.get("abc");
		assertThat(attr).isNull();
		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
		assertThat(mods[0].getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		attr = mods[0].getAttribute();
		assertThat(attr.size()).isEqualTo(2);
		assertThat((String) attr.get(0)).isEqualTo("123");
		assertThat((String) attr.get(1)).isEqualTo("456");

		String[] modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(1);
		assertThat(modNames[0]).isEqualTo("abc");

		tested.update();
		mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(0);
		modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(0);
		attrs = tested.getAttributes();
		attr = attrs.get("abc");
		assertThat((String) attr.get(0)).isEqualTo("123");
		assertThat((String) attr.get(1)).isEqualTo("456");
	}

	@Test
	public void testRemoveAttribute() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		fixtureAttrs.put(new BasicAttribute("abc", "123"));
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();

		tested.setUpdateMode(true);
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValue("abc", null);
		Attributes attrs = tested.getAttributes();
		Attribute attr = attrs.get("abc");
		assertThat((String) attr.get()).isEqualTo("123");

		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
		assertThat(mods[0].getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		attr = mods[0].getAttribute();
		assertThat((String) attr.getID()).isEqualTo("abc");
		String[] modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(1);
		assertThat(modNames[0]).isEqualTo("abc");

		tested.update();
		mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(0);
		modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(0);
		attrs = tested.getAttributes();
		attr = attrs.get("abc");
		assertThat(attr).isNull();
	}

	@Test
	public void testRemoveMultiAttribute() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute abc = new BasicAttribute("abc");
		abc.add("123");
		abc.add("456");
		fixtureAttrs.put(abc);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();

		tested.setUpdateMode(true);
		tested.setAttributeValues("abc", new String[] {});

		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
		assertThat(mods[0].getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		Attribute attr = mods[0].getAttribute();
		assertThat((String) attr.getID()).isEqualTo("abc");
		assertThat(attr.size()).isEqualTo(0);
	}

	@Test
	public void testChangeAttribute() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		fixtureAttrs.put(new BasicAttribute("abc", "123"));
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		tested.setAttributeValue("abc", "234"); // change

		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
		assertThat(mods[0].getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
		Attribute attr = mods[0].getAttribute();
		assertThat((String) attr.getID()).isEqualTo("abc");
		assertThat((String) attr.get()).isEqualTo("234");
	}

	@Test
	public void testNoChangeAttribute() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		fixtureAttrs.put(new BasicAttribute("abc", "123"));
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValue("abc", "123"); // change

		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(0);
	}

	@Test
	public void testNoChangeMultiAttribute() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("abc", new String[] { "123", "qwe" });

		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(0);
		String[] modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(0);
	}

	@Test
	public void testNoChangeMultiAttributeOrderDoesNotMatter() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		tested.setAttributeValues("abc", new String[] { "qwe", "123" });

		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(0);
		String[] modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(0);
	}

	@Test
	public void testChangeMultiAttributeOrderDoesMatter() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("abc", new String[] { "qwe", "123" }, true);

		// change
		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
		assertThat(mods[0].getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
		Attribute attr = mods[0].getAttribute();
		assertThat(attr.get(0)).isEqualTo("qwe");
		assertThat(attr.get(1)).isEqualTo("123");
	}

	/**
	 * Test case corresponding to LDAP-96 in Spring Jira.
	 * https://jira.springframework.org/browse/LDAP-96
	 */
	@Test
	public void testChangeMultiAttributeOrderDoesMatterLDAP96() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("title");
		multi.add("Juergen");
		multi.add("George");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("title", new String[] { "Jim", "George", "Juergen" }, true);

		// change
		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
		assertThat(mods[0].getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
		Attribute attr = mods[0].getAttribute();
		assertThat(attr.get(0)).isEqualTo("Jim");
		assertThat(attr.get(1)).isEqualTo("George");
		assertThat(attr.get(2)).isEqualTo("Juergen");
	}

	@Test
	public void testChangeMultiAttribute_AddValue() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("abc", new String[] { "123", "qwe", "klytt" });

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		assertThat(modificationItems[0].getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		assertThat(modificationItems[0].getAttribute().get()).isEqualTo("klytt");
	}

	@Test
	public void testChangeMultiAttribute_RemoveValue() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("abc", new String[] { "123" });

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		assertThat(modificationItems[0].getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		assertThat(modificationItems[0].getAttribute().get()).isEqualTo("qwe");
	}

	@Test
	public void testChangeMultiAttribute_RemoveTwoValues() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		multi.add("rty");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("abc", new String[] { "123" });

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		assertThat(modificationItems[0].getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		assertThat(modificationItems[0].getAttribute().get(0)).isEqualTo("qwe");
		assertThat(modificationItems[0].getAttribute().get(1)).isEqualTo("rty");
	}

	@Test
	public void testChangeMultiAttribute_RemoveAllValues() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("abc", null);

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		assertThat(modificationItems[0].getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
	}

	@Test
	public void testChangeMultiAttribute_SameValue() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("abc", new String[] { "123", "qwe" });

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(0);
	}

	@Test
	public void testChangeMultiAttribute_AddAndRemoveValue() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		multi.add("rty");
		multi.add("uio");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("abc", new String[] { "123", "qwe", "klytt", "kalle" });

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(2);

		Attribute modifiedAttribute = modificationItems[0].getAttribute();
		assertThat(modificationItems[0].getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		assertThat(modifiedAttribute.getID()).isEqualTo("abc");
		assertThat(modifiedAttribute.size()).isEqualTo(2);
		assertThat(modifiedAttribute.get(0)).isEqualTo("rty");
		assertThat(modifiedAttribute.get(1)).isEqualTo("uio");

		assertThat(modificationItems[1].getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		modifiedAttribute = modificationItems[1].getAttribute();
		assertThat(modifiedAttribute.getID()).isEqualTo("abc");
		assertThat(modifiedAttribute.size()).isEqualTo(2);
		assertThat(modifiedAttribute.get(0)).isEqualTo("klytt");
		assertThat(modifiedAttribute.get(1)).isEqualTo("kalle");
	}

	@Test
	public void testAddAttribute_Multivalue() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		Attribute multi = new BasicAttribute("abc");
		multi.add("123");
		multi.add("qwe");
		fixtureAttrs.put(multi);
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValues("def", new String[] { "kalle", "klytt" });

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);
		assertThat(modificationItems[0].getAttribute().getID()).isEqualTo("def");
	}

	@Test
	public void testChangeAttributeTwice() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		fixtureAttrs.put(new BasicAttribute("abc", "123"));
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValue("abc", "234"); // change
		tested.setAttributeValue("abc", "987");
		// change a second time
		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
		assertThat(mods[0].getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);
		Attribute attr = mods[0].getAttribute();
		assertThat((String) attr.getID()).isEqualTo("abc");
		assertThat((String) attr.get()).isEqualTo("987");

		tested.update();
		mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(0);
		String[] modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(0);
		Attributes attrs = tested.getAttributes();
		attr = attrs.get("abc");
		assertThat((String) attr.get()).isEqualTo("987");
		assertThat(tested.getStringAttribute("abc")).isEqualTo("987");
	}

	@Test
	public void testAddReplaceAndChangeAttribute() throws Exception {
		final Attributes fixtureAttrs = new BasicAttributes();
		fixtureAttrs.put(new BasicAttribute("abc", "123"));
		fixtureAttrs.put(new BasicAttribute("qwe", "42"));
		class TestableDirContextAdapter extends DirContextAdapter {

			public TestableDirContextAdapter() {
				super(fixtureAttrs, null);
				setUpdateMode(true);
			}

		}
		tested = new TestableDirContextAdapter();
		assertThat(tested.isUpdateMode()).isTrue();
		tested.setAttributeValue("abc", "234"); // change
		tested.setAttributeValue("qwe", null); // remove
		tested.setAttributeValue("zzz", "new"); // new
		Attributes attrs = tested.getAttributes();
		Attribute attr = attrs.get("abc");
		assertThat((String) attr.get()).isEqualTo("123");
		assertThat(attrs.size()).isEqualTo(2);

		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(3);
		String[] modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(3);

		ModificationItem mod = getModificationItem(mods, DirContext.REPLACE_ATTRIBUTE);
		assertThat(mod).isNotNull();
		attr = mod.getAttribute();
		assertThat((String) attr.getID()).isEqualTo("abc");
		assertThat((String) attr.get()).isEqualTo("234");

		mod = getModificationItem(mods, DirContext.REMOVE_ATTRIBUTE);
		assertThat(mod).isNotNull();
		attr = mod.getAttribute();
		assertThat((String) attr.getID()).isEqualTo("qwe");

		mod = getModificationItem(mods, DirContext.ADD_ATTRIBUTE);
		assertThat(mod).isNotNull();
		attr = mod.getAttribute();
		assertThat((String) attr.getID()).isEqualTo("zzz");
		assertThat((String) attr.get()).isEqualTo("new");

		tested.update();
		mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(0);
		modNames = tested.getNamesOfModifiedAttributes();
		assertThat(modNames.length).isEqualTo(0);

		attrs = tested.getAttributes();
		assertThat(attrs.size()).isEqualTo(2);
		attr = attrs.get("abc");
		assertThat((String) attr.get()).isEqualTo("234");
		assertThat(tested.getStringAttribute("zzz")).isEqualTo("new");
	}

	/**
	 * Test for LDAP-15: DirContextAdapter.setAttribute(). Verifies that setting an
	 * Attribute should modify updatedAttrs if in update mode.
	 * @throws NamingException
	 */
	@Test
	public void testSetAttribute_UpdateMode() throws NamingException {
		// Set original attribute value
		Attribute attribute = new BasicAttribute("cn", "john doe");
		tested.setAttribute(attribute);

		// Set to update mode
		tested.setUpdateMode(true);

		// Perform test - update the attribute
		Attribute updatedAttribute = new BasicAttribute("cn", "nisse hult");
		tested.setAttribute(updatedAttribute);

		// Verify result
		ModificationItem[] mods = tested.getModificationItems();
		assertThat(mods.length).isEqualTo(1);
		assertThat(mods[0].getModificationOp()).isEqualTo(DirContext.REPLACE_ATTRIBUTE);

		Attribute modificationAttribute = mods[0].getAttribute();
		assertThat(modificationAttribute.getID()).isEqualTo("cn");
		assertThat(modificationAttribute.get()).isEqualTo("nisse hult");
	}

	@Test
	public void testGetStringAttributes_NullValue() {
		String result = tested.getStringAttribute("someAbsentAttribute");
		assertThat(result).isNull();
	}

	@Test
	public void testGetStringAttributes_AttributeExists_NullValue() {
		tested.setAttribute(new BasicAttribute("someAttribute", null));
		String result = tested.getStringAttribute("someAttribute");
		assertThat(result).isNull();
	}

	private ModificationItem getModificationItem(ModificationItem[] mods, int operation) {
		for (int i = 0; i < mods.length; i++) {
			if (mods[i].getModificationOp() == operation)
				return mods[i];
		}
		return null;
	}

	@Test
	public void testModifyMultiValueAttributeModificationOrder() throws NamingException {
		BasicAttribute attribute = new BasicAttribute("abc");
		attribute.add("Some Person");
		attribute.add("Some Other Person");

		tested.setAttribute(attribute);
		tested.setUpdateMode(true);

		tested.setAttributeValues("abc", new String[] { "some person", "Some Other Person" });

		// Perform test
		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(2);
		ModificationItem modificationItem = modificationItems[0];
		assertThat(modificationItem.getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		assertThat(modificationItem.getAttribute().get()).isEqualTo("Some Person");
		modificationItem = modificationItems[1];
		assertThat(modificationItem.getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		assertThat(modificationItem.getAttribute().get()).isEqualTo("some person");
	}

	/**
	 * Test for LDAP-13.
	 */
	@Test
	public void testModifyAttributeByteArray() {
		tested.setAttribute(new BasicAttribute("abc", new byte[] { 1, 2, 3 }));

		tested.setUpdateMode(true);

		// Perform test
		tested.setAttributeValue("abc", new byte[] { 1, 2, 3 });

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(0);
	}

	/**
	 * Test for LDAP-109, since also DirContextAdapter may get an invalid CompositeName
	 * sent to it.
	 */
	@Test
	public void testConstructorUsingCompositeNameWithBackslashes() throws Exception {
		CompositeName compositeName = new CompositeName();
		compositeName.add("cn=Some\\\\Person6,ou=company1,c=Sweden");
		DirContextAdapter adapter = new DirContextAdapter(compositeName);
		assertThat(adapter.getDn().toString()).isEqualTo("cn=Some\\\\Person6,ou=company1,c=Sweden");
	}

	@Test
	public void testStringConstructor() {
		DirContextAdapter tested = new DirContextAdapter("cn=john doe, ou=company");
		assertThat(tested.getDn()).isEqualTo(LdapUtils.newLdapName("cn=john doe, ou=company"));
	}

	@Test
	public void testAddDnAttributeValueIdentical() {
		BasicAttributes attributes = new BasicAttributes();
		attributes.put("uniqueMember", "cn=john doe, ou=company");

		DirContextAdapter tested = new DirContextAdapter(attributes,
				LdapUtils.newLdapName("cn=administrators, ou=groups"));
		tested.setUpdateMode(true);

		tested.addAttributeValue("uniqueMember", LdapUtils.newLdapName("cn=john doe, ou=company"));
		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(0);
	}

	@Test
	public void testAddDnAttributeSyntacticallyEqual() {
		BasicAttributes attributes = new BasicAttributes();
		attributes.put("uniqueMember", "cn=john doe,OU=company");

		DirContextAdapter tested = new DirContextAdapter(attributes,
				LdapUtils.newLdapName("cn=administrators, ou=groups"));
		tested.setUpdateMode(true);

		tested.addAttributeValue("uniqueMember", LdapUtils.newLdapName("cn=john doe, ou=company"));
		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(0);
	}

	@Test
	public void testRemoveDnAttributeSyntacticallyEqual() throws NamingException {
		BasicAttributes attributes = new BasicAttributes();
		attributes.put("uniqueMember", "cn=john doe,OU=company");

		DirContextAdapter tested = new DirContextAdapter(attributes,
				LdapUtils.newLdapName("cn=administrators, ou=groups"));
		tested.setUpdateMode(true);

		tested.removeAttributeValue("uniqueMember", LdapUtils.newLdapName("cn=john doe, ou=company"));
		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);

		ModificationItem modificationItem = modificationItems[0];
		assertThat(modificationItem.getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		assertThat(modificationItem.getAttribute().getID()).isEqualTo("uniqueMember");
	}

	@Test
	public void testRemoveOneOfSeveralDnAttributeSyntacticallyEqual() throws NamingException {
		BasicAttributes attributes = new BasicAttributes();
		BasicAttribute attribute = new BasicAttribute("uniqueMember", "cn=john doe,OU=company");
		attribute.add("cn=jane doe, ou=company");
		attributes.put(attribute);

		DirContextAdapter tested = new DirContextAdapter(attributes,
				LdapUtils.newLdapName("cn=administrators, ou=groups"));
		tested.setUpdateMode(true);

		tested.removeAttributeValue("uniqueMember", LdapUtils.newLdapName("cn=john doe, ou=company"));
		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);

		ModificationItem modificationItem = modificationItems[0];
		assertThat(modificationItem.getModificationOp()).isEqualTo(DirContext.REMOVE_ATTRIBUTE);
		assertThat(modificationItem.getAttribute().getID()).isEqualTo("uniqueMember");
		assertThat(modificationItem.getAttribute().get()).isEqualTo("cn=john doe,OU=company");
	}

	@Test
	public void testAddDnAttributeNewValue() throws NamingException {
		BasicAttributes attributes = new BasicAttributes();
		attributes.put("uniqueMember", "cn=john doe, ou=company");

		DirContextAdapter tested = new DirContextAdapter(attributes,
				LdapUtils.newLdapName("cn=administrators, ou=groups"));
		tested.setUpdateMode(true);

		tested.addAttributeValue("uniqueMember", LdapUtils.newLdapName("cn=jane doe, ou=company"));
		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);

		ModificationItem modificationItem = modificationItems[0];
		assertThat(modificationItem.getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		assertThat(modificationItem.getAttribute().getID()).isEqualTo("uniqueMember");
		assertThat(modificationItem.getAttribute().get()).isEqualTo("cn=jane doe, ou=company");
	}

	@Test
	public void testSetDnAttributeValueIdentical() {
		BasicAttributes attributes = new BasicAttributes();
		attributes.put("uniqueMember", "cn=john doe, ou=company");

		DirContextAdapter tested = new DirContextAdapter(attributes,
				LdapUtils.newLdapName("cn=administrators, ou=groups"));
		tested.setUpdateMode(true);

		tested.setAttributeValue("uniqueMember", LdapUtils.newLdapName("cn=john doe, ou=company"));
		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(0);
	}

	@Test
	public void testSetDnAttributesValueIdentical() {
		BasicAttributes attributes = new BasicAttributes();
		attributes.put("uniqueMember", "cn=john doe, ou=company");

		DirContextAdapter tested = new DirContextAdapter(attributes,
				LdapUtils.newLdapName("cn=administrators, ou=groups"));
		tested.setUpdateMode(true);

		tested.setAttributeValues("uniqueMember", new Object[] { LdapUtils.newLdapName("cn=john doe, ou=company") });
		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(0);
	}

	@Test
	public void testSetDnAttributesValuesOneNewEntry() throws NamingException {
		BasicAttributes attributes = new BasicAttributes();
		attributes.put("uniqueMember", "cn=john doe, ou=company");

		DirContextAdapter tested = new DirContextAdapter(attributes,
				LdapUtils.newLdapName("cn=administrators, ou=groups"));
		tested.setUpdateMode(true);

		tested.setAttributeValues("uniqueMember", new Object[] { LdapUtils.newLdapName("cn=john doe, ou=company"),
				LdapUtils.newLdapName("cn=jane doe, ou=company") });

		ModificationItem[] modificationItems = tested.getModificationItems();
		assertThat(modificationItems.length).isEqualTo(1);

		ModificationItem modificationItem = modificationItems[0];
		assertThat(modificationItem.getModificationOp()).isEqualTo(DirContext.ADD_ATTRIBUTE);
		assertThat(modificationItem.getAttribute().getID()).isEqualTo("uniqueMember");
		assertThat(modificationItem.getAttribute().get()).isEqualTo("cn=jane doe, ou=company");
	}

}
