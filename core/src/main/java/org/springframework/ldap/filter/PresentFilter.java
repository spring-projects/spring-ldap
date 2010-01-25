/*
 * Copyright 2005-2008 the original author or authors.
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

/**
 * Filter that allows the user to check for the existence of a attribute. For an
 * attribute to be {@code 'present'} it must contain a value. Attributes that do
 * not contain a value are {@code 'NOT present'}. To filter on attributes that
 * are {@code 'NOT present'} use the {@link NotPresentFilter} or use this filter
 * in combination with a {@link NotFilter} .
 * 
 * <pre>
 * PresentFilter filter = new PresentFilter(&quot;foo*&quot;);
 * System.out.println(filter.encode());
 * </pre>
 * 
 * would result in:
 * 
 * <pre>
 *  (foo=*)
 * </pre>
 * @author Jordan Hein
 */
public class PresentFilter extends AbstractFilter {

	private String attribute;

	/**
	 * Creates a new instance of a present filter for a particular attribute.
	 * 
	 * @param attribute the attribute expected to be present (ie, contains a
	 * value).
	 */
	public PresentFilter(String attribute) {
		this.attribute = attribute;
	}

	public StringBuffer encode(StringBuffer buff) {
		buff.append("(");
		buff.append(attribute);
		buff.append("=*)");
		return buff;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (o.getClass() != getClass()) {
			return false;
		}
		PresentFilter f = (PresentFilter) o;
		return new EqualsBuilder().append(this.attribute, f.attribute).isEquals();
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder().append(attribute);
		return builder.toHashCode();
	}
}