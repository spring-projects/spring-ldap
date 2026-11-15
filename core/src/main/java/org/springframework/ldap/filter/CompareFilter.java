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

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Abstract superclass for filters that compare values.
 *
 * @author Mattias Hellborg Arthursson
 */
public abstract class CompareFilter extends AbstractFilter {

	private final String attribute;

	private final @Nullable String value;

	private final String encodedValue;

	private final String operator;

	/**
	 * For testing purposes.
	 * @return the encoded value.
	 */
	String getEncodedValue() {
		return this.encodedValue;
	}

	/**
	 * Construct a filter, specifying the comparison {@code operator} as well as the
	 * already-encoded value.
	 * @param attribute the attribute name
	 * @param operator the comparison operator; for example, {@code =}, {@code ~=}
	 * @param value the value of the attribute in the filter.
	 * @param encodedValue the already-encoded value
	 * @throws IllegalArgumentException if the attribute is not a valid RFC 4512 attribute
	 * description
	 * @since 3.3
	 */
	protected CompareFilter(String attribute, String operator, @Nullable String value, String encodedValue) {
		this.attribute = AttributeDescriptions.validate(attribute);
		this.value = value;
		this.encodedValue = encodedValue;
		this.operator = operator;
	}

	/*
	 * @see org.springframework.ldap.filter.AbstractFilter#encode(java.lang.StringBuffer)
	 */
	public StringBuffer encode(StringBuffer buff) {
		buff.append('(');
		buff.append(this.attribute).append(this.operator).append(this.encodedValue);
		buff.append(')');

		return buff;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof CompareFilter that) || this.getClass() != that.getClass()) {
			return false;
		}

		return Objects.equals(this.attribute, that.attribute) && Objects.equals(this.value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.attribute, this.value);
	}

}
