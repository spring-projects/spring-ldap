/*
 * Copyright 2005-2010 the original author or authors.
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
package org.springframework.ldap.support.ad;

import junit.framework.TestCase;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

/**
 * IncrementalAttributeMapper Tester.
 *
 * @author Marius Scurtescu
 */
public class IncrementalAttributeMapperTest extends TestCase {
    private IncrementalAttributeMapper incrementalAttributeMapper;
    private ListAttributeValueProcessor valueProcessor;

    public IncrementalAttributeMapperTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        valueProcessor = new ListAttributeValueProcessor();
        incrementalAttributeMapper = new IncrementalAttributeMapper("member", valueProcessor);
    }

    public void tearDown() throws Exception {
        incrementalAttributeMapper = null;
        valueProcessor = null;
    }

    public void testGetAttributesArray() throws Exception {
        String[] attributes = incrementalAttributeMapper.getAttributesArray();

        assertEquals(1, attributes.length);
        assertEquals("member", attributes[0]);

        incrementalAttributeMapper = new IncrementalAttributeMapper("member", valueProcessor, 10);

        attributes = incrementalAttributeMapper.getAttributesArray();

        assertEquals(1, attributes.length);
        assertEquals("member;Range=0-10", attributes[0]);
    }

    public void testLoopEmpty() throws Exception {
        assertTrue(incrementalAttributeMapper.hasMore());

        Attributes attributes = new BasicAttributes();

        incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(incrementalAttributeMapper.hasMore());
        assertEquals(0, valueProcessor.getValues().size());
    }

    public void testLoop() throws Exception {
        Attributes attributes = createAttributes(new RangeOption(0, 10), true);

        incrementalAttributeMapper.mapFromAttributes(attributes);

        assertTrue(incrementalAttributeMapper.hasMore());
        assertEquals(11, valueProcessor.getValues().size());

        attributes = createAttributes(new RangeOption(11), 5, false);

        incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(incrementalAttributeMapper.hasMore());
        assertEquals(16, valueProcessor.getValues().size());
    }

    public void test1LoopWithPageSizeExact() throws Exception {
        incrementalAttributeMapper = new IncrementalAttributeMapper("member", valueProcessor, 10);

        Attributes attributes = createAttributes(new RangeOption(0, 10), true);

        incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(incrementalAttributeMapper.hasMore());
        assertEquals(11, valueProcessor.getValues().size());
    }

    public void test2LoopsWithPageSizeExact() throws Exception {
        incrementalAttributeMapper = new IncrementalAttributeMapper("member", valueProcessor, 20);

        Attributes attributes = createAttributes(new RangeOption(0, 10), true);

        incrementalAttributeMapper.mapFromAttributes(attributes);

        assertTrue(incrementalAttributeMapper.hasMore());
        assertEquals(11, valueProcessor.getValues().size());

        attributes = createAttributes(new RangeOption(11, 30), false);

        incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(incrementalAttributeMapper.hasMore());
        assertEquals(31, valueProcessor.getValues().size());
    }

    public void test2LoopsWithPageSize() throws Exception {
        incrementalAttributeMapper = new IncrementalAttributeMapper("member", valueProcessor, 20);

        Attributes attributes = createAttributes(new RangeOption(0, 10), true);

        incrementalAttributeMapper.mapFromAttributes(attributes);

        assertTrue(incrementalAttributeMapper.hasMore());
        assertEquals(11, valueProcessor.getValues().size());

        attributes = createAttributes(new RangeOption(11), 5, false);

        incrementalAttributeMapper.mapFromAttributes(attributes);

        assertFalse(incrementalAttributeMapper.hasMore());
        assertEquals(16, valueProcessor.getValues().size());
    }

    private Attributes createAttributes(RangeOption range, boolean emptyPlain) {
        return createAttributes(range, range.getTerminal() - range.getInitial() + 1, emptyPlain);
    }

    private Attributes createAttributes(RangeOption range, int valueCnt, boolean emptyPlain) {
        Attributes attributes = new BasicAttributes();

        if (emptyPlain) {
            attributes.put(new BasicAttribute("member"));
        }

        Attribute attribute = new BasicAttribute("member;" + range.toString());
        for (int i = 0; i < valueCnt; i++) {
            attribute.add("value" + (range.getInitial() + i - 1));
        }
        attributes.put(attribute);

        return attributes;

    }
}
