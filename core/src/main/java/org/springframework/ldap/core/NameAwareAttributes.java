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

package org.springframework.ldap.core;

import org.springframework.util.Assert;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.HashMap;
import java.util.Map;

/**
 * Used internally to help DirContextAdapter properly handle Names as values.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class NameAwareAttributes implements Attributes {
    private Map<String, NameAwareAttribute> attributes = new HashMap<String, NameAwareAttribute>();

    /**
     * Create an empty instance
     */
    public NameAwareAttributes() {

    }

    /**
     * Create a new instance, populated with the data from the supplied instance.
     * @param attributes the instance to copy.
     */
    public NameAwareAttributes(Attributes attributes) {
        NamingEnumeration<? extends Attribute> allAttributes = attributes.getAll();
        while(allAttributes.hasMoreElements()) {
            Attribute attribute = allAttributes.nextElement();
            put(new NameAwareAttribute(attribute));
        }
    }

    @Override
    public boolean isCaseIgnored() {
        return true;
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public NameAwareAttribute get(String attrID) {
        Assert.hasLength(attrID, "Attribute ID must not be empty");
        return attributes.get(attrID.toLowerCase());
    }

    @Override
    public NamingEnumeration<? extends Attribute> getAll() {
        return new IterableNamingEnumeration<NameAwareAttribute>(attributes.values());
    }

    @Override
    public NamingEnumeration<String> getIDs() {
        return new IterableNamingEnumeration<String>(attributes.keySet());
    }

    @Override
    public Attribute put(String attrID, Object val) {
        Assert.hasLength(attrID, "Attribute ID must not be empty");
        NameAwareAttribute newAttribute = new NameAwareAttribute(attrID, val);
        attributes.put(attrID.toLowerCase(), newAttribute);

        return newAttribute;
    }

    @Override
    public Attribute put(Attribute attr) {
        Assert.notNull(attr, "Attribute must not be null");
        NameAwareAttribute newAttribute = new NameAwareAttribute(attr);
        attributes.put(attr.getID().toLowerCase(), newAttribute);

        return newAttribute;
    }

    @Override
    public Attribute remove(String attrID) {
        Assert.hasLength(attrID, "Attribute ID must not be empty");
        return attributes.remove(attrID);
    }

    @Override
    public Object clone() {
        return new NameAwareAttributes(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NameAwareAttributes that = (NameAwareAttributes) o;

        if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return attributes != null ? attributes.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("NameAwareAttribute; attributes: %s", attributes.toString());
    }
}
