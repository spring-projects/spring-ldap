/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ldap.support;

import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.util.Assert;

import javax.xml.bind.DatatypeConverter;

/**
 * Helper class to encode and decode ldap names and values.
 * 
 * @author Adam Skogman
 * @author Mattias Hellborg Arthursson
 * @author Thomas Darimont
 */
public final class LdapEncoder {

    private static final int HEX = 16;
    private static String[] NAME_ESCAPE_TABLE = new String[96];

    private static String[] FILTER_ESCAPE_TABLE = new String['\\' + 1];

    private static final int RFC2849_MAX_BASE64_CHARS_PER_LINE = 76;
    
    static {

        // Name encoding table -------------------------------------

        // all below 0x20 (control chars)
        for (char c = 0; c < ' '; c++) {
            NAME_ESCAPE_TABLE[c] = "\\" + toTwoCharHex(c);
        }

        NAME_ESCAPE_TABLE['#'] = "\\#";
        NAME_ESCAPE_TABLE[','] = "\\,";
        NAME_ESCAPE_TABLE[';'] = "\\;";
        NAME_ESCAPE_TABLE['='] = "\\=";
        NAME_ESCAPE_TABLE['+'] = "\\+";
        NAME_ESCAPE_TABLE['<'] = "\\<";
        NAME_ESCAPE_TABLE['>'] = "\\>";
        NAME_ESCAPE_TABLE['\"'] = "\\\"";
        NAME_ESCAPE_TABLE['\\'] = "\\\\";

        // Filter encoding table -------------------------------------

        // fill with char itself
        for (char c = 0; c < FILTER_ESCAPE_TABLE.length; c++) {
            FILTER_ESCAPE_TABLE[c] = String.valueOf(c);
        }

        // escapes (RFC2254)
        FILTER_ESCAPE_TABLE['*'] = "\\2a";
        FILTER_ESCAPE_TABLE['('] = "\\28";
        FILTER_ESCAPE_TABLE[')'] = "\\29";
        FILTER_ESCAPE_TABLE['\\'] = "\\5c";
        FILTER_ESCAPE_TABLE[0] = "\\00";

    }

    /**
     * All static methods - not to be instantiated.
     */
    private LdapEncoder() {
    }

    protected static String toTwoCharHex(char c) {

        String raw = Integer.toHexString(c).toUpperCase();

        if (raw.length() > 1) {
            return raw;
        } else {
            return "0" + raw;
        }
    }

    /**
     * Escape a value for use in a filter.
     * 
     * @param value
     *            the value to escape.
     * @return a properly escaped representation of the supplied value.
     */
    public static String filterEncode(String value) {

        if (value == null)
            return null;

        // make buffer roomy
        StringBuilder encodedValue = new StringBuilder(value.length() * 2);

        int length = value.length();

        for (int i = 0; i < length; i++) {

            char c = value.charAt(i);

            if (c < FILTER_ESCAPE_TABLE.length) {
                encodedValue.append(FILTER_ESCAPE_TABLE[c]);
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
     * <br>Escapes:<br> ' ' [space] - "\ " [if first or last] <br> '#'
     * [hash] - "\#" <br> ',' [comma] - "\," <br> ';' [semicolon] - "\;" <br> '=
     * [equals] - "\=" <br> '+' [plus] - "\+" <br> '&lt;' [less than] -
     * "\&lt;" <br> '&gt;' [greater than] - "\&gt;" <br> '"' [double quote] -
     * "\"" <br> '\' [backslash] - "\\" <br>
     * 
     * @param value
     *            the value to escape.
     * @return The escaped value.
     */
    public static String nameEncode(String value) {

        if (value == null)
            return null;

        // make buffer roomy
        StringBuilder encodedValue = new StringBuilder(value.length() * 2);

        int length = value.length();
        int last = length - 1;

        for (int i = 0; i < length; i++) {

            char c = value.charAt(i);

            // space first or last
            if (c == ' ' && (i == 0 || i == last)) {
                encodedValue.append("\\ ");
                continue;
            }

            if (c < NAME_ESCAPE_TABLE.length) {
                // check in table for escapes
                String esc = NAME_ESCAPE_TABLE[c];

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
        StringBuilder decoded = new StringBuilder(value.length());

        int i = 0;
        while (i < value.length()) {
            char currentChar = value.charAt(i);
            if (currentChar == '\\') {
                if (value.length() <= i + 1) {
                    // Ending with a single backslash is not allowed
                    throw new BadLdapGrammarException(
                            "Unexpected end of value " + "unterminated '\\'");
                } else {
                    char nextChar = value.charAt(i + 1);
                    if (nextChar == ',' || nextChar == '=' || nextChar == '+'
                            || nextChar == '<' || nextChar == '>'
                            || nextChar == '#' || nextChar == ';'
                            || nextChar == '\\' || nextChar == '\"'
                            || nextChar == ' ') {
                        // Normal backslash escape
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
                                    HEX));
                            i += 3;
                        }
                    }
                }
            } else {
                // This character wasn't escaped - just append it
                decoded.append(currentChar);
                i++;
            }
        }

        return decoded.toString();

    }

    /**
     * Converts an array of bytes into a Base64 encoded string according to the rules for converting LDAP Attributes in RFC2849.
     *
     * @param val
     * @return
     *   A string containing a lexical representation of base64Binary wrapped around 76 characters.
     * @throws IllegalArgumentException if <tt>val</tt> is null.
     */
    public static String printBase64Binary(byte[] val) {

        Assert.notNull(val, "val must not be null!");

        String encoded = DatatypeConverter.printBase64Binary(val);

        int length = encoded.length();
        StringBuilder sb = new StringBuilder(length + length / RFC2849_MAX_BASE64_CHARS_PER_LINE);

        for (int i = 0, len = length; i < len; i++) {
            sb.append(encoded.charAt(i));

            if ((i + 1) % RFC2849_MAX_BASE64_CHARS_PER_LINE == 0) {
                sb.append('\n');
                sb.append(' ');
            }
        }

        return sb.toString();
    }

    /**
     * Converts the Base64 encoded string argument into an array of bytes.
     *
     * @param val
     * @return
     *   An array of bytes represented by the string argument.
     * @throws IllegalArgumentException if <tt>val</tt> is null or does not conform to lexical value space defined in XML Schema Part 2: Datatypes for xsd:base64Binary.
     */
    public static byte[] parseBase64Binary(String val) {

        Assert.notNull(val, "val must not be null!");

        int length = val.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0, len = length; i < len; i++) {

            char c = val.charAt(i);

            if(c == '\n'){
                if(i + 1 < len && val.charAt(i + 1) == ' ') {
                    i++;
                }
                continue;
            }

            sb.append(c);
        }

        return DatatypeConverter.parseBase64Binary(sb.toString());
    }
}
