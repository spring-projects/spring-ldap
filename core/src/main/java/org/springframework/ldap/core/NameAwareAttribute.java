/*
 * Copyright 2005-2013 the original author or authors.
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

import org.springframework.ldap.InvalidNameException;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Used internally to make DirContextAdapter properly handle Names as values.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public final class NameAwareAttribute implements Attribute, Iterable<Object> {

	private final String id;

	private final boolean orderMatters;

	private final Set<Object> values = new LinkedHashSet<Object>();

	private Map<Name, String> valuesAsNames = new HashMap<Name, String>();

	/**
	 * Construct a new instance with the specified id and one value.
	 * @param id the attribute id
	 * @param value the value to start off with
	 */
	public NameAwareAttribute(String id, Object value) {
		this(id);
		this.values.add(value);
	}

	/**
	 * Construct a new instance from the supplied Attribute.
	 * @param attribute the Attribute to copy.
	 */
	public NameAwareAttribute(Attribute attribute) {
		this(attribute.getID(), attribute.isOrdered());
		try {
			NamingEnumeration<?> incomingValues = attribute.getAll();
			while (incomingValues.hasMore()) {
				this.add(incomingValues.next());
			}
		}
		catch (NamingException e) {
			throw LdapUtils.convertLdapException(e);
		}

		if (attribute instanceof NameAwareAttribute) {
			NameAwareAttribute nameAwareAttribute = (NameAwareAttribute) attribute;
			populateValuesAsNames(nameAwareAttribute, this);
		}
	}

	/**
	 * Construct a new instance with the specified id and no values.
	 * @param id the attribute id
	 */
	public NameAwareAttribute(String id) {
		this(id, false);
	}

	/**
	 * Construct a new instance with the specified id, no values and order significance as
	 * specified.
	 * @param id the attribute id
	 * @param orderMatters whether order has significance in this attribute.
	 */
	public NameAwareAttribute(String id, boolean orderMatters) {
		this.id = id;
		this.orderMatters = orderMatters;
	}

	@Override
	public NamingEnumeration<?> getAll() {
		return new IterableNamingEnumeration<Object>(this.values);
	}

	@Override
	public Object get() {
		if (this.values.isEmpty()) {
			return null;
		}

		return this.values.iterator().next();
	}

	@Override
	public int size() {
		return this.values.size();
	}

	@Override
	public String getID() {
		return this.id;
	}

	@Override
	public boolean contains(Object attrVal) {
		return this.values.contains(attrVal);
	}

	@Override
	public boolean add(Object attrVal) {
		if (attrVal instanceof Name) {
			initValuesAsNames();

			Name name = LdapUtils.newLdapName((Name) attrVal);
			String currentValue = this.valuesAsNames.get(name);
			String nameAsString = name.toString();
			if (currentValue == null) {
				this.valuesAsNames.put(name, name.toString());
				this.values.add(nameAsString);
				return true;
			}
			else {
				if (!currentValue.equals(nameAsString)) {
					this.values.remove(currentValue);
					this.values.add(nameAsString);
				}

				return false;
			}
		}

		return this.values.add(attrVal);
	}

	public void initValuesAsNames() {
		if (hasValuesAsNames()) {
			return;
		}

		Map<Name, String> newValuesAsNames = new HashMap<Name, String>();
		for (Object value : this.values) {
			if (value instanceof String) {
				String s = (String) value;
				try {
					newValuesAsNames.put(LdapUtils.newLdapName(s), s);
				}
				catch (InvalidNameException e) {
					throw new IllegalArgumentException(
							"This instance has values that are not valid distinguished names; "
									+ "cannot handle Name values",
							e);
				}
			}
			else if (value instanceof LdapName) {
				newValuesAsNames.put((LdapName) value, value.toString());
			}
			else {
				throw new IllegalArgumentException(
						"This instance has non-string attribute values; " + "cannot handle Name values");
			}
		}

		this.valuesAsNames = newValuesAsNames;
	}

	public boolean hasValuesAsNames() {
		return !this.valuesAsNames.isEmpty();
	}

	@Override
	public boolean remove(Object attrval) {
		if (attrval instanceof Name) {
			initValuesAsNames();

			Name name = LdapUtils.newLdapName((Name) attrval);
			String removedValue = this.valuesAsNames.remove(name);
			if (removedValue != null) {
				this.values.remove(removedValue);

				return true;
			}

			return false;
		}
		return this.values.remove(attrval);
	}

	@Override
	public void clear() {
		this.values.clear();
	}

	@Override
	public DirContext getAttributeSyntaxDefinition() throws NamingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DirContext getAttributeDefinition() throws NamingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOrdered() {
		return this.orderMatters;
	}

	/**
	 * <p>
	 * Due to performance reasons it is not advised to iterate over the attribute's values
	 * using this method. Please use the {@link #iterator()} instead.
	 * </p>
	 * {@inheritDoc}
	 */
	@Override
	public Object get(int ix) throws NamingException {
		Iterator<Object> iterator = this.values.iterator();

		try {
			Object value = iterator.next();
			for (int i = 0; i < ix; i++) {
				value = iterator.next();
			}

			return value;
		}
		catch (NoSuchElementException e) {
			throw new IndexOutOfBoundsException("No value at index i");
		}
	}

	@Override
	public Object remove(int ix) {
		Iterator<Object> iterator = this.values.iterator();

		try {
			Object value = iterator.next();
			for (int i = 0; i < ix; i++) {
				value = iterator.next();
			}
			iterator.remove();
			if (value instanceof String) {
				try {
					this.valuesAsNames.remove(new LdapName((String) value));
				}
				catch (javax.naming.InvalidNameException ignored) {
				}
			}
			return value;
		}
		catch (NoSuchElementException e) {
			throw new IndexOutOfBoundsException("No value at index i");
		}
	}

	@Override
	public void add(int ix, Object attrVal) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object set(int ix, Object attrVal) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object clone() {
		return new NameAwareAttribute(this);
	}

	private void populateValuesAsNames(NameAwareAttribute from, NameAwareAttribute to) {
		Set<Map.Entry<Name, String>> entries = from.valuesAsNames.entrySet();
		for (Map.Entry<Name, String> entry : entries) {
			to.valuesAsNames.put(LdapUtils.newLdapName(entry.getKey()), entry.getValue());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		NameAwareAttribute that = (NameAwareAttribute) o;

		if (this.id != null ? !this.id.equals(that.id) : that.id != null)
			return false;
		if (this.values.size() != that.values.size()) {
			return false;
		}

		if (this.orderMatters != that.orderMatters || this.size() != that.size()) {
			return false;
		}

		if (this.hasValuesAsNames() != that.hasValuesAsNames()) {
			return false;
		}

		Set<?> myValues = this.values;
		Set<?> theirValues = that.values;
		if (this.hasValuesAsNames()) {
			// We have Name values - compare these to get
			// syntactically correct comparison of the values

			myValues = this.valuesAsNames.keySet();
			theirValues = that.valuesAsNames.keySet();
		}

		if (this.orderMatters) {
			Iterator<?> thisIterator = myValues.iterator();
			Iterator<?> thatIterator = theirValues.iterator();
			while (thisIterator.hasNext()) {
				if (!ObjectUtils.nullSafeEquals(thisIterator.next(), thatIterator.next())) {
					return false;
				}
			}

			return true;
		}
		else {
			for (Object value : myValues) {
				if (!CollectionUtils.contains(theirValues.iterator(), value)) {
					return false;
				}
			}

			return true;
		}
	}

	@Override
	public int hashCode() {
		int result = this.id != null ? this.id.hashCode() : 0;

		int valuesHash = 7;
		Set<?> myValues = this.values;
		if (hasValuesAsNames()) {
			myValues = this.valuesAsNames.keySet();
		}

		for (Object value : myValues) {
			result += ObjectUtils.nullSafeHashCode(value);
		}
		result = 31 * result + valuesHash;

		return result;
	}

	@Override
	public String toString() {
		return String.format("NameAwareAttribute; id: %s; hasValuesAsNames: %s; orderMatters: %s; values: %s", this.id,
				hasValuesAsNames(), this.orderMatters, this.values);
	}

	@Override
	public Iterator<Object> iterator() {
		return this.values.iterator();
	}

}
