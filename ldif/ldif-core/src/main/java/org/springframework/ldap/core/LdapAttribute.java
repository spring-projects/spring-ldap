/*
 * Copyright 2005-2010 the original author or authors.
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.naming.directory.BasicAttribute;

/**
 * Extends {@link javax.naming.directory.BasicAttribute} to add support for 
 * options as defined in RFC2849.
 * <p>
 * While uncommon, options can be used to specify additional descriptors for 
 * the attribute. Options are backed by a {@link java.util.HashSet} of 
 * {@link java.lang.String}.
 * 
 * @author Keith Barlow
 *
 */
public class LdapAttribute extends BasicAttribute {

	private static final long serialVersionUID = -5263905906016179429L;
	
	/**
	 * Holds the attributes options.
	 */
	protected Set<String> options = new HashSet<String>();
	
	/**
	 * Creates an unordered attribute with the specified ID.
	 * 
	 * @param id {@link java.lang.String}  ID of the attribute.
	 */
	public LdapAttribute(String id) {
		super(id);
	}

	/**
	 * Creates an unordered attribute with the specified ID and value.
	 * 
	 * @param id {@link java.lang.String}  ID of the attribute.
	 * @param value Attribute value.
	 */
	public LdapAttribute(String id, Object value) {
		super(id, value);
	}

	/**
	 * Creates an unordered attribute with the specified ID, value, and options.
	 * 
	 * @param id {@link java.lang.String}  ID of the attribute.
	 * @param value Attribute value.
	 * @param options {@link java.util.Collection} of {@link java.lang.String} attribute options.
	 */
	public LdapAttribute(String id, Object value, Collection<String> options) {
		super(id, value);
		this.options.addAll(options);
	}

	/**
	 * Creates an attribute with the specified ID whose values may be ordered.
	 * 
	 * @param id {@link java.lang.String}  ID of the attribute.
	 * @param ordered boolean indicating whether or not the attributes values are ordered.
	 */
	public LdapAttribute(String id, boolean ordered) {
		super(id, ordered);
	}

	/**
	 * Creates an attribute with the specified ID and options whose values may be ordered.
	 * 
	 * @param id {@link java.lang.String}  ID of the attribute.
	 * @param options {@link java.util.Collection} of {@link java.lang.String} attribute options.
	 * @param ordered boolean indicating whether or not the attributes values are ordered.
	 */
	public LdapAttribute(String id, Collection<String> options, boolean ordered) {
		super(id, ordered);
		this.options.addAll(options);
	}

	/**
	 * Creates an attribute with the specified ID and value whose values may be ordered.
	 * 
	 * @param id {@link java.lang.String}  ID of the attribute.
	 * @param value Attribute value.
	 * @param ordered boolean indicating whether or not the attributes values are ordered.
	 */
	public LdapAttribute(String id, Object value, boolean ordered) {
		super(id, value, ordered);
	}

	/**
	 * Creates an attribute with the specified ID, value, and options whose values may be ordered.
	 * 
	 * @param id {@link java.lang.String} ID of the attribute.
	 * @param value Attribute value.
	 * @param options {@link java.util.Collection} of {@link java.lang.String} attribute options.
	 * @param ordered boolean indicating whether or not the attributes values are ordered.
	 */
	public LdapAttribute(String id, Object value, Collection<String> options, boolean ordered) {
		super(id, value, ordered);
		this.options.addAll(options);
	}

	/**
	 * Get options.
	 * 
	 * @return returns a {@link java.util.Set} of {@link java.lang.String}
	 */
	public Set<String> getOptions() {
		return this.options;
	}
	
	/**
	 * Set options.
	 * 
	 * @param options {@link java.util.Set} of {@link java.lang.String}
	 */
	public void setOptions(Set<String> options) {
		this.options = options;
	}
	
	/**
	 * Add an option.
	 * 
	 * @param option {@link java.lang.String} option.
	 * @return boolean indication successful addition of option.
	 */
	public boolean addOption(String option) {
		return this.options.add(option);
	}
	
	/**
	 * Add all values in the collection to the options.
	 * 
	 * @param options {@link java.util.Collection} of {@link java.lang.String} values.
	 * @return boolean indication successful addition of options.
	 */
	public boolean addAllOptions(Collection<String> options) {
		return this.options.addAll(options);
	}
	
	/**
	 * Clears all stored options.
	 */
	public void clearOptions() {
		this.options.clear();
	}
	
	/**
	 * Checks for existence of a particular option on the set.
	 * 
	 * @param option {@link java.lang.String} option.
	 * @return boolean indicating result.
	 */
	public boolean contains(String option) {
		return this.options.contains(option);
	}
	
	/**
	 * Checks for existence of a series of options on the set.
	 * 
	 * @param options {@link java.util.Collection} of {@link java.lang.String} options.
	 * @return boolean indicating result.
	 */
	public boolean containsAll(Collection<String> options) {
		return this.options.containsAll(options);
	}
	
	/**
	 * Tests for the presence of options.
	 * 
	 * @return boolean indicating result.
	 */
	public boolean hasOptions() {
		return !options.isEmpty();
	}
	
	/**
	 * Removes an option from the the set.
	 * 
	 * @param option {@link java.lang.String} option.
	 * @return boolean indicating successful removal of option.
	 */
	public boolean removeOption(String option) {
		return this.options.remove(options);
	}
	
	/**
	 * Removes all options listed in the supplied set.
	 * 
	 * @param options {@link java.util.Collection} of {@link java.lang.String} options.
	 * @return boolean indicating successful removal of options.
	 */
	public boolean removeAllOptions(Collection<String> options) {
		return this.options.removeAll(options);
	}
	
	/**
	 * Removes any options not on the set of supplied options.
	 * 
	 * @param options {@link java.util.Collection} of {@link java.lang.String} options.
	 * @return boolean indicating successful retention of options.
	 */
	public boolean retainAllOptions(Collection<String> options) {
		return this.options.retainAll(options);
	}
	
}
