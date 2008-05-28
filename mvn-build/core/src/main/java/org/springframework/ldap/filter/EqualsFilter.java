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
 * A filter for 'equals'. The following code:
 * 
 * <pre>
 * EqualsFilter filter = new EqualsFilter(&quot;cn&quot;, &quot;Some CN&quot;);
 * System.out.println(filter.ecode());
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
	 * Convenience constructor for int values.
	 * 
	 * @param attribute Name of attribute in filter.
	 * @param value The value of the attribute in the filter.
	 */
	public EqualsFilter(String attribute, int value) {
		super(attribute, value);
	}

	/*
	 * @see org.springframework.ldap.filter.CompareFilter#getCompareString()
	 */
	protected String getCompareString() {
		return EQUALS_SIGN;
	}
}
