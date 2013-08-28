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
package org.springframework.ldap.filter;

/**
 * A convenience class that combines {@code NOT} behavior with {@code present}
 * behavior to allow the user to check for the non-existence of a attribute. For
 * an attribute to be {@code NOT present} it must not have any values set. To
 * filter on attributes at are {@code present} use the {@link PresentFilter}.
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
 * @author Jordan Hein
 */
public class NotPresentFilter extends AbstractFilter {
	
	private String attribute;

	/**
	 * Creates a new instance of a not present filter for a particular
	 * attribute.
	 * 
	 * @param attribute the attribute expected to be not-present (ie, unset, or
	 * null).
	 */
	public NotPresentFilter(String attribute) {
		this.attribute = attribute;
	}

	public StringBuffer encode(StringBuffer buff) {
		buff.append("(!(");
		buff.append(attribute);
		buff.append("=*))");
		return buff;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotPresentFilter that = (NotPresentFilter) o;

        if (attribute != null ? !attribute.equals(that.attribute) : that.attribute != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return attribute != null ? attribute.hashCode() : 0;
    }
}