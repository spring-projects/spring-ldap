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

package org.springframework.ldap.filter;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.ldap.core.LdapEncoder;

/**
 * Abstract superclass for filters that compare values.
 * 
 * @author Mattias Arthursson
 */
public abstract class CompareFilter extends AbstractFilter {

	private final String attribute;

	private final String value;

	private final String encodedValue;

	public CompareFilter(String attribute, String value) {
		this.attribute = attribute;
		this.value = value;
		this.encodedValue = encodeValue(value);
	}

	/**
	 * For testing purposes.
	 * 
	 * @return the encoded value.
	 */
	String getEncodedValue() {
		return encodedValue;
	}

	/**
	 * Override to perform special encoding in subclass.
	 * 
	 * @param value the value to encode.
	 * @return properly escaped value.
	 */
	protected String encodeValue(String value) {
		return LdapEncoder.filterEncode(value);
	}

	/**
	 * Convenience constructor for <code>int</code> values.
	 * 
	 * @param attribute Name of attribute in filter.
	 * @param value The value of the attribute in the filter.
	 */
	public CompareFilter(String attribute, int value) {
		this.attribute = attribute;
		this.value = String.valueOf(value);
		this.encodedValue = LdapEncoder.filterEncode(this.value);
	}

	/*
	 * @see org.springframework.ldap.filter.AbstractFilter#encode(java.lang.StringBuffer)
	 */
	public StringBuffer encode(StringBuffer buff) {
		buff.append('(');
		buff.append(attribute).append(getCompareString()).append(encodedValue);
		buff.append(')');

		return buff;
	}

	/**
	 * Compares key and value before encoding.
	 * 
	 * @see org.springframework.ldap.filter.Filter#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof CompareFilter && o.getClass() == this.getClass()) {
			CompareFilter that = (CompareFilter) o;
			EqualsBuilder builder = new EqualsBuilder();
			builder.append(this.attribute, that.attribute);
			builder.append(this.value, that.value);
			return builder.isEquals();
		}
		else {
			return false;
		}
	}

	/**
	 * Calculate the hash code for the attribute and the value.
	 * 
	 * @see org.springframework.ldap.filter.Filter#hashCode()
	 */
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(attribute);
		builder.append(value);
		return builder.toHashCode();
	}

	/**
	 * Implement this method in subclass to return a String representing the
	 * operator. The {@link EqualsFilter#getCompareString()} would for example
	 * return an equals sign, &quot;=&quot;.
	 * 
	 * @return the String to use as operator in the comparison for the specific
	 * subclass.
	 */
	protected abstract String getCompareString();
}
