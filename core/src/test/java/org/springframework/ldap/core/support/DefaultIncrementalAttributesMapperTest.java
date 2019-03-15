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

package org.springframework.ldap.core.support;

import org.junit.Before;
import org.junit.Test;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IncrementalAttributesMapper Tester.
 *
 * @author Marius Scurtescu
 * @author Mattias Hellborg Arthursson
 */
public class DefaultIncrementalAttributesMapperTest {
    private DefaultIncrementalAttributesMapper tested;

    @Before
    public void setUp() throws Exception {
        tested = new DefaultIncrementalAttributesMapper("member");
    }

    @Test
    public void testGetAttributesArray() throws Exception {
        String[] attributes = tested.getAttributesForLookup();

        assertThat(attributes.length).isEqualTo(1);
        assertThat(attributes[0]).isEqualTo("member");

        tested = new DefaultIncrementalAttributesMapper(10, "member");

        attributes = tested.getAttributesForLookup();

        assertThat(attributes.length).isEqualTo(1);
        assertThat(attributes[0]).isEqualTo("member;Range=0-10");
    }

    @Test
    public void testGetAttributesArrayWithTwoAttributes() {
        tested = new DefaultIncrementalAttributesMapper(20, new String[]{"member", "cn"});
        String[] attributes = tested.getAttributesForLookup();

        assertThat(attributes.length).isEqualTo(2);

        assertThat(attributes[0]).isEqualTo("member;Range=0-20");
        assertThat(attributes[1]).isEqualTo("cn;Range=0-20");
    }

    @Test
    public void testLoopEmpty() throws Exception {
        assertThat(tested.hasMore()).isTrue();

        Attributes attributes = new BasicAttributes();

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isFalse();
        assertThat(tested.getValues("member")).isNull();
    }

    @Test
    public void testLoop() throws Exception {
        Attributes attributes = createAttributes("member", new RangeOption(0, 10));

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isTrue();
        assertThat(tested.getValues("member")).hasSize(11);

        attributes = createAttributes("member", new RangeOption(11), 5);

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isFalse();
        assertThat(tested.getValues("member")).hasSize(16);
    }

    @Test
    public void test1LoopWithPageSizeExact() throws Exception {
        tested = new DefaultIncrementalAttributesMapper(10, "member");

        Attributes attributes = createAttributes("member", new RangeOption(0, 10));

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isFalse();
        assertThat(tested.getValues("member")).hasSize(11);
    }

    @Test
    public void test2LoopsWithPageSizeExact() throws Exception {
        tested = new DefaultIncrementalAttributesMapper(20, "member");

        Attributes attributes = createAttributes("member", new RangeOption(0, 10));

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isTrue();
        assertThat(tested.getValues("member")).hasSize(11);

        attributes = createAttributes("member", new RangeOption(11, 30));

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isFalse();
        assertThat(tested.getValues("member")).hasSize(31);
    }

    @Test
    public void test2LoopsWithPageSize() throws Exception {
        tested = new DefaultIncrementalAttributesMapper(20, "member");

        Attributes attributes = createAttributes("member", new RangeOption(0, 10));

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isTrue();
        assertThat(tested.getValues("member")).hasSize(11);

        attributes = createAttributes("member", new RangeOption(11), 5);

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isFalse();
        assertThat(tested.getValues("member")).hasSize(16);
    }

    @Test
    public void testLoopWithTwoRangedAttributesLoopOnOneAttribute() throws Exception {
        tested = new DefaultIncrementalAttributesMapper(10, new String[]{"member", "cn"});

        Attributes attributes = createAttributes("member", new RangeOption(0, 5));
        attributes.put(createRangeAttribute("cn", new RangeOption(0, 10), 10));

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isTrue();
        assertThat(tested.getValues("member")).hasSize(6);
        assertThat(tested.getValues("cn")).hasSize(10);

        assertThat(tested.getAttributesForLookup().length).isEqualTo(1);

        attributes = createAttributes("member", new RangeOption(6), 5);

        tested.mapFromAttributes(attributes);

        assertThat(tested.hasMore()).isFalse();
        assertThat(tested.getValues("member")).hasSize(11);
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
