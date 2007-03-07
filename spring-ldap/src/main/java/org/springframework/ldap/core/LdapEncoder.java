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

package org.springframework.ldap.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.BadLdapGrammarException;

/**
 * Helper class to encode and decode ldap names and values.
 * 
 * @author Adam Skogman
 * @author Mattias Arthursson
 */
public class LdapEncoder {

    static private String[] nameEscapeTable = new String[96];

    static private String[] filterEscapeTable = new String['\\' + 1];

    /**
     * Pattern for matching escaped ldap name values.
     * 
     * Double escaping: \ -> \\ (in pattern) -> \\\\ (in java string literal)
     * 
     * Group 1: Hex escapes = \XX -> \p{XDigit}{2} Group 2: Ordinary escapes =
     * \x -> \. Group 3: Anything but \ [^\\]
     * 
     * Note that the \ is not part of the match.
     */
    static private final Pattern VALUE_DECODE_PATTERN = Pattern
            .compile("(?:\\\\(\\p{XDigit}{2}))|(?:\\\\(.))|([^\\\\])");

    static {

        // Name encoding table -------------------------------------

        // all below 0x20 (control chars)
        for (char c = 0; c < ' '; c++) {
            nameEscapeTable[c] = "\\" + toTwoCharHex(c);
        }

        nameEscapeTable['#'] = "\\#";
        nameEscapeTable[','] = "\\,";
        nameEscapeTable[';'] = "\\;";
        nameEscapeTable['='] = "\\=";
        nameEscapeTable['+'] = "\\+";
        nameEscapeTable['<'] = "\\<";
        nameEscapeTable['>'] = "\\>";
        // nameEscapeTable['\''] = "\\";
        nameEscapeTable['\"'] = "\\\"";
        // nameEscapeTable['/'] = "\\" + toTwoCharHex('/');
        nameEscapeTable['\\'] = "\\\\";

        // Filter encoding table -------------------------------------

        // fill with char itself
        for (char c = 0; c < filterEscapeTable.length; c++) {
            filterEscapeTable[c] = String.valueOf(c);
        }

        // escapes (RFC2254)
        filterEscapeTable['*'] = "\\2a";
        filterEscapeTable['('] = "\\28";
        filterEscapeTable[')'] = "\\29";
        filterEscapeTable['\\'] = "\\5c";
        filterEscapeTable[0] = "\\00";

    }

    static protected String toTwoCharHex(char c) {

        String raw = Integer.toHexString(c).toUpperCase();

        if (raw.length() > 1)
            return raw;
        else
            return "0" + raw;
    }

    /**
     * All static methods
     */
    private LdapEncoder() {
    }

    static public String filterEncode(String value) {

        if (value == null)
            return null;

        // make buffer roomy
        StringBuffer encodedValue = new StringBuffer(value.length() * 2);

        int length = value.length();

        for (int i = 0; i < length; i++) {

            char c = value.charAt(i);

            if (c < filterEscapeTable.length) {
                encodedValue.append(filterEscapeTable[c]);
            } else {
                // default: add the char
                encodedValue.append(c);
            }
        }

        return encodedValue.toString();
    }

    /**
     * LDAP Encodes a value for use with a DN. Escapes for LDAP, not JNDI!
     * 
     * <br/>Escapes:<br/> ' ' [space] - "\ " [if first or last] <br/> '#'
     * [hash] - "\#" <br/> ',' [comma] - "\," <br/> ';' [semicolon] - "\;" <br/> '=
     * [equals] - "\=" <br/> '+' [plus] - "\+" <br/> '&lt;' [less than] -
     * "\&lt;" <br/> '&gt;' [greater than] - "\&gt;" <br/> '"' [double quote] -
     * "\"" <br/> '\' [backslash] - "\\" <br/>
     * 
     * @param value
     * @return The escaped value
     */
    static public String nameEncode(String value) {

        if (value == null)
            return null;

        // make buffer roomy
        StringBuffer encodedValue = new StringBuffer(value.length() * 2);

        int length = value.length();
        int last = length - 1;

        for (int i = 0; i < length; i++) {

            char c = value.charAt(i);

            // space first or last
            if (c == ' ' && (i == 0 || i == last)) {
                encodedValue.append("\\ ");
                continue;
            }

            if (c < nameEscapeTable.length) {
                // check in table for escapes
                String esc = nameEscapeTable[c];

                if (esc != null) {
                    encodedValue.append(esc);
                    continue;
                }
            }

            // default: add the char
            encodedValue.append(c);
        }

        return encodedValue.toString();

    }

    /**
     * Decodes a value. Converts escaped chars to ordinary chars.
     * 
     * @param value
     *            Trimmed value, so no leading an trailing blanks, except an
     *            escaped space last.
     * @return The decoded value as a string.
     * @throws BadLdapGrammarException
     */
    static public String nameDecode(String value)
            throws BadLdapGrammarException {

        if (value == null)
            return null;

        // make buffer same size
        StringBuffer decoded = new StringBuffer(value.length());

        int i = 0;
        while (i < value.length()) {
            char currentChar = value.charAt(i);
            if (currentChar == '\\') {
                if (value.length() <= i + 1) {
                    throw new BadLdapGrammarException(
                            "Unexpected end of value " + "unterminated '\\'");
                } else {
                    char nextChar = value.charAt(i + 1);
                    if (nextChar == ',' | nextChar == '=' | nextChar == '+'
                            | nextChar == '<' | nextChar == '>'
                            | nextChar == '#' | nextChar == ';'
                            | nextChar == '\\' | nextChar == '\"'
                            | nextChar == ' ') {
                        decoded.append(nextChar);
                        i += 2;
                    } else {
                        if (value.length() <= i + 2) {
                            throw new BadLdapGrammarException(
                                    "Unexpected end of value "
                                            + "expected special or hex, found '"
                                            + nextChar + "'");
                        } else {
                            // This should be a hex value
                            String hexString = "" + nextChar
                                    + value.charAt(i + 2);
                            decoded.append((char) Integer.parseInt(hexString,
                                    16));
                            i += 3;
                        }
                    }
                }
            } else {
                decoded.append(currentChar);
                i++;
            }
        }

        return decoded.toString();

    }
}
