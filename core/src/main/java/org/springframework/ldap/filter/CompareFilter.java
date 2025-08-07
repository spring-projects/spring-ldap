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

package org.springframework.ldap.filter;

import org.springframework.ldap.support.LdapEncoder;

/**
 * Abstract superclass for filters that compare values.
 *
 * @author Mattias Hellborg Arthursson
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
	 * @return the encoded value.
	 */
	String getEncodedValue() {
		return this.encodedValue;
	}

	/**
	 * Override to perform special encoding in subclass.
	 * @param value the value to encode.
	 * @return properly escaped value.
	 */
	protected String encodeValue(String value) {
		return LdapEncoder.filterEncode(value);
	}

	/**
	 * Convenience constructor for <code>int</code> values.
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
		buff.append(this.attribute).append(getCompareString()).append(this.encodedValue);
		buff.append(')');

		return buff;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CompareFilter that = (CompareFilter) o;

		if ((this.attribute != null) ? !this.attribute.equals(that.attribute) : that.attribute != null) {
			return false;
		}
		if ((this.value != null) ? !this.value.equals(that.value) : that.value != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = (this.attribute != null) ? this.attribute.hashCode() : 0;
		result = 31 * result + ((this.value != null) ? this.value.hashCode() : 0);
		return result;
	}

	/**
	 * Implement this method in subclass to return a String representing the operator. The
	 * {@link EqualsFilter#getCompareString()} would for example return an equals sign,
	 * &quot;=&quot;.
	 * @return the String to use as operator in the comparison for the specific subclass.
	 */
	protected abstract String getCompareString();

}
