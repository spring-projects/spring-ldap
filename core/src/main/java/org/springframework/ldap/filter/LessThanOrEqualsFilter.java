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
 * A filter to compare &lt;=. LDAP RFC does not allow &lt; comparison. The following code:
 *
 * <pre>
 * LessThanOrEqualsFilter filter = new LessThanOrEqualsFilter(&quot;cn&quot;, &quot;Some CN&quot;);
 * System.out.println(filter.ecode());
 * </pre>
 *
 * would result in:
 *
 * <pre>
 * (cn&lt;=Some CN)
 * </pre>
 *
 * @author Mattias Hellborg Arthursson
 */
public class LessThanOrEqualsFilter extends CompareFilter {

	private static final String LESS_THAN_OR_EQUALS = "<=";

	public LessThanOrEqualsFilter(String attribute, String value) {
		super(attribute, LESS_THAN_OR_EQUALS, value, LdapEncoder.filterEncode(value));
	}

	public LessThanOrEqualsFilter(String attribute, int value) {
		this(attribute, String.valueOf(value));
	}

	/**
	 * @deprecated please extend {@link CompareFilter} instead
	 * @see org.springframework.ldap.filter.CompareFilter#getCompareString()
	 */
	@Deprecated(forRemoval = true, since = "3.3")
	protected String getCompareString() {
		return LESS_THAN_OR_EQUALS;
	}

}
