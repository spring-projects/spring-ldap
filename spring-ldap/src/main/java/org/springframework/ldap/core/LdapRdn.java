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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
     * Default constructor.
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
     * @param value
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
     *             if no components have been added.
     */
    public LdapRdnComponent getComponent() {
        return (LdapRdnComponent) components.get(0);
    }

    /**
     * Get the LdapRdnComponent at index <code>idx</code>.
     * 
     * @param idx
     *            the 0-based index of the component to get.
     * @return the LdapRdnComponent at indet <code>idx</code>.
     * @throws IndexOutOfBoundsException
     *             if no component exists at index <code>idx</code>.
     */
    public LdapRdnComponent getComponent(int idx) {
        return (LdapRdnComponent) components.get(idx);
    }

    /**
     * Get a properly rfc2253-encoded String representation to this LdapRdn.
     * 
     * @return an encoded String corresponding to this LdapRdn.
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
}