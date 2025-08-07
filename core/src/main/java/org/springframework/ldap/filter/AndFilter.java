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
 * A filter for a logical AND. Example:
 *
 * <pre>
 *	 AndFilter filter = new AndFilter();
 *	 filter.and(new EqualsFilter(&quot;objectclass&quot;, &quot;person&quot;);
 *	 filter.and(new EqualsFilter(&quot;cn&quot;, &quot;Some CN&quot;);
 *	 System.out.println(filter.encode());
 * </pre>
 *
 * would result in: <code>(&amp;(objectclass=person)(cn=Some CN))</code>
 *
 * @author Adam Skogman
 * @author Mattias Hellborg Arthursson
 * @see org.springframework.ldap.filter.EqualsFilter
 */
public class AndFilter extends BinaryLogicalFilter {

	private static final String AMPERSAND = "&";

	/*
	 * @see org.springframework.ldap.filter.BinaryLogicalFilter#getLogicalOperator()
	 */
	protected String getLogicalOperator() {
		return AMPERSAND;
	}

	/**
	 * Add a query to the AND expression.
	 * @param query The expression to AND with the rest of the AND:ed expressions.
	 * @return This LdapAndQuery
	 */
	public AndFilter and(Filter query) {
		append(query);
		return this;
	}

}
