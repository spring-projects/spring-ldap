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

/**
 * Filter for logical OR.
 * 
 * <pre>
 *        AndFilter filter = new AndFilter();
 *        filter.or(new EqualsFilter(&quot;objectclass&quot;, &quot;person&quot;);
 *        filter.or(new EqualsFilter(&quot;objectclass&quot;, &quot;organizationalUnit&quot;);
 *        System.out.println(filter.ecode());    
 * </pre>
 * 
 * would result in:
 * <code>(|(objectclass=person)(objectclass=organizationalUnit))</code>
 * 
 * @author Adam Skogman
 * @author Mattias Arthursson
 */
public class OrFilter extends BinaryLogicalFilter {

	private static final String PIPE_SIGN = "|";

	/**
	 * Add a query to the OR expression
	 * 
	 * @param query The query to or with the rest of the or:ed queries.
	 * @return This LdapOrQuery
	 */
	public OrFilter or(Filter query) {
		queryList.add(query);
		return this;
	}

	/*
	 * @see org.springframework.ldap.filter.BinaryLogicalFilter#getLogicalOperator()
	 */
	protected String getLogicalOperator() {
		return PIPE_SIGN;
	}
}
