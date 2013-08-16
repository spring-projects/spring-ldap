/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.core.support;

import junit.framework.TestCase;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

/**
 * IncrementalAttributesMapper Tester.
 *
 * @author Marius Scurtescu
 * @author Mattias Hellborg Arthursson
 */
public class DefaultIncrementalAttributesMapperTest extends TestCase {
    private DefaultIncrementalAttributesMapper tested;

    public DefaultIncrementalAttributesMapperTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        tested = new DefaultIncrementalAttributesMapper("member");
    }

    public void tearDown() throws Exception {
        tested = null;
    }

    public void testGetAttributesArray() throws Exception {
        String[] attributes = tested.getAttributesForLookup();

        assertEquals(1, attributes.length);
        assertEquals("member", attributes[0]);

        tested = new DefaultIncrementalAttributesMapper(10, "member");

        attributes = tested.getAttributesForLookup();

        assertEquals(1, attributes.length);
        assertEquals("member;Range=0-10", attributes[0]);
    }

    public void testGetAttributesArrayWithTwoAttributes() {
        tested = new DefaultIncrementalAttributesMapper(20, new String[]{"member", "cn"});
        String[] attributes = tested.getAttributesForLookup();

        assertEquals(2, attributes.length);

        assertEquals("member;Range=0-20", attributes[0]);
        assertEquals("cn;Range=0-20", attributes[1]);
    }

    public void testLoopEmpty() throws Exception {
        assertTrue(tested.hasMore());

        Attributes attributes = new BasicAttributes();

        tested.mapFromAttributes(attributes);

        assertFalse(tested.hasMore());
        assertNull(tested.getValues("member"));
    }

    public void testLoop() throws Exception {
        Attributes attributes = createAttributes("member", new RangeOption(0, 10));

        tested.mapFromAttributes(attributes);

        assertTrue(tested.hasMore());
        assertEquals(11, tested.getValues("member").size());

        attributes = createAttributes("member", new RangeOption(11), 5);

        tested.mapFromAttributes(attributes);

        assertFalse(tested.hasMore());
        assertEquals(16, tested.getValues("member").size());
    }

    public void test1LoopWithPageSizeExact() throws Exception {
        tested = new DefaultIncrementalAttributesMapper(10, "member");

        Attributes attributes = createAttributes("member", new RangeOption(0, 10));

        tested.mapFromAttributes(attributes);

        assertFalse(tested.hasMore());
        assertEquals(11, tested.getValues("member").size());
    }

    public void test2LoopsWithPageSizeExact() throws Exception {
        tested = new DefaultIncrementalAttributesMapper(20, "member");

        Attributes attributes = createAttributes("member", new RangeOption(0, 10));

        tested.mapFromAttributes(attributes);

        assertTrue(tested.hasMore());
        assertEquals(11, tested.getValues("member").size());

        attributes = createAttributes("member", new RangeOption(11, 30));

        tested.mapFromAttributes(attributes);

        assertFalse(tested.hasMore());
        assertEquals(31, tested.getValues("member").size());
    }

    public void test2LoopsWithPageSize() throws Exception {
        tested = new DefaultIncrementalAttributesMapper(20, "member");

        Attributes attributes = createAttributes("member", new RangeOption(0, 10));

        tested.mapFromAttributes(attributes);

        assertTrue(tested.hasMore());
        assertEquals(11, tested.getValues("member").size());

        attributes = createAttributes("member", new RangeOption(11), 5);

        tested.mapFromAttributes(attributes);

        assertFalse(tested.hasMore());
        assertEquals(16, tested.getValues("member").size());
    }

    public void testLoopWithTwoRangedAttributesLoopOnOneAttribute() throws Exception {
        tested = new DefaultIncrementalAttributesMapper(10, new String[]{"member", "cn"});

        Attributes attributes = createAttributes("member", new RangeOption(0, 5));
        attributes.put(createRangeAttribute("cn", new RangeOption(0, 10), 10));

        tested.mapFromAttributes(attributes);

        assertTrue(tested.hasMore());
        assertEquals(6, tested.getValues("member").size());
        assertEquals(10, tested.getValues("cn").size());

        assertEquals(1, tested.getAttributesForLookup().length);

        attributes = createAttributes("member", new RangeOption(6), 5);

        tested.mapFromAttributes(attributes);

        assertFalse(tested.hasMore());
        assertEquals(11, tested.getValues("member").size());
    }
    private Attributes createAttributes(String attributeName, RangeOption range) {
        return createAttributes(attributeName, range, range.getTerminal() - range.getInitial() + 1);
    }

    private Attributes createAttributes(String attributeName, RangeOption range, int valueCnt) {
        Attributes attributes = new BasicAttributes();

        Attribute attribute = createRangeAttribute(attributeName, range, valueCnt);
        attributes.put(attribute);

        return attributes;

    }

    private Attribute createRangeAttribute(String attributeName, RangeOption range, int valueCnt) {
        Attribute attribute = new BasicAttribute(attributeName + ";" + range.toString());
        for (int i = 0; i < valueCnt; i++) {
            attribute.add("value" + (range.getInitial() + i - 1));
        }
        return attribute;
    }
}
