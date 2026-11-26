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

/**
 * A convenience class that combines {@code NOT} behavior with {@code present} behavior to
 * allow the user to check for the non-existence of an attribute. For an attribute to be
 * {@code NOT present} it must not have any values set. To filter on attributes at are
 * {@code present} use the {@link PresentFilter}.
 *
 * <pre>
 * NotPresentFilter filter = new NotPresentFilter(&quot;foo&quot;);
 * System.out.println(filter.encode());
 * </pre>
 *
 * would result in:
 *
 * <pre>
 *  (!(foo=*))
 * </pre>
 *
 * @author Jordan Hein
 * @author Ngoc Nhan
 */
public class NotPresentFilter extends AbstractFilter {

	private final String attribute;

	/**
	 * Creates a new instance of a not present filter for a particular attribute.
	 * @param attribute the attribute expected to be not-present (ie, unset, or null).
	 * @throws IllegalArgumentException if the attribute is not a valid RFC 4512 attribute
	 * description
	 */
	public NotPresentFilter(String attribute) {
		this.attribute = AttributeDescriptions.validate(attribute);
	}

	public StringBuffer encode(StringBuffer buff) {
		buff.append("(!(");
		buff.append(this.attribute);
		buff.append("=*))");
		return buff;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof NotPresentFilter that) || this.getClass() != that.getClass()) {
			return false;
		}

		return Objects.equals(this.attribute, that.attribute);
	}

	@Override
	public int hashCode() {
		return this.attribute.hashCode();
	}

}
