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
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Represents part of an LdapRdn. As specified in RFC2253 an LdapRdn may be
 * composed of several attributes, separated by &quot;+&quot;. An
 * LdapRdnComponent represents one of these attributes.
 * 
 * @author Mattias Arthursson
 * 
 */
public class LdapRdnComponent implements Comparable, Serializable {
	private static final long serialVersionUID = -3296747972616243038L;

	public static final boolean DONT_DECODE_VALUE = false;

	private String key;

	private String value;

	/**
	 * Constructs an LdapRdnComponent without decoding the value.
	 * 
	 * @param key the Attribute name.
	 * @param value the Attribute value.
	 */
	public LdapRdnComponent(String key, String value) {
		this(key, value, DONT_DECODE_VALUE);
	}

	/**
	 * Constructs an LdapRdnComponent, optionally decoding the value.
	 * 
	 * @param key the Atttribute name.
	 * @param value the Attribute value.
	 * @param decodeValue if <code>true</code> the value is decoded (typically
	 * used when a DN is parsed from a String), otherwise the value is used as
	 * specified.
	 */
	public LdapRdnComponent(String key, String value, boolean decodeValue) {
		Validate.notEmpty(key, "Key must not be empty");
		Validate.notEmpty(value, "Value must not be empty");

		this.key = StringUtils.lowerCase(key);
		if (decodeValue) {
			this.value = LdapEncoder.nameDecode(value);
		}
		else {
			this.value = value;
		}
	}

	/**
	 * Get the key (Attribute name) of this component.
	 * 
	 * @return the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the key (Attribute name) of this component.
	 * 
	 * @param key the key.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Get the (Attribute) value of this component.
	 * 
	 * @return the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the (Attribute) value of this component.
	 * 
	 * @param value the value.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Encode key and value to ldap.
	 * 
	 * @return Properly ldap escaped rdn.
	 */
	protected String encodeLdap() {
		StringBuffer buff = new StringBuffer(key.length() + value.length() * 2);

		buff.append(key);
		buff.append('=');
		buff.append(LdapEncoder.nameEncode(value));

		return buff.toString();
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
	 * @return The LdapRdn as a string where the value is LDAP-encoded.
	 */
	public String getLdapEncoded() {
		return encodeLdap();
	}

	/**
	 * Get a String representation of this instance for use in URLs.
	 * 
	 * @return a properly URL encoded representation of this instancs.
	 */
	public String encodeUrl() {
		// Use the URI class to properly URL encode the value.
		try {
			URI valueUri = new URI(null, null, value, null);
			return key + "=" + valueUri.toString();
		}
		catch (URISyntaxException e) {
			// This should really never happen...
			return key + "=" + "value";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return key.hashCode() ^ value.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == LdapRdnComponent.class) {
			LdapRdnComponent that = (LdapRdnComponent) obj;
			return StringUtils.equalsIgnoreCase(this.key, that.key)
					&& StringUtils.equalsIgnoreCase(this.value, that.value);

		}
		else {
			return false;
		}
	}

	/**
	 * Compare this instance to the supplied object.
	 * 
	 * @param obj the object to compare to.
	 * @throws ClassCastException if the object is not possible to cast to an
	 * LdapRdnComponent.
	 */
	public int compareTo(Object obj) {
		LdapRdnComponent that = (LdapRdnComponent) obj;
		return this.toString().compareTo(that.toString());
	}
}
