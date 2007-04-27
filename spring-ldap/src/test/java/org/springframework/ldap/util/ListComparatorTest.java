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
package org.springframework.ldap.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.ldap.support.ListComparator;

import junit.framework.TestCase;

/**
 * Tests for ListComparator.
 * 
 * @author Mattias Arthursson
 */
public class ListComparatorTest extends TestCase {

    private ListComparator tested;

    protected void setUp() throws Exception {
        super.setUp();
        tested = new ListComparator();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCompare_Equals() {
        List list1 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(0) });
        List list2 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(0) });

        int result = tested.compare(list1, list2);
        assertEquals(0, result);
    }

    public void testCompare_Less() {
        List list1 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(0) });
        List list2 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(1) });

        int result = tested.compare(list1, list2);
        assertTrue(result < 0);
    }

    public void testCompare_Greater() {
        List list1 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(1) });
        List list2 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(0) });

        int result = tested.compare(list1, list2);
        assertTrue(result > 0);
    }

    public void testCompare_Longer() {
        List list1 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(0), new Integer(0) });
        List list2 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(0) });

        int result = tested.compare(list1, list2);
        assertTrue(result > 0);
    }

    public void testCompare_Shorter() {
        List list1 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(0) });
        List list2 = Arrays.asList(new Object[] { new Integer(0),
                new Integer(0), new Integer(0) });

        int result = tested.compare(list1, list2);
        assertTrue(result < 0);
    }
}
