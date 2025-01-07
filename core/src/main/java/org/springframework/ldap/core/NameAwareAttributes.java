/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ldap.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Used internally to help DirContextAdapter properly handle Names as values.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public final class NameAwareAttributes implements Attributes, Iterable<NameAwareAttribute> {

	private Map<String, NameAwareAttribute> attributes = new HashMap<>();

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
		while (allAttributes.hasMoreElements()) {
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
		return this.attributes.size();
	}

	@Override
	public NameAwareAttribute get(String attrID) {
		Assert.hasLength(attrID, "Attribute ID must not be empty");
		return this.attributes.get(attrID.toLowerCase(Locale.ROOT));
	}

	@Override
	public NamingEnumeration<NameAwareAttribute> getAll() {
		return new IterableNamingEnumeration<>(this.attributes.values());
	}

	@Override
	public NamingEnumeration<String> getIDs() {
		return new IterableNamingEnumeration<>(this.attributes.keySet());
	}

	/**
	 * @inheritDoc
	 *
	 * @since 3.3
	 */
	@NonNull
	@Override
	public Iterator<NameAwareAttribute> iterator() {
		return this.attributes.values().iterator();
	}

	@Override
	public Attribute put(String attrID, Object val) {
		Assert.hasLength(attrID, "Attribute ID must not be empty");
		NameAwareAttribute newAttribute = new NameAwareAttribute(attrID, val);
		this.attributes.put(attrID.toLowerCase(Locale.ROOT), newAttribute);

		return newAttribute;
	}

	@Override
	public Attribute put(Attribute attr) {
		Assert.notNull(attr, "Attribute must not be null");
		NameAwareAttribute newAttribute = new NameAwareAttribute(attr);
		this.attributes.put(attr.getID().toLowerCase(Locale.ROOT), newAttribute);

		return newAttribute;
	}

	@Override
	public Attribute remove(String attrID) {
		Assert.hasLength(attrID, "Attribute ID must not be empty");
		return this.attributes.remove(attrID.toLowerCase(Locale.ROOT));
	}

	@Override
	public Object clone() {
		return new NameAwareAttributes(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		NameAwareAttributes that = (NameAwareAttributes) o;

		if ((this.attributes != null) ? !this.attributes.equals(that.attributes) : that.attributes != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return (this.attributes != null) ? this.attributes.hashCode() : 0;
	}

	@Override
	public String toString() {
		return String.format("NameAwareAttribute; attributes: %s", this.attributes.toString());
	}

}
