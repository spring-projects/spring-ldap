/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ldap.support;

import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.springframework.ldap.support.DirContextAdapter;

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

    private DirContextAdapter classUnderTest;

    protected void setUp() throws Exception {
        super.setUp();
        classUnderTest = new DirContextAdapter();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetUpdateMode() throws Exception {
        assertFalse(classUnderTest.isUpdateMode());
        classUnderTest.setUpdateMode(true);
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setUpdateMode(false);
        assertFalse(classUnderTest.isUpdateMode());
    }

    public void testGetModificationItems() throws Exception {
        ModificationItem[] items = classUnderTest.getModificationItems();
        assertEquals(0, items.length);
        classUnderTest.setUpdateMode(true);
        assertEquals(0, items.length);
    }

    public void testAlwaysReplace() throws Exception {
        ModificationItem[] items = classUnderTest.getModificationItems();
        assertEquals(0, items.length);
        classUnderTest.setUpdateMode(true);
        assertEquals(0, items.length);
    }

    public void testGetStringAttributeNotExists() throws Exception {
        String s = classUnderTest.getStringAttribute("does not exist");
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
        classUnderTest = new TestableDirContextAdapter();
        String s = classUnderTest.getStringAttribute("abc");
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
        classUnderTest = new TestableDirContextAdapter();
        String s[] = classUnderTest.getStringAttributes("abc");
        assertEquals("123", s[0]);
        assertEquals("234", s[1]);
        assertEquals(2, s.length);
    }

    public void testGetStringAttributesNotExists() throws Exception {
        String s[] = classUnderTest.getStringAttributes("abc");
        assertNull(s);
    }

    public void testSetStringAttribute() throws Exception {
        assertFalse(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValue("abc", "123");
        Attributes attrs = classUnderTest.getAttributes();
        Attribute attr = attrs.get("abc");
        assertEquals("123", (String) attr.get());
    }

    public void testSetStringAttributeNull() throws Exception {
        assertFalse(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValue("abc", null);
        Attributes attrs = classUnderTest.getAttributes();
        Attribute attr = attrs.get("abc");
        assertNull(attr);
    }

    public void testAddAttribute() throws Exception {
        classUnderTest.setUpdateMode(true);
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValue("abc", "123");
        Attributes attrs = classUnderTest.getAttributes();
        Attribute attr = attrs.get("abc");
        assertNull(attr);

        ModificationItem[] mods = classUnderTest.getModificationItems();
        assertEquals(1, mods.length);
        assertEquals(DirContext.ADD_ATTRIBUTE, mods[0].getModificationOp());
        attr = mods[0].getAttribute();
        assertEquals("123", (String) attr.get());

        String[] modNames = classUnderTest.getNamesOfModifiedAttributes();
        assertEquals(1, modNames.length);
        assertEquals("abc", modNames[0]);

        classUnderTest.update();
        mods = classUnderTest.getModificationItems();
        assertEquals(0, mods.length);
        modNames = classUnderTest.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
        attrs = classUnderTest.getAttributes();
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
        classUnderTest.setUpdateMode(true);
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("abc", new String[] { "123", "456" });
        Attributes attrs = classUnderTest.getAttributes();
        Attribute attr = attrs.get("abc");
        assertNull(attr);
        ModificationItem[] mods = classUnderTest.getModificationItems();
        assertEquals(1, mods.length);
        assertEquals(DirContext.ADD_ATTRIBUTE, mods[0].getModificationOp());
        attr = mods[0].getAttribute();
        assertEquals(2, attr.size());
        assertEquals("123", (String) attr.get(0));
        assertEquals("456", (String) attr.get(1));

        String[] modNames = classUnderTest.getNamesOfModifiedAttributes();
        assertEquals(1, modNames.length);
        assertEquals("abc", modNames[0]);

        classUnderTest.update();
        mods = classUnderTest.getModificationItems();
        assertEquals(0, mods.length);
        modNames = classUnderTest.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
        attrs = classUnderTest.getAttributes();
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
        classUnderTest = new TestableDirContextAdapter();

        classUnderTest.setUpdateMode(true);
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValue("abc", null);
        Attributes attrs = classUnderTest.getAttributes();
        Attribute attr = attrs.get("abc");
        assertEquals("123", (String) attr.get());

        ModificationItem[] mods = classUnderTest.getModificationItems();
        assertEquals(1, mods.length);
        assertEquals(DirContext.REMOVE_ATTRIBUTE, mods[0].getModificationOp());
        attr = mods[0].getAttribute();
        assertEquals("abc", (String) attr.getID());
        String[] modNames = classUnderTest.getNamesOfModifiedAttributes();
        assertEquals(1, modNames.length);
        assertEquals("abc", modNames[0]);

        classUnderTest.update();
        mods = classUnderTest.getModificationItems();
        assertEquals(0, mods.length);
        modNames = classUnderTest.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
        attrs = classUnderTest.getAttributes();
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
        classUnderTest = new TestableDirContextAdapter();

        classUnderTest.setUpdateMode(true);
        classUnderTest.setAttributeValues("abc", new String[] {});

        ModificationItem[] mods = classUnderTest.getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        classUnderTest.setAttributeValue("abc", "234"); // change

        ModificationItem[] mods = classUnderTest.getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValue("abc", "123"); // change

        ModificationItem[] mods = classUnderTest.getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("abc", new String[] { "123", "qwe" });

        ModificationItem[] mods = classUnderTest.getModificationItems();
        assertEquals(0, mods.length);
        String[] modNames = classUnderTest.getNamesOfModifiedAttributes();
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
        classUnderTest = new TestableDirContextAdapter();
        classUnderTest.setAttributeValues("abc", new String[] { "qwe", "123" });

        ModificationItem[] mods = classUnderTest.getModificationItems();
        assertEquals(0, mods.length);
        String[] modNames = classUnderTest.getNamesOfModifiedAttributes();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("abc", new String[] { "qwe", "123" },
                true);

        // change
        ModificationItem[] mods = classUnderTest.getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("abc", new String[] { "123", "qwe",
                "klytt" });

        ModificationItem[] modificationItems = classUnderTest
                .getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("abc", new String[] { "123" });

        ModificationItem[] modificationItems = classUnderTest
                .getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("abc", new String[] { "123" });

        ModificationItem[] modificationItems = classUnderTest
                .getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("abc", null);

        ModificationItem[] modificationItems = classUnderTest
                .getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("abc", new String[] { "123", "qwe" });

        ModificationItem[] modificationItems = classUnderTest
                .getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("abc", new String[] { "123", "qwe",
                "klytt", "kalle" });

        ModificationItem[] modificationItems = classUnderTest
                .getModificationItems();
        assertEquals(2, modificationItems.length);

        assertEquals(DirContext.ADD_ATTRIBUTE, modificationItems[0]
                .getModificationOp());
        Attribute modifiedAttribute = modificationItems[0].getAttribute();
        assertEquals("abc", modifiedAttribute.getID());
        assertEquals(2, modifiedAttribute.size());
        assertEquals("klytt", modifiedAttribute.get(0));
        assertEquals("kalle", modifiedAttribute.get(1));

        modifiedAttribute = modificationItems[1].getAttribute();
        assertEquals(DirContext.REMOVE_ATTRIBUTE, modificationItems[1]
                .getModificationOp());
        assertEquals("abc", modifiedAttribute.getID());
        assertEquals(2, modifiedAttribute.size());
        assertEquals("rty", modifiedAttribute.get(0));
        assertEquals("uio", modifiedAttribute.get(1));
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValues("def", new String[] { "kalle",
                "klytt" });

        ModificationItem[] modificationItems = classUnderTest
                .getModificationItems();
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValue("abc", "234"); // change
        classUnderTest.setAttributeValue("abc", "987");
        // change a second time
        ModificationItem[] mods = classUnderTest.getModificationItems();
        assertEquals(1, mods.length);
        assertEquals(DirContext.REPLACE_ATTRIBUTE, mods[0].getModificationOp());
        Attribute attr = mods[0].getAttribute();
        assertEquals("abc", (String) attr.getID());
        assertEquals("987", (String) attr.get());

        classUnderTest.update();
        mods = classUnderTest.getModificationItems();
        assertEquals(0, mods.length);
        String[] modNames = classUnderTest.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);
        Attributes attrs = classUnderTest.getAttributes();
        attr = attrs.get("abc");
        assertEquals("987", (String) attr.get());
        assertEquals("987", classUnderTest.getStringAttribute("abc"));
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
        classUnderTest = new TestableDirContextAdapter();
        assertTrue(classUnderTest.isUpdateMode());
        classUnderTest.setAttributeValue("abc", "234"); // change
        classUnderTest.setAttributeValue("qwe", null); // remove
        classUnderTest.setAttributeValue("zzz", "new"); // new
        Attributes attrs = classUnderTest.getAttributes();
        Attribute attr = attrs.get("abc");
        assertEquals("123", (String) attr.get());
        assertEquals(2, attrs.size());

        ModificationItem[] mods = classUnderTest.getModificationItems();
        assertEquals(3, mods.length);
        String[] modNames = classUnderTest.getNamesOfModifiedAttributes();
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

        classUnderTest.update();
        mods = classUnderTest.getModificationItems();
        assertEquals(0, mods.length);
        modNames = classUnderTest.getNamesOfModifiedAttributes();
        assertEquals(0, modNames.length);

        attrs = classUnderTest.getAttributes();
        assertEquals(2, attrs.size());
        attr = attrs.get("abc");
        assertEquals("234", (String) attr.get());
        assertEquals("new", classUnderTest.getStringAttribute("zzz"));
    }

    private ModificationItem getModificationItem(ModificationItem[] mods,
            int operation) {
        for (int i = 0; i < mods.length; i++) {
            if (mods[i].getModificationOp() == operation)
                return mods[i];
        }
        return null;
    }
}
