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

/**
 * A filter for 'equals'. The following code:
 *
 * <pre>
 * EqualsFilter filter = new EqualsFilter(&quot;cn&quot;, &quot;Some CN&quot;);
 * System.out.println(filter.encode());
 * </pre>
 *
 * would result in:
 *
 * <pre>
 * (cn=Some CN)
 * </pre>
 *
 * @author Adam Skogman
 */
public class EqualsFilter extends CompareFilter {

	private static final String EQUALS_SIGN = "=";

	public EqualsFilter(String attribute, String value) {
		super(attribute, value);
	}

	/**
	 * Constructor for binary values, such as {@code objectGUID} or {@code objectSid}.
	 * Each byte of the value is escaped as {@code \xx}, so the filter matches the exact
	 * octets supplied.
	 * @param attribute Name of attribute in filter.
	 * @param value The binary value of the attribute in the filter.
	 * @since 4.3
	 */
	public EqualsFilter(String attribute, byte[] value) {
		this(attribute, filterEncode(value), filterEncode(value));
	}

	EqualsFilter(String attribute, String value, String encodedValue) {
		super(attribute, EQUALS_SIGN, value, encodedValue);
	}

	private static String filterEncode(byte[] value) {
		StringBuilder encodedValue = new StringBuilder(value.length * 3);
		for (byte b : value) {
			encodedValue.append('\\');
			encodedValue.append(Character.forDigit((b >> 4) & 0xf, 16));
			encodedValue.append(Character.forDigit(b & 0xf, 16));
		}
		return encodedValue.toString();
	}

	/**
	 * Convenience constructor for int values.
	 * @param attribute Name of attribute in filter.
	 * @param value The value of the attribute in the filter.
	 */
	public EqualsFilter(String attribute, int value) {
		super(attribute, value);
	}

	/**
	 * {@inheritDoc}
	 * @deprecated please extend {@link CompareFilter} instead
	 * @see org.springframework.ldap.filter.CompareFilter#getCompareString()
	 */
	@Override
	@Deprecated(forRemoval = true, since = "3.3")
	protected String getCompareString() {
		return EQUALS_SIGN;
	}

}
