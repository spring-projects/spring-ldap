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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.LdapEncoder;

/**
 * This filter automatically converts all whitespace to wildcards (*). The
 * following code:
 * 
 * <pre>
 * WhitespaceWildcardsFilter filter = new WhitespaceWildcardsFilter(&quot;cn&quot;, &quot;Some CN&quot;);
 * System.out.println(filter.ecode());
 * </pre>
 * 
 * would result in: <code>(cn=*Some*CN*)</code>
 * 
 * @author Adam Skogman
 * @author Mattias Arthursson
 */
public class WhitespaceWildcardsFilter extends EqualsFilter {
	private static Pattern starReplacePattern = Pattern.compile("\\s+");

	public WhitespaceWildcardsFilter(String attribute, String value) {
		super(attribute, value);
	}

	/*
	 * @see org.springframework.ldap.filter.CompareFilter#encodeValue(java.lang.String)
	 */
	protected String encodeValue(String value) {

		// blank string means just ONE star
		if (StringUtils.isBlank(value)) {
			return "*";
		}

		// trim value, we will add in stars first and last anywhay
		value = value.trim();

		// filter encode so that any stars etc. are preserved
		String filterEncoded = LdapEncoder.filterEncode(value);

		// Now replace all whitespace with stars
		Matcher m = starReplacePattern.matcher(filterEncoded);

		// possibly 2 longer (stars at ends)
		StringBuffer buff = new StringBuffer(value.length() + 2);

		buff.append('*');

		while (m.find()) {
			m.appendReplacement(buff, "*");
		}
		m.appendTail(buff);

		buff.append('*');

		return buff.toString();
	}
}
