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

package org.springframework.ldap.filter;

import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

import com.gargoylesoftware.base.testing.EqualsTester;

import junit.framework.TestCase;

/**
 * @author Adam Skogman
 */
public class AndFilterTest extends TestCase {

    /**
     * Constructor for AndFilterTest.
     * 
     * @param name
     */
    public AndFilterTest(String name) {
        super(name);
    }

    public void testZero() {
        AndFilter aq = new AndFilter();

        assertEquals("", aq.encode());
    }

    public void testOne() {
        AndFilter aq = new AndFilter().and(new EqualsFilter("a", "b"));

        assertEquals("(a=b)", aq.encode());
    }

    public void testTwo() {
        AndFilter aq = new AndFilter().and(new EqualsFilter("a", "b")).and(
                new EqualsFilter("c", "d"));

        assertEquals("(&(a=b)(c=d))", aq.encode());
    }

    public void testThree() {
        AndFilter aq = new AndFilter().and(new EqualsFilter("a", "b")).and(
                new EqualsFilter("c", "d")).and(new EqualsFilter("e", "f"));

        assertEquals("(&(a=b)(c=d)(e=f))", aq.encode());
    }

    public void testEquals() {
        AndFilter originalObject = new AndFilter().and(new EqualsFilter("a",
                "b"));
        AndFilter identicalObject = new AndFilter().and(new EqualsFilter("a",
                "b"));
        AndFilter differentObject = new AndFilter().and(new EqualsFilter("b",
                "b"));
        AndFilter subclassObject = new AndFilter() {
        }.and(new EqualsFilter("a", "b"));

        new EqualsTester(originalObject, identicalObject, differentObject,
                subclassObject);
    }
}
