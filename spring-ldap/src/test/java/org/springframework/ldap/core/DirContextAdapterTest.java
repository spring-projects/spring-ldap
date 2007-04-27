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

package org.springframework.ldap.core;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import junit.framework.TestCase;

/**
 * Tests the DirContextAdapter class.
 * 
 * @author Andreas Ronge
 * @author Mattias Arthursson
 */
public class DirContextAdapterTest extends TestCase {
    private static final DistinguishedName BASE_NAME = new DistinguishedName(
            "dc=jayway, dc=se");

    private static final DistinguishedName DUMMY_NAME = new DistinguishedName(
            "c=SE, dc=jayway, dc=se");

    private DirContextAdapter tested;

    protected void setUp() throws Exception {
        super.setUp();
        tested = new DirContextAdapter();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetUpdateMode() throws Exception {
        assertFalse(tested.isUpdateMode());
        tested.setUpdateMode(true);
        assertTrue(tested.isUpdateMode());
        tested.setUpdateMode(false);
        assertFalse(tested.isUpdateMode());
    }

    public void testGetModificationItems() throws Exception {
        ModificationItem[] items = tested.getModificationItems();
        assertEquals(0, items.length);
        tested.setUpdateMode(true);
        assertEquals(0, items.length);
    }

    public void testAlwaysReplace() throws Exception {
        ModificationItem[] items = tested.getModificationItems();
        assertEquals(0, items.length);
        tested.setUpdateMode(true);
        assertEquals(0, items.length);
    }

    public void testGetStringAttributeNotExists() throws Exception {
        String s = tested.getStringAttribute("does not exist");
        assertNull(s);
    }

    public void testGetStringAttributeExists() throws Exception {
        final Attributes attrs = new BasicAttributes();
        attrs.put(new BasicAttribute("abc", "def"));
        class TestableDirContextAdapter extends DirContextAdapter {
            public TestableDirContextAdapter() {
                super(attrs, null);
            }
        }
        tested = new TestableDirContextAdapter();
        String s = tested.getStringAttribute("abc");
        assertEquals("def", s);
    }

    public void testGetStringAttributesExists() throws Exception {
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
        assertEquals("123", s[0]);
        assertEquals("234", s[1]);
        assertEquals(2, s.length);
    }

    public void testGetStringAttributesNotExists() throws Exception {
        String s[] = tested.getStringAttributes("abc");
        assertNull(s);
    }

    public void testAddAttributeValue() throws NamingException {
        // Perform test
        tested.addAttributeValue("abc", "123");

        Attributes attrs = tested.getAttributes();
        Attribute attr = attrs.get("abc");
        assertEquals("123", (String) attr.get());
    }

    public void testAddAttributeValueAttributeWithOtherValueExists()
            throws NamingException {
        tested.setAttribute(new BasicAttribute("abc", "321"));

        // Perform test
        tested.addAttributeValue("abc", "123");

        Attributes attrs = tested.getAttributes();
        Attribute attr = attrs.get("abc");
        assertEquals("321", (String) attr.get(0));
        assertEquals("123", (String) attr.get(1));
    }

    public void testAddAttributeValueAttributeWithSameValueExists()
            throws NamingException {
        tested.setAttribute(new BasicAttribute("abc", "123"));

        // Perform test
        tested.addAttributeValue("abc", "123");

        Attributes attrs = tested.getAttributes();
        Attribute attr = attrs.get("abc");
        assertEquals(1, attr.size());
        assertEquals("123", (String) attr.get(0));
    }

    public void testAddAttributeValueInUpdateMode() throws NamingException {
        tested.setUpdateMode(true);
        tested.addAttributeValue("abc", "123");

        // Perform test
        Attributes attrs = tested.getAttributes();
        assertNull(attrs.get("abc"));

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        Attribute attribute = modificationItems[0].getAttribute();
        assertEquals("abc", attribute.getID());
        assertEquals("123", attribute.get());
    }

    public void testAddAttributeValueInUpdateModeAttributeWithOtherValueExistsInOrigAttrs()
            throws NamingException {

        tested.setAttribute(new BasicAttribute("abc", "321"));
        tested.setUpdateMode(true);

        // Perform test
        tested.addAttributeValue("abc", "123");

        Attributes attrs = tested.getAttributes();
        assertNotNull(attrs.get("abc"));

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        Attribute attribute = modificationItems[0].getAttribute();
        assertEquals(1, attribute.size());
        assertEquals("abc", attribute.getID());
        assertEquals("123", attribute.get());
    }

    public void testAddAttributeValueInUpdateModeAttributeWithSameValueExistsInOrigAttrs()
            throws NamingException {

        tested.setAttribute(new BasicAttribute("abc", "123"));
        tested.setUpdateMode(true);

        // Perform test
        tested.addAttributeValue("abc", "123");

        Attributes attrs = tested.getAttributes();
        assertNotNull(attrs.get("abc"));

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(0, modificationItems.length);
    }

    public void testAddAttributeValueInUpdateModeAttributeWithOtherValueExistsInUpdAttrs()
            throws NamingException {
        tested.setUpdateMode(true);
        tested.setAttributeValue("abc", "321");

        // Perform test
        tested.addAttributeValue("abc", "123");

        Attributes attrs = tested.getAttributes();
        assertNull(attrs.get("abc"));

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        Attribute attribute = modificationItems[0].getAttribute();
        assertEquals("abc", attribute.getID());
        assertEquals("321", attribute.get(0));
        assertEquals("123", attribute.get(1));
    }

    public void testAddAttributeValueInUpdateModeAttributeWithSameValueExistsInUpdAttrs()
            throws NamingException {
        tested.setUpdateMode(true);
        tested.setAttributeValue("abc", "123");

        // Perform test
        tested.addAttributeValue("abc", "123");

        Attributes attrs = tested.getAttributes();
        assertNull(attrs.get("abc"));

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        Attribute attribute = modificationItems[0].getAttribute();
        assertEquals(1, attribute.size());
        assertEquals("abc", attribute.getID());
        assertEquals("123", attribute.get());
    }

    public void testRemoveAttributeValueAttributeDoesntExist() {
        // Perform test
        tested.removeAttributeValue("abc", "123");

        Attributes attributes = tested.getAttributes();
        assertNull(attributes.get("abc"));
    }

    public void testRemoveAttributeValueAttributeWithOtherValueExists()
            throws NamingException {
        tested.setAttribute(new BasicAttribute("abc", "321"));

        // Perform test
        tested.removeAttributeValue("abc", "123");

        Attributes attributes = tested.getAttributes();
        Attribute attr = attributes.get("abc");
        assertNotNull(attr);
        assertEquals(1, attr.size());
        assertEquals("321", attr.get());
    }

    public void testRemoveAttributeValueAttributeWithSameValueExists() {
        tested.setAttribute(new BasicAttribute("abc", "123"));

        // Perform test
        tested.removeAttributeValue("abc", "123");

        Attributes attributes = tested.getAttributes();
        Attribute attr = attributes.get("abc");
        assertNull(attr);
    }

    public void testRemoveAttributeValueAttributeWithOtherAndSameValueExists()
            throws NamingException {
        BasicAttribute basicAttribute = new BasicAttribute("abc");
        basicAttribute.add("123");
        basicAttribute.add("321");
        tested.setAttribute(basicAttribute);

        // Perform test
        tested.removeAttributeValue("abc", "123");

        Attributes attributes = tested.getAttributes();
        Attribute attr = attributes.get("abc");
        assertNotNull(attr);
        assertEquals(1, attr.size());
        assertEquals("321", attr.get());
    }

    public void testRemoveAttributeValueInUpdateMode() {
        tested.setUpdateMode(true);

        // Perform test
        tested.removeAttributeValue("abc", "123");

        assertNull(tested.getAttributes().get("abc"));

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(0, modificationItems.length);
    }

    public void testRemoveAttributeValueInUpdateModeSameValueExistsInUpdatedAttrs() {
        tested.setUpdateMode(true);
        tested.setAttributeValue("abc", "123");

        // Perform test
        tested.removeAttributeValue("abc", "123");

        assertNull(tested.getAttributes().get("abc"));

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(0, modificationItems.length);
    }

    public void testRemoveAttributeValueInUpdateModeOtherValueExistsInUpdatedAttrs()
            throws NamingException {
        tested.setUpdateMode(true);
        tested.setAttributeValue("abc", "321");

        // Perform test
        tested.removeAttributeValue("abc", "123");

        assertNull(tested.getAttributes().get("abc"));

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        Attribute modificationAttribute = modificationItems[0].getAttribute();
        assertEquals("abc", modificationAttribute.getID());
        assertEquals(1, modificationAttribute.size());
        assertEquals("321", modificationAttribute.get());
    }

    public void testRemoveAttributeValueInUpdateModeOtherAndSameValueExistsInUpdatedAttrs()
            throws NamingException {
        tested.setUpdateMode(true);
        tested.setAttributeValues("abc", new String[] { "321", "123" });

        // Perform test
        tested.removeAttributeValue("abc", "123");

        assertNull(tested.getAttributes().get("abc"));

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        Attribute modificationAttribute = modificationItems[0].getAttribute();
        assertEquals("abc", modificationAttribute.getID());
        assertEquals(1, modificationAttribute.size());
    }

    public void testRemoveAttributeValueInUpdateModeSameValueExistsInOrigAttrs() {
        tested.setAttribute(new BasicAttribute("abc", "123"));
        tested.setUpdateMode(true);

        // Perform test
        tested.removeAttributeValue("abc", "123");

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        Attribute modificationAttribute = modificationItems[0].getAttribute();
        assertEquals("abc", modificationAttribute.getID());
        assertEquals(0, modificationAttribute.size());
        assertEquals(DirContext.REMOVE_ATTRIBUTE, modificationItems[0]
                .getModificationOp());
    }

    public void testRemoveAttributeValueInUpdateModeSameAndOtherValueExistsInOrigAttrs()
            throws NamingException {
        BasicAttribute basicAttribute = new BasicAttribute("abc");
        basicAttribute.add("123");
        basicAttribute.add("321");
        tested.setAttribute(basicAttribute);
        tested.setUpdateMode(true);

        // Perform test
        tested.removeAttributeValue("abc", "123");

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        Attribute modificationAttribute = modificationItems[0].getAttribute();
        assertEquals("abc", modificationAttribute.getID());
        assertEquals(1, modificationAttribute.size());
        assertEquals("123", modificationAttribute.get());
        assertEquals(DirContext.REMOVE_ATTRIBUTE, modificationItems[0]
                .getModificationOp());
    }

    public void testSetStringAttribute() throws Exception {
        assertFalse(tested.isUpdateMode());
        tested.setAttributeValue("abc", "123");
        Attributes attrs = tested.getAttributes();
        Attribute attr = attrs.get("abc");
        assertEquals("123", (String) attr.get());
    }

    public void testSetStringAttributeNull() throws Exception {
        assertFalse(tested.isUpdateMode());
        tested.setAttributeValue("abc", null);
        Attributes attrs = tested.getAttributes();
        Attribute attr = attrs.get("abc");
        assertNull(attr);
    }

    public void testAddAttribute() throws Exception {
        tested.setUpdateMode(true);
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValue("abc", "123");
        Attributes attrs = tested.getAttributes();
        Attribute attr = attrs.get("abc");
        assertNull(attr);

        ModificationItem[] mods = tested.getModificationItems();
        assertEquals(1, mods.length);
        assertEquals(DirContext.ADD_ATTRIBUTE, mods[0].getModificationOp());
        attr = mods[0].getAttribute();
        assertEquals("123", (String) attr.get());

        String[] modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(1, modNames.length);
        assertEquals("abc", modNames[0]);

        tested.update();
        mods = tested.getModificationItems();
        assertEquals(0, mods.length);
        modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
        attrs = tested.getAttributes();
        attr = attrs.get("abc");
        assertEquals("123", (String) attr.get());
    }

    public void testGetDn() throws Exception {
        DirContextAdapter tested = new DirContextAdapter(DUMMY_NAME);
        Name result = tested.getDn();
        assertEquals(DUMMY_NAME, result);
    }

    public void testGetDn_BasePath() {
        DirContextAdapter tested = new DirContextAdapter(null, DUMMY_NAME,
                BASE_NAME);
        Name result = tested.getDn();
        assertEquals(DUMMY_NAME, result);
    }

    public void testGetNameInNamespace() {
        DirContextAdapter tested = new DirContextAdapter(DUMMY_NAME);
        String result = tested.getNameInNamespace();
        assertEquals(DUMMY_NAME.toString(), result);
    }

    public void testGetNameInNamespace_BasePath() {
        DirContextAdapter tested = new DirContextAdapter(null,
                new DistinguishedName("c=SE"), BASE_NAME);
        String result = tested.getNameInNamespace();
        assertEquals(DUMMY_NAME.toString(), result);
    }

    public void testAddMultiAttributes() throws Exception {
        tested.setUpdateMode(true);
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValues("abc", new String[] { "123", "456" });
        Attributes attrs = tested.getAttributes();
        Attribute attr = attrs.get("abc");
        assertNull(attr);
        ModificationItem[] mods = tested.getModificationItems();
        assertEquals(1, mods.length);
        assertEquals(DirContext.ADD_ATTRIBUTE, mods[0].getModificationOp());
        attr = mods[0].getAttribute();
        assertEquals(2, attr.size());
        assertEquals("123", (String) attr.get(0));
        assertEquals("456", (String) attr.get(1));

        String[] modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(1, modNames.length);
        assertEquals("abc", modNames[0]);

        tested.update();
        mods = tested.getModificationItems();
        assertEquals(0, mods.length);
        modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
        attrs = tested.getAttributes();
        attr = attrs.get("abc");
        assertEquals("123", (String) attr.get(0));
        assertEquals("456", (String) attr.get(1));
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValue("abc", null);
        Attributes attrs = tested.getAttributes();
        Attribute attr = attrs.get("abc");
        assertEquals("123", (String) attr.get());

        ModificationItem[] mods = tested.getModificationItems();
        assertEquals(1, mods.length);
        assertEquals(DirContext.REMOVE_ATTRIBUTE, mods[0].getModificationOp());
        attr = mods[0].getAttribute();
        assertEquals("abc", (String) attr.getID());
        String[] modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(1, modNames.length);
        assertEquals("abc", modNames[0]);

        tested.update();
        mods = tested.getModificationItems();
        assertEquals(0, mods.length);
        modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
        attrs = tested.getAttributes();
        attr = attrs.get("abc");
        assertNull(attr);
    }

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
        assertEquals(1, mods.length);
        assertEquals(DirContext.REMOVE_ATTRIBUTE, mods[0].getModificationOp());
        Attribute attr = mods[0].getAttribute();
        assertEquals("abc", (String) attr.getID());
        assertEquals(0, attr.size());
    }

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
        assertEquals(1, mods.length);
        assertEquals(DirContext.REPLACE_ATTRIBUTE, mods[0].getModificationOp());
        Attribute attr = mods[0].getAttribute();
        assertEquals("abc", (String) attr.getID());
        assertEquals("234", (String) attr.get());
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValue("abc", "123"); // change

        ModificationItem[] mods = tested.getModificationItems();
        assertEquals(0, mods.length);
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValues("abc", new String[] { "123", "qwe" });

        ModificationItem[] mods = tested.getModificationItems();
        assertEquals(0, mods.length);
        String[] modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
    }

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
        assertEquals(0, mods.length);
        String[] modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValues("abc", new String[] { "qwe", "123" }, true);

        // change
        ModificationItem[] mods = tested.getModificationItems();
        assertEquals(1, mods.length);
        assertEquals(DirContext.REPLACE_ATTRIBUTE, mods[0].getModificationOp());
        Attribute attr = mods[0].getAttribute();
        assertEquals("qwe", attr.get(0));
        assertEquals("123", attr.get(1));
    }

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
        assertTrue(tested.isUpdateMode());
        tested
                .setAttributeValues("abc",
                        new String[] { "123", "qwe", "klytt" });

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        assertEquals(DirContext.ADD_ATTRIBUTE, modificationItems[0]
                .getModificationOp());
        assertEquals("klytt", modificationItems[0].getAttribute().get());
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValues("abc", new String[] { "123" });

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        assertEquals(DirContext.REMOVE_ATTRIBUTE, modificationItems[0]
                .getModificationOp());
        assertEquals("qwe", modificationItems[0].getAttribute().get());
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValues("abc", new String[] { "123" });

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        assertEquals(DirContext.REMOVE_ATTRIBUTE, modificationItems[0]
                .getModificationOp());
        assertEquals("qwe", modificationItems[0].getAttribute().get(0));
        assertEquals("rty", modificationItems[0].getAttribute().get(1));
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValues("abc", null);

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        assertEquals(DirContext.REMOVE_ATTRIBUTE, modificationItems[0]
                .getModificationOp());
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValues("abc", new String[] { "123", "qwe" });

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(0, modificationItems.length);
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValues("abc", new String[] { "123", "qwe", "klytt",
                "kalle" });

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(2, modificationItems.length);

        Attribute modifiedAttribute = modificationItems[0].getAttribute();
        assertEquals(DirContext.REMOVE_ATTRIBUTE, modificationItems[0]
                .getModificationOp());
        assertEquals("abc", modifiedAttribute.getID());
        assertEquals(2, modifiedAttribute.size());
        assertEquals("rty", modifiedAttribute.get(0));
        assertEquals("uio", modifiedAttribute.get(1));

        assertEquals(DirContext.ADD_ATTRIBUTE, modificationItems[1]
                .getModificationOp());
        modifiedAttribute = modificationItems[1].getAttribute();
        assertEquals("abc", modifiedAttribute.getID());
        assertEquals(2, modifiedAttribute.size());
        assertEquals("klytt", modifiedAttribute.get(0));
        assertEquals("kalle", modifiedAttribute.get(1));
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValues("def", new String[] { "kalle", "klytt" });

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(1, modificationItems.length);
        assertEquals("def", modificationItems[0].getAttribute().getID());
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValue("abc", "234"); // change
        tested.setAttributeValue("abc", "987");
        // change a second time
        ModificationItem[] mods = tested.getModificationItems();
        assertEquals(1, mods.length);
        assertEquals(DirContext.REPLACE_ATTRIBUTE, mods[0].getModificationOp());
        Attribute attr = mods[0].getAttribute();
        assertEquals("abc", (String) attr.getID());
        assertEquals("987", (String) attr.get());

        tested.update();
        mods = tested.getModificationItems();
        assertEquals(0, mods.length);
        String[] modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
        Attributes attrs = tested.getAttributes();
        attr = attrs.get("abc");
        assertEquals("987", (String) attr.get());
        assertEquals("987", tested.getStringAttribute("abc"));
    }

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
        assertTrue(tested.isUpdateMode());
        tested.setAttributeValue("abc", "234"); // change
        tested.setAttributeValue("qwe", null); // remove
        tested.setAttributeValue("zzz", "new"); // new
        Attributes attrs = tested.getAttributes();
        Attribute attr = attrs.get("abc");
        assertEquals("123", (String) attr.get());
        assertEquals(2, attrs.size());

        ModificationItem[] mods = tested.getModificationItems();
        assertEquals(3, mods.length);
        String[] modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(3, modNames.length);

        ModificationItem mod = getModificationItem(mods,
                DirContext.REPLACE_ATTRIBUTE);
        assertNotNull(mod);
        attr = mod.getAttribute();
        assertEquals("abc", (String) attr.getID());
        assertEquals("234", (String) attr.get());

        mod = getModificationItem(mods, DirContext.REMOVE_ATTRIBUTE);
        assertNotNull(mod);
        attr = mod.getAttribute();
        assertEquals("qwe", (String) attr.getID());

        mod = getModificationItem(mods, DirContext.ADD_ATTRIBUTE);
        assertNotNull(mod);
        attr = mod.getAttribute();
        assertEquals("zzz", (String) attr.getID());
        assertEquals("new", (String) attr.get());

        tested.update();
        mods = tested.getModificationItems();
        assertEquals(0, mods.length);
        modNames = tested.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);

        attrs = tested.getAttributes();
        assertEquals(2, attrs.size());
        attr = attrs.get("abc");
        assertEquals("234", (String) attr.get());
        assertEquals("new", tested.getStringAttribute("zzz"));
    }

    /**
     * Test for LDAP-15: DirContextAdapter.setAttribute(). Verifies that setting
     * an Attribute should modify updatedAttrs if in update mode.
     * 
     * @throws NamingException
     */
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
        assertEquals(1, mods.length);
        assertEquals(DirContext.REPLACE_ATTRIBUTE, mods[0].getModificationOp());

        Attribute modificationAttribute = mods[0].getAttribute();
        assertEquals("cn", modificationAttribute.getID());
        assertEquals("nisse hult", modificationAttribute.get());
    }

    public void testGetStringAttributes_NullValue() {
        String result = tested.getStringAttribute("someAbsentAttribute");
        assertNull(result);
    }

    public void testGetStringAttributes_AttributeExists_NullValue() {
        tested.setAttribute(new BasicAttribute("someAttribute", null));
        String result = tested.getStringAttribute("someAttribute");
        assertNull(result);
    }

    private ModificationItem getModificationItem(ModificationItem[] mods,
            int operation) {
        for (int i = 0; i < mods.length; i++) {
            if (mods[i].getModificationOp() == operation)
                return mods[i];
        }
        return null;
    }

    public void testModifyMultiValueAttributeModificationOrder()
            throws NamingException {
        BasicAttribute attribute = new BasicAttribute("abc");
        attribute.add("Some Person");
        attribute.add("Some Other Person");

        tested.setAttribute(attribute);
        tested.setUpdateMode(true);

        tested.setAttributeValues("abc", new String[] { "some person",
                "Some Other Person" });

        // Perform test
        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(2, modificationItems.length);
        ModificationItem modificationItem = modificationItems[0];
        assertEquals(DirContext.REMOVE_ATTRIBUTE, modificationItem
                .getModificationOp());
        assertEquals("Some Person", modificationItem.getAttribute().get());
        modificationItem = modificationItems[1];
        assertEquals(DirContext.ADD_ATTRIBUTE, modificationItem
                .getModificationOp());
        assertEquals("some person", modificationItem.getAttribute().get());
    }

    /**
     * Test for LDAP-13.
     */
    public void testModifyAttributeByteArray() {
        tested.setAttribute(new BasicAttribute("abc", new byte[] { 1, 2, 3 }));

        tested.setUpdateMode(true);

        // Perform test
        tested.setAttributeValue("abc", new byte[] { 1, 2, 3 });

        ModificationItem[] modificationItems = tested.getModificationItems();
        assertEquals(0, modificationItems.length);
    }
}
