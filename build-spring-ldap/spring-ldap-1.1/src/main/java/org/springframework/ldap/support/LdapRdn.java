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

package org.springframework.ldap.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.BadLdapGrammarException;

/**
 * Datatype for a LDAP name, a part of a path.
 * 
 * The name: uid=adam.skogman Key: uid Value: adam.skogman
 * 
 * @author Adam Skogman
 */
public class LdapRdn {

    private String key;

    private String value;

    private String ldapEncoded;

    protected static final Pattern RDN_PATTERN = Pattern
            .compile("\\s*(\\p{Alnum}+)\\s*=\\s*((\\\\\\p{XDigit}{2}|\\\\.|[^\\\\])+?)\\s*");

    private static final Log log = LogFactory.getLog(LdapRdn.class);
    
    /**
     * Constructs a RDN from an ldap encoded rdn "foo=bar".
     * 
     * The key will be forced to lowercase. The value is kept as is.
     * 
     * The ldapEncoded value will not be the same, but rather a reencoded value,
     * so that all looks the same.
     * 
     * @param ldapRdn
     * @throws BadLdapGrammarException
     *             If the rdn could not be parsed.
     */
    public LdapRdn(String ldapRdn) throws BadLdapGrammarException {

        parseLdap(ldapRdn);

    }

    /**
     * Constructs a RDN from a key and a value.
     * 
     * The key will be forced to lowercase. The value is kept as is.
     * 
     * @param key
     *            Not blank
     * @param value
     *            Not blank
     */
    public LdapRdn(String key, String value) {

        if (StringUtils.isBlank(key))
            throw new IllegalArgumentException("Key may not be blank");

        if (StringUtils.isBlank(value))
            throw new IllegalArgumentException("Value may not be blank");

        this.key = key.toLowerCase();
        this.value = value;

        this.ldapEncoded = encodeLdap();

    }

    /**
     * Encode key and value to ldap
     * 
     * @return The ldap encoded rdn
     */
    protected String encodeLdap() {

        StringBuffer buff = new StringBuffer(key.length() + value.length() * 2);

        buff.append(key);
        buff.append('=');
        buff.append(LdapEncoder.nameEncode(value));

        return buff.toString();
    }

    /**
     * @param rdn
     *            The ldap rdn to parse
     * @throws BadLdapGrammarException if any error occurs parsing the rdn.
     */
    protected void parseLdap(String rdn) throws BadLdapGrammarException {

        Matcher matcher = RDN_PATTERN.matcher(rdn);

        if (!matcher.matches()) {
            throw new BadLdapGrammarException(
                    "Not a proper name (such as key=value): " + rdn);
        }

        String rawKey = matcher.group(1);
        String rawValue = matcher.group(2);

        this.key = StringUtils.lowerCase(rawKey);
        this.value = LdapEncoder.nameDecode(rawValue);

        if (key == null) {
            throw new BadLdapGrammarException("Key empty in rdn '" + rdn + "'");
        }
        if (value == null) {
            throw new BadLdapGrammarException("Value empty in rdn '" + rdn
                    + "'");
        }

        this.ldapEncoded = encodeLdap();

    }

    /**
     * Parse the supplied String for a valid LdapRdn. Swallow any exceptions.
     * 
     * @param rdn
     *            the String to parse.
     * @return an LdapRdn corresponding to the supplied String if the format was
     *         valid, null otherwise.
     */
    public static LdapRdn parse(String rdn) {

        try {
            return new LdapRdn(rdn);
        } catch (BadLdapGrammarException e) {
            return null;
        }

    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    /**
     * 
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return ldapEncoded;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // A subclass with identical values should NOT be considered equal.
        // EqualsBuilder in commons-lang cannot handle subclasses correctly.
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        LdapRdn rdn = (LdapRdn) obj;

        return StringUtils.equalsIgnoreCase(rdn.key, this.key)
                && StringUtils.equalsIgnoreCase(rdn.value, this.value);
    }

    public Object encodeUrl() {
        // Use the URI class to properly URL encode the value.
        try {
            URI valueUri = new URI(null, null, value, null);
            return key + "=" + valueUri.toString();
        } catch (URISyntaxException e) {
            // This should really never happen...
            log.error("Failed to URL encode value " + value, e);
            return key + "=" + "value";
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return key.hashCode() ^ value.hashCode();
    }

    /**
     * @return The LdapRdn as a string where the value is LDAP-encoded.
     */
    public String getLdapEncoded() {
        return ldapEncoded;
    }
}