/*
 * Copyright 2006-present the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.Rdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.BadLdapGrammarException;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.support.ListComparator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of a {@link Name} corresponding to an LDAP path. A Distinguished
 * Name manipulation implementation is included in JDK1.5 (LdapName), but not in prior
 * releases.
 *
 * A <code>DistinguishedName</code> is particularly useful when building or modifying an
 * LDAP path dynamically, as escaping will be taken care of.
 *
 * A path is split into several names. The {@link Name} interface specifies that the most
 * significant part be in position 0.
 * <p>
 * Example:
 *
 * <dl>
 * <dt>The path</dt>
 * <dd>uid=adam.skogman, ou=People, ou=EU</dd>
 * <dt>Name[0]</dt>
 * <dd>ou=EU</dd>
 * <dt>Name[1]</dt>
 * <dd>ou=People</dd>
 * <dt>Name[2]</dt>
 * <dd>uid=adam.skogman</dd>
 * </dl>
 * <p>
 * <code>Name</code> instances, and consequently <code>DistinguishedName</code> instances
 * are naturally mutable, which is useful when constructing DistinguishedNames. Example:
 *
 * <pre>
 * DistinguishedName path = new DistinguishedName(&quot;dc=jayway,dc=se&quot;);
 * path.add(&quot;ou&quot;, &quot;People&quot;);
 * path.add(&quot;uid&quot;, &quot;adam.skogman&quot;);
 * String dn = path.toString();
 * </pre>
 *
 * will render <code>uid=adam.skogman,ou=People,dc=jayway,dc=se</code>.
 * <p>
 * <b>NOTE:</b> The fact that DistinguishedName instances are mutable needs to be taken
 * into careful account, as this means that they may be modified involuntarily. This means
 * that whenever a <code>DistinguishedName</code> instance is kept for reference (e.g. for
 * identification of a domain entry) or as a constant, you should consider getting an
 * immutable copy of the instance using {@link #immutableDistinguishedName()} or
 * {@link #immutableDistinguishedName(String)}.
 * <p>
 * <b>NB:</b>As of version 1.3 the default toString representation of DistinguishedName
 * now defaults to a compact one, without spaces between the respective RDNs. For backward
 * compatibility, set the {@link #SPACED_DN_FORMAT_PROPERTY}
 * ({@value #SPACED_DN_FORMAT_PROPERTY}) to <code>true</code>.
 *
 * @author Adam Skogman
 * @author Mattias Hellborg Arthursson
 * @see javax.naming.ldap.LdapName
 * @see LdapUtils#newLdapName(javax.naming.Name)
 * @see LdapUtils#newLdapName(String)
 * @see org.springframework.ldap.support.LdapUtils#emptyLdapName()
 * @deprecated As of 2.0 it is recommended to use {@link javax.naming.ldap.LdapName} along
 * with utility methods in {@link LdapUtils} instead.
 */
@Deprecated
public class DistinguishedName implements Name {

	/**
	 * System property that will be inspected to determine whether {@link #toString()}
	 * will format the DN with spaces after each comma or use a more compact
	 * representation, i.e.: <code>uid=adam.skogman, ou=People, dc=jayway, dc=se</code>
	 * rather than <code>uid=adam.skogman,ou=People,dc=jayway,dc=se</code>. A value other
	 * than null or blank will trigger the spaced format. Default is the compact
	 * representation.
	 * <p>
	 * Valid values are:
	 * <ul>
	 * <li>blank or null (property not set)</li>
	 * <li>any non-blank value</li>
	 * </ul>
	 * @since 1.3
	 * @see #toCompactString()
	 */
	public static final String SPACED_DN_FORMAT_PROPERTY = "org.springframework.ldap.core.spacedDnFormat";

	/**
	 * System property that will be inspected to determine whether creating a
	 * DistinguishedName will convert the keys to <em>lowercase</em>, convert the keys to
	 * <em>uppercase</em>, or leave the keys as they were in the original String, ie
	 * <em>none</em>. Default is to convert the keys to lowercase.
	 * <p>
	 * Valid values are:
	 * <ul>
	 * <li>"lower" or blank or null (property not set)</li>
	 * <li>"upper"</li>
	 * <li>"none"</li>
	 * </ul>
	 * @since 1.3.1
	 * @see #KEY_CASE_FOLD_LOWER
	 * @see #KEY_CASE_FOLD_UPPER
	 * @see #KEY_CASE_FOLD_NONE
	 */
	public static final String KEY_CASE_FOLD_PROPERTY = "org.springframework.ldap.core.keyCaseFold";

	public static final String KEY_CASE_FOLD_LOWER = "lower";

	public static final String KEY_CASE_FOLD_UPPER = "upper";

	public static final String KEY_CASE_FOLD_NONE = "none";

	private static final String MANGLED_DOUBLE_QUOTES = "\\\\\"";

	private static final String PROPER_DOUBLE_QUOTES = "\\\"";

	private static final Logger LOG = LoggerFactory.getLogger(DistinguishedName.class);

	private static final boolean COMPACT = true;

	private static final boolean NON_COMPACT = false;

	private static final long serialVersionUID = 3514344371999042586L;

	/**
	 * An empty, unmodifiable DistinguishedName.
	 */
	public static final DistinguishedName EMPTY_PATH = new DistinguishedName(Collections.EMPTY_LIST);

	private static final int DEFAULT_BUFFER_SIZE = 256;

	private List names;

	/**
	 * Construct a new DistinguishedName with no components.
	 */
	public DistinguishedName() {
		this.names = new LinkedList();
	}

	/**
	 * Construct a new <code>DistinguishedName</code> from a String.
	 * @param path a String corresponding to a (syntactically) valid LDAP path.
	 */
	public DistinguishedName(String path) {
		if (!StringUtils.hasText(path)) {
			this.names = new LinkedList();
		}
		else {
			parse(path);
		}
	}

	/**
	 * Construct a new <code>DistinguishedName</code> from the supplied <code>List</code>
	 * of {@link LdapRdn} objects.
	 * @param list the components that this instance will consist of.
	 */
	public DistinguishedName(List list) {
		this.names = list;
	}

	/**
	 * Construct a new <code>DistinguishedName</code> from the supplied {@link Name}. The
	 * parts of the supplied {@link Name} must be syntactically correct {@link LdapRdn}s.
	 * @param name the {@link Name} to construct a new <code>DistinguishedName</code>
	 * from.
	 */
	public DistinguishedName(Name name) {
		Assert.notNull(name, "name cannot be null");
		if (name instanceof CompositeName) {
			parse(LdapUtils.convertCompositeNameToString((CompositeName) name));
			return;
		}
		this.names = new LinkedList();
		for (int i = 0; i < name.size(); i++) {
			this.names.add(new LdapRdn(name.get(i)));
		}
	}

	/**
	 * Parse the supplied String and make this instance represent the corresponding
	 * distinguished name.
	 * @param path the LDAP path to parse.
	 */
	protected final void parse(String path) {
		DnParser parser = DefaultDnParserFactory.createDnParser(unmangleCompositeName(path));
		DistinguishedName dn;
		try {
			dn = parser.dn();
		}
		catch (ParseException ex) {
			throw new BadLdapGrammarException("Failed to parse DN", ex);
		}
		catch (org.springframework.ldap.core.TokenMgrError ex) {
			throw new BadLdapGrammarException("Failed to parse DN", ex);
		}
		this.names = dn.names;
	}

	/**
	 * If path is surrounded by quotes, strip them. JNDI considers forward slash ('/')
	 * special, but LDAP doesn't. {@link CompositeName#toString()} tends to mangle a
	 * {@link Name} with a slash by surrounding it with quotes ('"').
	 * @param path Path to check and possibly strip.
	 * @return A String with the possibly stripped path.
	 */
	private String unmangleCompositeName(String path) {
		String tempPath;
		// Check if CompositeName has mangled the name with quotes
		if (path.startsWith("\"") && path.endsWith("\"")) {
			tempPath = path.substring(1, path.length() - 1);
		}
		else {
			tempPath = path;
		}

		tempPath = StringUtils.replace(tempPath, MANGLED_DOUBLE_QUOTES, PROPER_DOUBLE_QUOTES);
		return tempPath;
	}

	/**
	 * Get the {@link LdapRdn} at a specified position.
	 * @param index the {@link LdapRdn} to retrieve.
	 * @return the {@link LdapRdn} at the requested position.
	 */
	public LdapRdn getLdapRdn(int index) {
		return (LdapRdn) this.names.get(index);
	}

	/**
	 * Get the {@link LdapRdn} with the specified key. If there are several {@link Rdn}s
	 * with the same key, the first one found (in order of significance) will be returned.
	 * @param key Attribute name of the {@link LdapRdn} to retrieve.
	 * @return the {@link LdapRdn} with the requested key.
	 * @throws IllegalArgumentException if no Rdn matches the given key.
	 */
	public LdapRdn getLdapRdn(String key) {
		for (Iterator iter = this.names.iterator(); iter.hasNext();) {
			LdapRdn rdn = (LdapRdn) iter.next();
			if (ObjectUtils.nullSafeEquals(rdn.getKey(), key)) {
				return rdn;
			}
		}

		throw new IllegalArgumentException("No Rdn with the requested key: '" + key + "'");
	}

	/**
	 * Get the value of the {@link LdapRdnComponent} with the specified key (Attribute
	 * value). If there are several Rdns with the same key, the value of the first one
	 * found (in order of significance) will be returned.
	 * @param key Attribute name of the {@link LdapRdn} to retrieve.
	 * @return the value.
	 * @throws IllegalArgumentException if no Rdn matches the given key.
	 */
	public String getValue(String key) {
		return getLdapRdn(key).getValue();
	}

	/**
	 * Get the name <code>List</code>.
	 * @return the list of {@link LdapRdn}s that this <code>DistinguishedName</code>
	 * consists of.
	 */
	public List getNames() {
		return this.names;
	}

	/**
	 * Get the compact String representation of this <code>DistinguishedName</code>. Add
	 * no space after each comma, to make it compact.
	 * @return a syntactically correct, properly escaped String representation of the
	 * <code>DistinguishedName</code>.
	 */
	public String toCompactString() {
		return format(COMPACT);
	}

	/**
	 * Builds a complete LDAP path, ldap encoded, useful as a DN.
	 *
	 * Always uses lowercase, always separates with ", " i.e. comma and a space.
	 * @return the LDAP path.
	 */
	public String encode() {
		return format(NON_COMPACT);
	}

	private String format(boolean compact) {
		// empty path
		if (this.names.size() == 0) {
			return "";
		}

		StringBuffer buffer = new StringBuffer(DEFAULT_BUFFER_SIZE);

		ListIterator i = this.names.listIterator(this.names.size());
		while (i.hasPrevious()) {
			LdapRdn rdn = (LdapRdn) i.previous();
			buffer.append(rdn.getLdapEncoded());

			// add comma, except in last iteration
			if (i.hasPrevious()) {
				if (compact) {
					buffer.append(",");
				}
				else {
					buffer.append(", ");

				}
			}
		}

		return buffer.toString();
	}

	/**
	 * Builds a complete LDAP path, ldap and url encoded. Separates only with ",".
	 * @return the LDAP path, for use in an url.
	 */
	public String toUrl() {
		StringBuffer buffer = new StringBuffer(DEFAULT_BUFFER_SIZE);

		for (int i = this.names.size() - 1; i >= 0; i--) {
			LdapRdn n = (LdapRdn) this.names.get(i);
			buffer.append(n.encodeUrl());
			if (i > 0) {
				buffer.append(",");
			}
		}
		return buffer.toString();
	}

	/**
	 * Determines if this <code>DistinguishedName</code> path contains another path.
	 * @param path the path to check.
	 * @return <code>true</code> if the supplied path is conained in this instance,
	 * <code>false</code> otherwise.
	 */
	public boolean contains(DistinguishedName path) {

		List shortlist = path.getNames();

		// this path must be at least as long
		if (getNames().size() < shortlist.size()) {
			return false;
		}

		// must have names
		if (shortlist.size() == 0) {
			return false;
		}

		Iterator longiter = getNames().iterator();
		Iterator shortiter = shortlist.iterator();

		LdapRdn longname = (LdapRdn) longiter.next();
		LdapRdn shortname = (LdapRdn) shortiter.next();

		// find first match
		while (!longname.equals(shortname) && longiter.hasNext()) {
			longname = (LdapRdn) longiter.next();
		}

		// Done?
		if (!shortiter.hasNext() && longname.equals(shortname)) {
			return true;
		}
		if (!longiter.hasNext()) {
			return false;
		}

		// compare
		while (longname.equals(shortname) && longiter.hasNext() && shortiter.hasNext()) {
			longname = (LdapRdn) longiter.next();
			shortname = (LdapRdn) shortiter.next();
		}

		// Done
		return !shortiter.hasNext() && longname.equals(shortname);

	}

	/**
	 * Add an LDAP path last in this DistinguishedName. E.g.:
	 *
	 * <pre>
	 * DistinguishedName name1 = new DistinguishedName(&quot;c=SE, dc=jayway, dc=se&quot;);
	 * DistinguishedName name2 = new DistinguishedName(&quot;ou=people&quot;);
	 * name1.append(name2);
	 * </pre>
	 *
	 * will result in <code>ou=people, c=SE, dc=jayway, dc=se</code>
	 * @param path the path to append.
	 * @return this instance.
	 */
	public DistinguishedName append(DistinguishedName path) {
		getNames().addAll(path.getNames());
		return this;
	}

	/**
	 * Append a new {@link LdapRdn} using the supplied key and value.
	 * @param key the key of the {@link LdapRdn}.
	 * @param value the value of the {@link LdapRdn}.
	 * @return this instance.
	 */
	public DistinguishedName append(String key, String value) {
		add(key, value);
		return this;
	}

	/**
	 * Add an LDAP path first in this DistinguishedName. E.g.:
	 *
	 * <pre>
	 * DistinguishedName name1 = new DistinguishedName(&quot;ou=people&quot;);
	 * DistinguishedName name2 = new DistinguishedName(&quot;c=SE, dc=jayway, dc=se&quot;);
	 * name1.prepend(name2);
	 * </pre>
	 *
	 * will result in <code>ou=people, c=SE, dc=jayway, dc=se</code>
	 * @param path the path to prepend.
	 */
	public void prepend(DistinguishedName path) {
		ListIterator i = path.getNames().listIterator(path.getNames().size());
		while (i.hasPrevious()) {
			this.names.add(0, i.previous());
		}
	}

	/**
	 * Remove the first part of this <code>DistinguishedName</code>.
	 * @return the removed entry.
	 */
	public LdapRdn removeFirst() {
		return (LdapRdn) this.names.remove(0);
	}

	/**
	 * Remove the supplied path from the beginning of this <code>DistinguishedName</code>
	 * if this instance starts with <code>path</code>. Useful for stripping base path
	 * suffix from a <code>DistinguishedName</code>.
	 * @param path the path to remove from the beginning of this instance.
	 */
	public void removeFirst(Name path) {
		if (path != null && this.startsWith(path)) {
			for (int i = 0; i < path.size(); i++) {
				this.removeFirst();
			}
		}
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			DistinguishedName result = (DistinguishedName) super.clone();
			result.names = new LinkedList(this.names);
			return result;
		}
		catch (CloneNotSupportedException ex) {
			LOG.error("CloneNotSupported thrown from superclass - this should not happen");
			throw new UncategorizedLdapException("Fatal error in clone", ex);
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// A subclass with identical values should NOT be considered equal.
		// EqualsBuilder in commons-lang cannot handle subclasses correctly.
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}

		DistinguishedName name = (DistinguishedName) obj;

		// compare the lists
		return getNames().equals(name.getNames());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getClass().hashCode() ^ getNames().hashCode();
	}

	/**
	 * Get the String representation of this <code>DistinguishedName</code>. Depending on
	 * the setting of property <code>org.springframework.ldap.core.spacedDnFormat</code> a
	 * space will be added after each comma, to make the result more readable. Default is
	 * compact representation, i.e. without any spaces.
	 * @return a syntactically correct, properly escaped String representation of the
	 * <code>DistinguishedName</code>.
	 * @see #SPACED_DN_FORMAT_PROPERTY
	 */
	@Override
	public String toString() {
		String spacedFormatting = System.getProperty(SPACED_DN_FORMAT_PROPERTY);
		if (!StringUtils.hasText(spacedFormatting)) {
			return format(COMPACT);
		}
		else {
			return format(NON_COMPACT);
		}
	}

	/**
	 * Compare this instance to another object. Note that the comparison is done in order
	 * of significance, so the most significant Rdn is compared first, then the second and
	 * so on.
	 *
	 * @see javax.naming.Name#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object obj) {
		DistinguishedName that = (DistinguishedName) obj;
		ListComparator comparator = new ListComparator();
		return comparator.compare(this.names, that.names);
	}

	public int size() {
		return this.names.size();
	}

	public boolean isEmpty() {
		return this.names.size() == 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#getAll()
	 */
	public Enumeration getAll() {
		LinkedList strings = new LinkedList();
		for (Iterator iter = this.names.iterator(); iter.hasNext();) {
			LdapRdn rdn = (LdapRdn) iter.next();
			strings.add(rdn.getLdapEncoded());
		}

		return Collections.enumeration(strings);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#get(int)
	 */
	public String get(int index) {
		LdapRdn rdn = (LdapRdn) this.names.get(index);
		return rdn.getLdapEncoded();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#getPrefix(int)
	 */
	public Name getPrefix(int index) {
		LinkedList newNames = new LinkedList();
		for (int i = 0; i < index; i++) {
			newNames.add(this.names.get(i));
		}

		return new DistinguishedName(newNames);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#getSuffix(int)
	 */
	public Name getSuffix(int index) {
		if (index > this.names.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}

		LinkedList newNames = new LinkedList();
		for (int i = index; i < this.names.size(); i++) {
			newNames.add(this.names.get(i));
		}

		return new DistinguishedName(newNames);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#startsWith(javax.naming.Name)
	 */
	public boolean startsWith(Name name) {
		if (name.size() == 0) {
			return false;
		}

		DistinguishedName start = null;
		if (name instanceof DistinguishedName) {
			start = (DistinguishedName) name;
		}
		else {
			return false;
		}

		if (start.size() > this.size()) {
			return false;
		}

		Iterator longiter = this.names.iterator();
		Iterator shortiter = start.getNames().iterator();

		while (shortiter.hasNext()) {
			Object longname = longiter.next();
			Object shortname = shortiter.next();

			if (!longname.equals(shortname)) {
				return false;
			}
		}

		// All names in shortiter matched.
		return true;
	}

	/**
	 * Determines if this <code>DistinguishedName</code> ends with a certian path.
	 *
	 * If the argument path is empty (no names in path) this method will return
	 * <code>false</code>.
	 * @param name The suffix to check for.
	 *
	 */
	public boolean endsWith(Name name) {
		DistinguishedName path = null;
		if (name instanceof DistinguishedName) {
			path = (DistinguishedName) name;
		}
		else {
			return false;
		}

		List shortlist = path.getNames();

		// this path must be at least as long
		if (getNames().size() < shortlist.size()) {
			return false;
		}

		// must have names
		if (shortlist.size() == 0) {
			return false;
		}

		ListIterator longiter = getNames().listIterator(getNames().size());
		ListIterator shortiter = shortlist.listIterator(shortlist.size());

		while (shortiter.hasPrevious()) {
			LdapRdn longname = (LdapRdn) longiter.previous();
			LdapRdn shortname = (LdapRdn) shortiter.previous();

			if (!longname.equals(shortname)) {
				return false;
			}
		}

		// if short list ended, all were equal
		return true;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#addAll(javax.naming.Name)
	 */
	public Name addAll(Name name) throws InvalidNameException {
		return addAll(this.names.size(), name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#addAll(int, javax.naming.Name)
	 */
	public Name addAll(int arg0, Name name) throws InvalidNameException {
		DistinguishedName distinguishedName = null;
		try {
			distinguishedName = (DistinguishedName) name;
		}
		catch (ClassCastException ex) {
			throw new InvalidNameException("Invalid name type");
		}

		this.names.addAll(arg0, distinguishedName.getNames());
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#add(java.lang.String)
	 */
	public Name add(String string) throws InvalidNameException {
		return add(this.names.size(), string);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#add(int, java.lang.String)
	 */
	public Name add(int index, String string) throws InvalidNameException {
		try {
			this.names.add(index, new LdapRdn(string));
		}
		catch (BadLdapGrammarException ex) {
			throw new InvalidNameException("Failed to parse rdn '" + string + "'");
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.naming.Name#remove(int)
	 */
	public Object remove(int arg0) throws InvalidNameException {
		LdapRdn rdn = (LdapRdn) this.names.remove(arg0);
		return rdn.getLdapEncoded();
	}

	/**
	 * Remove the last part of this <code>DistinguishedName</code>.
	 * @return the removed {@link LdapRdn}.
	 */
	public LdapRdn removeLast() {
		return (LdapRdn) this.names.remove(this.names.size() - 1);
	}

	/**
	 * Add a new {@link LdapRdn} using the supplied key and value.
	 * @param key the key of the {@link LdapRdn}.
	 * @param value the value of the {@link LdapRdn}.
	 */
	public void add(String key, String value) {
		this.names.add(new LdapRdn(key, value));
	}

	/**
	 * Add the supplied {@link LdapRdn} last in the list of Rdns.
	 * @param rdn the {@link LdapRdn} to add.
	 */
	public void add(LdapRdn rdn) {
		this.names.add(rdn);
	}

	/**
	 * Add the supplied {@link LdapRdn} att the specified index.
	 * @param idx the index at which to add the LdapRdn.
	 * @param rdn the LdapRdn to add.
	 */
	public void add(int idx, LdapRdn rdn) {
		this.names.add(idx, rdn);
	}

	/**
	 * Return an immutable copy of this instance. It will not be possible to add or remove
	 * any Rdns to or from the returned instance, and the respective Rdns will also be
	 * immutable in turn.
	 * @return a copy of this instance backed by an immutable list.
	 * @since 1.2
	 */
	public DistinguishedName immutableDistinguishedName() {
		List listWithImmutableRdns = new ArrayList(this.names.size());
		for (Iterator iterator = this.names.iterator(); iterator.hasNext();) {
			LdapRdn rdn = (LdapRdn) iterator.next();
			listWithImmutableRdns.add(rdn.immutableLdapRdn());
		}

		return new DistinguishedName(Collections.unmodifiableList(listWithImmutableRdns));
	}

	/**
	 * Create an immutable DistinguishedName instance, suitable as a constant.
	 * @param dnString the DN string to parse.
	 * @return an immutable DistinguishedName corresponding to the supplied DN string.
	 * @since 1.3
	 */
	public static final DistinguishedName immutableDistinguishedName(String dnString) {
		return new DistinguishedName(dnString).immutableDistinguishedName();
	}

}
