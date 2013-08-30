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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents part of an LdapRdn. As specified in RFC2253 an LdapRdn may be
 * composed of several attributes, separated by &quot;+&quot;. An
 * LdapRdnComponent represents one of these attributes.
 * 
 * @author Mattias Hellborg Arthursson
 * @deprecated {@link DistinguishedName and associated classes are deprecated as of 2.0}.
 */
public class LdapRdnComponent implements Comparable, Serializable {
	private static final long serialVersionUID = -3296747972616243038L;

	private static final Log log = LogFactory.getLog(LdapRdnComponent.class);

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
	 * <p>
	 * Depending on the value of the "key case fold" System property, the keys
	 * will be lowercased, uppercased, or preserve their original case. Default
	 * is to convert them to lowercase.
	 * 
	 * @param key the Attribute name.
	 * @param value the Attribute value.
	 * @param decodeValue if <code>true</code> the value is decoded (typically
	 * used when a DN is parsed from a String), otherwise the value is used as
	 * specified.
	 * @see DistinguishedName#KEY_CASE_FOLD_PROPERTY
	 */
	public LdapRdnComponent(String key, String value, boolean decodeValue) {
		Assert.hasText(key, "Key must not be empty");
		Assert.hasText(value, "Value must not be empty");

		String caseFold = System.getProperty(DistinguishedName.KEY_CASE_FOLD_PROPERTY);
		if (!StringUtils.hasText(caseFold) || caseFold.equals(DistinguishedName.KEY_CASE_FOLD_LOWER)) {
			this.key = key.toLowerCase();
		} else if (caseFold.equals(DistinguishedName.KEY_CASE_FOLD_UPPER)) {
			this.key = key.toUpperCase();
		} else if (caseFold.equals(DistinguishedName.KEY_CASE_FOLD_NONE)) {
			this.key = key;
		} else {
			log
					.warn("\"" + caseFold + "\" invalid property value for " + DistinguishedName.KEY_CASE_FOLD_PROPERTY
							+ "; expected \"" + DistinguishedName.KEY_CASE_FOLD_LOWER + "\", \""
							+ DistinguishedName.KEY_CASE_FOLD_UPPER + "\", or \""
							+ DistinguishedName.KEY_CASE_FOLD_NONE + "\"");
			this.key = key.toLowerCase();
		}
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
	 * @deprecated Using this method changes the internal state of surrounding
	 * DistinguishedName instance. This should be avoided.
	 */
	public void setKey(String key) {
        Assert.hasText(key, "Key must not be empty");
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
	 * @deprecated Using this method changes the internal state of surrounding
	 * DistinguishedName instance. This should be avoided.
	 */
	public void setValue(String value) {
        Assert.hasText(value, "Value must not be empty");
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
		return key.toUpperCase().hashCode() ^ value.toUpperCase().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		// Slightly more lenient equals comparison here to enable immutable
		// instances to equal mutable ones.
		if (obj != null && obj instanceof LdapRdnComponent) {
			LdapRdnComponent that = (LdapRdnComponent) obj;
            // It's safe to compare directly against key and value,
            // because they are validated not to be null on instance creation.
			return this.key.equalsIgnoreCase(that.key)
					&& this.value.equalsIgnoreCase(that.value);

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

        // It's safe to compare directly against key and value,
        // because they are validated not to be null on instance creation.
        int keyCompare = this.key.toLowerCase().compareTo(that.key.toLowerCase());
        if(keyCompare == 0) {
            return this.value.toLowerCase().compareTo(that.value.toLowerCase());
        } else {
            return keyCompare;
        }
	}

	/**
	 * Create an immutable copy of this instance. It will not be possible to
	 * modify the key or the value of the returned instance.
	 * 
	 * @return an immutable copy of this instance.
	 * @since 1.3
	 */
	public LdapRdnComponent immutableLdapRdnComponent() {
		return new ImmutableLdapRdnComponent(key, value);
	}

	private static class ImmutableLdapRdnComponent extends LdapRdnComponent {
		private static final long serialVersionUID = -7099970046426346567L;

		public ImmutableLdapRdnComponent(String key, String value) {
			super(key, value);
		}

		public void setKey(String key) {
			throw new UnsupportedOperationException("SetValue not supported for this immutable LdapRdnComponent");
		}

		public void setValue(String value) {
			throw new UnsupportedOperationException("SetKey not supported for this immutable LdapRdnComponent");
		}
	}
}
