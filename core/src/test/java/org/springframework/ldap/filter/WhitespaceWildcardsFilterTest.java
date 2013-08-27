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

package org.springframework.ldap.filter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the WhitespaceWildcardsFilter class.
 * 
 * @author Adam Skogman
 */
public class WhitespaceWildcardsFilterTest {

    @Test
    public void testEncodeValue_blank() {

        // blank
        assertEquals("*", new WhitespaceWildcardsFilter("", null)
                .getEncodedValue());
        assertEquals("*", new WhitespaceWildcardsFilter("", " ")
                .getEncodedValue());
        assertEquals("*", new WhitespaceWildcardsFilter("", "  ")
                .getEncodedValue());
        assertEquals("*", new WhitespaceWildcardsFilter("", "\t")
                .getEncodedValue());

    }

    @Test
    public void testEncodeValue_normal() {

        assertEquals("*foo*", new WhitespaceWildcardsFilter("", "foo")
                .getEncodedValue());
        assertEquals("*foo*bar*", new WhitespaceWildcardsFilter("", "foo bar")
                .getEncodedValue());
        assertEquals("*foo*bar*",
                new WhitespaceWildcardsFilter("", " foo bar ")
                        .getEncodedValue());
        assertEquals("*foo*bar*", new WhitespaceWildcardsFilter("",
                " \t foo \n bar \r ").getEncodedValue());
    }

    @Test
    public void testEncodeValue_escape() {

        assertEquals("*\\28\\2a\\29*", new WhitespaceWildcardsFilter("", "(*)")
                .getEncodedValue());
        assertEquals("*\\2a*", new WhitespaceWildcardsFilter("", "*")
                .getEncodedValue());
        assertEquals("*\\5c*", new WhitespaceWildcardsFilter("", " \\ ")
                .getEncodedValue());

    }
}
