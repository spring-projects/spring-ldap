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

import java.net.URI;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.support.LdapEncoder;
import org.springframework.ldap.support.LdapUtils;

/**
 * Extends {@link javax.naming.directory.BasicAttributes} to add specialized support for
 * DNs.
 * <p>
 * While DNs appear to be and can be treated as attributes, they have a special meaning in
 * that they define the address to which the object is bound. DNs must conform to special
 * formatting rules and are typically required to be handled separately from other
 * attributes.
 * <p>
 * This class makes this distinction between the DN and other attributes prominent and
 * apparent.
 *
 * @author Keith Barlow
 *
 */
public class LdapAttributes extends BasicAttributes {

	private static final long serialVersionUID = 97903297123869138L;

	private static Logger log = LoggerFactory.getLogger(LdapAttributes.class);

	private static final String SAFE_CHAR = "[\\p{ASCII}&&[^\\x00\\x0A\\x0D]]"; // Any
																				// ASCII
																				// except
																				// NUL,
																				// LF, and
																				// CR

	private static final String SAFE_INIT_CHAR = "[\\p{ASCII}&&[^ \\x00\\x0A\\x0D\\x3A\\x3C]]"; // Any
																								// ASCII
																								// except
																								// NUL,
																								// LF,
																								// CR,
																								// SPACE,
																								// colon,
																								// and
																								// less-than

	/**
	 * Distinguished name to which the object is bound.
	 */
	protected LdapName dn = LdapUtils.emptyLdapName();

	/**
	 * Default constructor.
	 */
	public LdapAttributes() {

	}

	/**
	 * Constructor for specifying whether or not the object is case sensitive.
	 * @param ignoreCase boolean indicator.
	 */
	public LdapAttributes(boolean ignoreCase) {
		super(ignoreCase);
	}

	/**
	 * Returns the distinguished name to which the object is bound.
	 * @return {@link org.springframework.ldap.core.DistinguishedName} specifying the name
	 * to which the object is bound.
	 * @deprecated {@link DistinguishedName and associated classes and methods are
	 * deprecated as of 2.0}. use {@link #getName()} instead.
	 */
	@Deprecated
	public DistinguishedName getDN() {
		return new DistinguishedName(this.dn);
	}

	/**
	 * Returns the distinguished name to which the object is bound.
	 * @return {@link LdapName} specifying the name to which the object is bound.
	 */
	public LdapName getName() {
		return LdapUtils.newLdapName(this.dn);
	}

	/**
	 * Sets the distinguished name of the object.
	 * @param dn {@link org.springframework.ldap.core.DistinguishedName} specifying the
	 * name to which the object is bound.
	 * @deprecated {@link DistinguishedName and associated classes and methods are
	 * deprecated as of 2.0}. use {@link #setName(javax.naming.Name)} instead.
	 */
	@Deprecated
	public void setDN(DistinguishedName dn) {
		this.dn = LdapUtils.newLdapName(dn);
	}

	public void setName(Name name) {
		this.dn = LdapUtils.newLdapName(name);
	}

	/**
	 * Returns a string representation of the object in LDIF format.
	 * @return {@link java.lang.String} formated to RFC2849 LDIF specifications.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();

		try {

			LdapName dn = getName();

			if (!dn.toString().matches(SAFE_INIT_CHAR + SAFE_CHAR + "*")) {
				sb.append("dn:: " + LdapEncoder.printBase64Binary(dn.toString().getBytes()) + "\n");
			}
			else {
				sb.append("dn: " + getDN() + "\n");
			}

			NamingEnumeration<Attribute> attributes = getAll();

			while (attributes.hasMore()) {
				Attribute attribute = attributes.next();
				NamingEnumeration<?> values = attribute.getAll();

				while (values.hasMore()) {
					Object value = values.next();

					if (value instanceof String) {
						sb.append(attribute.getID() + ": " + (String) value + "\n");

					}
					else if (value instanceof byte[]) {
						sb.append(attribute.getID() + ":: " + LdapEncoder.printBase64Binary((byte[]) value) + "\n");

					}
					else if (value instanceof URI) {
						sb.append(attribute.getID() + ":< " + (URI) value + "\n");

					}
					else {
						sb.append(attribute.getID() + ": " + value + "\n");
					}
				}
			}

		}
		catch (NamingException ex) {
			log.error("Error formating attributes for output.", ex);
			sb = new StringBuilder();
		}

		return sb.toString();
	}

}
