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

package org.springframework.ldap.core;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;

import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.Assert;

/**
 * Wrapper class to handle the full identification of an LDAP entry. An LDAP entry is
 * identified by its Distinguished Name, in Spring LDAP represented by the
 * {@link DistinguishedName} class. A Distinguished Name can be absolute - i.e. complete
 * including the very root (base) of the LDAP tree - or relative - i.e relative to the
 * base LDAP path of the current LDAP connection (specified as <code>base</code> to the
 * {@link ContextSource}).
 * <p>
 * The different representations are needed on different occasions, e.g. the relative DN
 * is typically what is needed to perform lookups and searches in the LDAP tree, whereas
 * the absolute DN is needed when authenticating and when an LDAP entry is referred to in
 * e.g. a group. This wrapper class contains both of these representations.
 *
 * @author Mattias Hellborg Arthursson
 */
public class LdapEntryIdentification {

	private final LdapName relativeDn;

	private final LdapName absoluteDn;

	/**
	 * Construct an LdapEntryIdentification instance.
	 * @param absoluteDn the absolute DN of the identified entry, e.g. as returned by
	 * {@link DirContext#getNameInNamespace()}.
	 * @param relativeDn the DN of the identified entry relative to the base LDAP path,
	 * e.g. as returned by {@link DirContextOperations#getDn()}.
	 * @deprecated {@link DistinguishedName} and associated classes and methods are
	 * deprecated as of 2.0. use
	 * {@link #LdapEntryIdentification(javax.naming.ldap.LdapName, javax.naming.ldap.LdapName)}
	 * instead.
	 */
	public LdapEntryIdentification(DistinguishedName absoluteDn, DistinguishedName relativeDn) {
		Assert.notNull(absoluteDn, "Absolute DN must not be null");
		Assert.notNull(relativeDn, "Relative DN must not be null");
		this.absoluteDn = LdapUtils.newLdapName(absoluteDn);
		this.relativeDn = LdapUtils.newLdapName(relativeDn);
	}

	/**
	 * Construct an LdapEntryIdentification instance.
	 * @param absoluteDn the absolute DN of the identified entry, e.g. as returned by
	 * {@link DirContext#getNameInNamespace()}.
	 * @param relativeDn the DN of the identified entry relative to the base LDAP path,
	 * e.g. as returned by {@link DirContextOperations#getDn()}.
	 * @since 2.0
	 */
	public LdapEntryIdentification(LdapName absoluteDn, LdapName relativeDn) {
		Assert.notNull(absoluteDn, "Absolute DN must not be null");
		Assert.notNull(relativeDn, "Relative DN must not be null");
		this.absoluteDn = LdapUtils.newLdapName(absoluteDn);
		this.relativeDn = LdapUtils.newLdapName(relativeDn);
	}

	/**
	 * Get the DN of the identified entry relative to the base LDAP path, e.g. as returned
	 * by {@link DirContextOperations#getDn()}.
	 * @return the relative DN.
	 * @since 2.0
	 */
	public LdapName getAbsoluteName() {
		return LdapUtils.newLdapName(this.absoluteDn);
	}

	/**
	 * Get the absolute DN of the identified entry, e.g. as returned by
	 * {@link DirContext#getNameInNamespace()}.
	 * @return the absolute DN.
	 * @since 2.0
	 */
	public LdapName getRelativeName() {
		return LdapUtils.newLdapName(this.relativeDn);
	}

	/**
	 * Get the DN of the identified entry relative to the base LDAP path, e.g. as returned
	 * by {@link DirContextOperations#getDn()}.
	 * @return the relative DN.
	 * @deprecated {@link DistinguishedName} and associated classes and methods are
	 * deprecated as of 2.0. use {@link #getRelativeName()} instead.
	 */
	public DistinguishedName getRelativeDn() {
		return new DistinguishedName(this.relativeDn);
	}

	/**
	 * Get the absolute DN of the identified entry, e.g. as returned by
	 * {@link DirContext#getNameInNamespace()}.
	 * @return the absolute DN.
	 * @deprecated {@link DistinguishedName} and associated classes and methods are
	 * deprecated as of 2.0. use {@link #getAbsoluteName()} instead.
	 */
	public DistinguishedName getAbsoluteDn() {
		return new DistinguishedName(this.absoluteDn);
	}

	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			LdapEntryIdentification that = (LdapEntryIdentification) obj;
			return this.absoluteDn.equals(that.absoluteDn) && this.relativeDn.equals(that.relativeDn);
		}

		return false;
	}

	public int hashCode() {
		return this.absoluteDn.hashCode() ^ this.relativeDn.hashCode();
	}

}
