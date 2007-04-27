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

import org.springframework.ldap.filter.LikeFilter;

import junit.framework.TestCase;

/**
 * @author Anders Henja
 */
public class LikeFilterTest extends TestCase {
    /**
     * Constructor for LikeFilterTest.
     * 
     * @param name
     */
    public LikeFilterTest(String name) {
        super(name);
    }

    final public void testEncodeValue_blank() {
        assertEquals("", new LikeFilter("", null).getEncodedValue());
        assertEquals(" ", new LikeFilter("", " ").getEncodedValue());
    }

    final public void testEncodeValue_normal() {
        assertEquals("foo", new LikeFilter("", "foo").getEncodedValue());
        assertEquals("foo*bar", new LikeFilter("", "foo*bar").getEncodedValue());
        assertEquals("*foo*bar*", new LikeFilter("", "*foo*bar*")
                .getEncodedValue());
        assertEquals("**foo**bar**", new LikeFilter("", "**foo**bar**")
                .getEncodedValue());
    }

    final public void testEncodeValue_escape() {
        assertEquals("*\\28*\\29*", new LikeFilter("", "*(*)*")
                .getEncodedValue());
        assertEquals("*\\5c2a*", new LikeFilter("", "*\\2a*").getEncodedValue());
    }

}
