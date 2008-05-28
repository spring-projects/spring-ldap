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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.ldap.support.ListComparator;

/**
 * Datatype for a LDAP name, a part of a path.
 * 
 * The name: uid=adam.skogman Key: uid Value: adam.skogman
 * 
 * @author Adam Skogman
 * @author Mattias Arthursson
 */
public class LdapRdn implements Serializable, Comparable {
    private static final long serialVersionUID = 5681397547245228750L;

    private List components = new LinkedList();

    /**
     * Default constructor. Create an empty, uninitialized LdapRdn.
     */
    public LdapRdn() {
    }

    /**
     * Parse the supplied string and construct this instance accordingly.
     * 
     * @param string
     *            the string to parse.
     */
    public LdapRdn(String string) {
        DnParser parser = DefaultDnParserFactory.createDnParser(string);
        LdapRdn rdn;
        try {
            rdn = parser.rdn();
        } catch (ParseException e) {
            throw new BadLdapGrammarException("Failed to parse Rdn", e);
        } catch (TokenMgrError e) {
            throw new BadLdapGrammarException("Failed to parse Rdn", e);
        }
        this.components = rdn.components;
    }

    /**
     * Construct an LdapRdn using the supplied key and value.
     * 
     * @param key
     *            the attribute name.
     * @param value
     *            the attribute value.
     */
    public LdapRdn(String key, String value) {
        components.add(new LdapRdnComponent(key, value));
    }

    /**
     * Add an LdapRdnComponent to this LdapRdn.
     * 
     * @param rdnComponent
     *            the LdapRdnComponent to add.s
     */
    public void addComponent(LdapRdnComponent rdnComponent) {
        components.add(rdnComponent);
    }

    /**
     * Gets all components in this LdapRdn.
     * 
     * @return the List of all LdapRdnComponents composing this LdapRdn.
     */
    public List getComponents() {
        return components;
    }

    /**
     * Gets the first LdapRdnComponent of this LdapRdn.
     * 
     * @return The first LdapRdnComponent of this LdapRdn.
     * @throws IndexOutOfBoundsException
     *             if there are no components in this Rdn.
     */
    public LdapRdnComponent getComponent() {
        return (LdapRdnComponent) components.get(0);
    }

    /**
     * Get the LdapRdnComponent at index <code>idx</code>.
     * 
     * @param idx
     *            the 0-based index of the component to get.
     * @return the LdapRdnComponent at index <code>idx</code>.
     * @throws IndexOutOfBoundsException
     *             if there are no components in this Rdn.
     */
    public LdapRdnComponent getComponent(int idx) {
        return (LdapRdnComponent) components.get(idx);
    }

    /**
     * Get a properly rfc2253-encoded String representation of this LdapRdn.
     * 
     * @return an escaped String corresponding to this LdapRdn.
     * @throws IndexOutOfBoundsException
     *             if there are no components in this Rdn.
     */
    public String getLdapEncoded() {
        if (components.size() == 0) {
            throw new IndexOutOfBoundsException("No components in Rdn.");
        }
        StringBuffer sb = new StringBuffer(100);
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            LdapRdnComponent component = (LdapRdnComponent) iter.next();
            sb.append(component.encodeLdap());
            if (iter.hasNext()) {
                sb.append("+");
            }
        }

        return sb.toString();
    }

    /**
     * Get a String representation of this LdapRdn for use in urls.
     * 
     * @return a String representation of this LdapRdn for use in urls.
     */
    public String encodeUrl() {
        StringBuffer sb = new StringBuffer(100);
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            LdapRdnComponent component = (LdapRdnComponent) iter.next();
            sb.append(component.encodeUrl());
            if (iter.hasNext()) {
                sb.append("+");
            }
        }

        return sb.toString();
    }

    /**
     * Compare this LdapRdn to another object.
     * 
     * @param obj
     *            the object to compare to.
     * @throws ClassCastException
     *             if the supplied object is not an LdapRdn instance.
     */
    public int compareTo(Object obj) {
        LdapRdn that = (LdapRdn) obj;
        Comparator comparator = new ListComparator();
        return comparator.compare(this.components, that.components);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        LdapRdn that = (LdapRdn) obj;
        return this.getComponents().equals(that.getComponents());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.getClass().hashCode() ^ getComponents().hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getLdapEncoded();
    }

    /**
     * Get the value of this LdapRdn. Note that if this Rdn is multi-value the
     * first value will be returned. E.g. for the Rdn
     * <code>cn=john doe+sn=doe</code>, the return value would be
     * <code>john doe</code>.
     * 
     * @return the (first) value of this LdapRdn.
     * @throws IndexOutOfBoundsException
     *             if there are no components in this Rdn.
     */
    public String getValue() {
        return getComponent().getValue();
    }

    /**
     * Get the key of this LdapRdn. Note that if this Rdn is multi-value the
     * first key will be returned. E.g. for the Rdn
     * <code>cn=john doe+sn=doe</code>, the return value would be
     * <code>cn</code>.
     * 
     * @return the (first) key of this LdapRdn.
     * @throws IndexOutOfBoundsException
     *             if there are no components in this Rdn.
     */
    public String getKey() {
        return getComponent().getKey();
    }

    /**
     * Get the value of the LdapComponent with the specified key (Attribute
     * name).
     * 
     * @param key
     *            the key
     * @return the value.
     * @throws IllegalArgumentException
     *             if there is no component with the specified key.
     */
    public String getValue(String key) {
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            LdapRdnComponent component = (LdapRdnComponent) iter.next();
            if (StringUtils.equals(component.getKey(), key)) {
                return component.getValue();
            }
        }

        throw new IllegalArgumentException("No RdnComponent with the key "
                + key);
    }
}