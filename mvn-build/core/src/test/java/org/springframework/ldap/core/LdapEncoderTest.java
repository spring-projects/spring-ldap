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

import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.ldap.core.LdapEncoder;

import junit.framework.TestCase;

/**
 * Unit test for the LdapEncode class.
 * 
 * @author Adam Skogman
 */
public class LdapEncoderTest extends TestCase {

    /**
     * Constructor for LdapEncoderTest.
     * 
     * @param name
     */
    public LdapEncoderTest(String name) {
        super(name);
    }

    public void testFilterEncode() {
        String correct = "\\2aa\\2ab\\28c\\29d\\2a\\5c";
        assertEquals(correct, LdapEncoder.filterEncode("*a*b(c)d*\\"));

    }

    public void testNameEncode() {

        String res = LdapEncoder.nameEncode("# foo ,+\"\\<>; ");

        assertEquals("\\# foo \\,\\+\\\"\\\\\\<\\>\\;\\ ", res);
    }

    public void testNameDecode() {

        String res = (String) LdapEncoder
                .nameDecode("\\# foo \\,\\+\\\"\\\\\\<\\>\\;\\ ");

        assertEquals("# foo ,+\"\\<>; ", res);
    }

    public void testNameDecode_slashlast() {

        try {
            LdapEncoder.nameDecode("\\");
            fail("Should throw BadLdapGrammarException");
        } catch (BadLdapGrammarException e) {
            assertTrue(true);
        }
    }

}
