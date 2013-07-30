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

/**
 * IncrementalAttributeMapper Tester.
 *
 * @author Marius Scurtescu
 */
public class RangeOptionTest extends TestCase {
    public RangeOptionTest(String name) {
        super(name);
    }

    public void testConstructorInvalid() {
        try {
            new RangeOption(101, 100);

            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

        try {
            new RangeOption(-1, 100);

            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

        try {
            new RangeOption(-10, 100);

            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }

        try {
            new RangeOption(0, -3);

            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    public void testToString() throws Exception {
        RangeOption range = new RangeOption(0, 100);
        assertEquals("Range=0-100", range.toString());

        range = new RangeOption(0, RangeOption.TERMINAL_END_OF_RANGE);
        assertEquals("Range=0-*", range.toString());

        range = new RangeOption(0, RangeOption.TERMINAL_MISSING);
        assertEquals("Range=0", range.toString());
    }

    public void testParse() throws Exception {
        RangeOption range = RangeOption.parse("Range=0-100");
        assertEquals(0, range.getInitial());
        assertEquals(100, range.getTerminal());

        range = RangeOption.parse("range=0-100");
        assertEquals(0, range.getInitial());
        assertEquals(100, range.getTerminal());

        range = RangeOption.parse("RANGE=0-100");
        assertEquals(0, range.getInitial());
        assertEquals(100, range.getTerminal());

        range = RangeOption.parse("Range=0-*");
        assertEquals(0, range.getInitial());
        assertEquals(RangeOption.TERMINAL_END_OF_RANGE, range.getTerminal());

        range = RangeOption.parse("Range=10");
        assertEquals(10, range.getInitial());
        assertEquals(RangeOption.TERMINAL_MISSING, range.getTerminal());
    }

    public void testParseInvalid() {
        assertNull(RangeOption.parse("Range=10-"));
        assertNull(RangeOption.parse("Range=10-a"));
        assertNull(RangeOption.parse("lang-en"));
        assertNull(RangeOption.parse("member;Range=10-100"));
        assertNull(RangeOption.parse(";Range=10-100"));
        assertNull(RangeOption.parse("Range=10-100;"));
        assertNull(RangeOption.parse("Range=10-100;lang-de"));
    }

    public void testCompare() {
        RangeOption range1 = RangeOption.parse("Range=10-500");
        RangeOption range2 = RangeOption.parse("Range=10-500");
        assertTrue(range1.compareTo(range2) == 0);
        assertTrue(range2.compareTo(range1) == 0);

        range1 = RangeOption.parse("Range=0-*");
        range2 = RangeOption.parse("Range=0-*");
        assertTrue(range1.compareTo(range2) == 0);
        assertTrue(range2.compareTo(range1) == 0);

        range1 = RangeOption.parse("Range=0");
        range2 = RangeOption.parse("Range=0");
        assertTrue(range1.compareTo(range2) == 0);
        assertTrue(range2.compareTo(range1) == 0);

        range1 = RangeOption.parse("Range=0-101");
        range2 = RangeOption.parse("Range=0-100");
        assertTrue(range1.compareTo(range2) > 0);
        assertTrue(range2.compareTo(range1) < 0);

        range1 = RangeOption.parse("Range=0-*");
        range2 = RangeOption.parse("Range=0-100");
        assertTrue(range1.compareTo(range2) > 0);
        assertTrue(range2.compareTo(range1) < 0);
    }

    public void testCompareInvalid() {
        RangeOption range1 = RangeOption.parse("Range=10-500");
        RangeOption range2 = RangeOption.parse("Range=11-500");

        try {
            assertTrue(range1.compareTo(range2) == 0);

            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
            assertTrue(true);
        }

        range1 = RangeOption.parse("Range=10");
        range2 = RangeOption.parse("Range=10-500");

        try {
            assertTrue(range1.compareTo(range2) == 0);

            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
            assertTrue(true);
        }

        range1 = RangeOption.parse("Range=10-500");
        range2 = RangeOption.parse("Range=10");

        try {
            assertTrue(range1.compareTo(range2) == 0);

            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
            assertTrue(true);
        }
    }

    public void testNext() {
        RangeOption range = RangeOption.parse("Range=0-100");

        range = range.nextRange(100);
        assertEquals(101, range.getInitial());
        assertEquals(200, range.getTerminal());

        range = range.nextRange(10);
        assertEquals(201, range.getInitial());
        assertEquals(210, range.getTerminal());

        range = range.nextRange(RangeOption.TERMINAL_END_OF_RANGE);
        assertEquals(211, range.getInitial());
        assertEquals(RangeOption.TERMINAL_END_OF_RANGE, range.getTerminal());
    }
}
