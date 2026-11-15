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

import org.jspecify.annotations.Nullable;

import org.springframework.ldap.support.LdapEncoder;

/**
 * This filter allows the user to specify wildcards (*) by not escaping them in the
 * filter. The following code:
 *
 * <pre>
 * LikeFilter filter = new LikeFilter(&quot;cn&quot;, &quot;foo*&quot;);
 * System.out.println(filter.encode());
 * </pre>
 *
 * would result in:
 *
 * <pre>
 *  (cn=foo*)
 * </pre>
 *
 * @author Anders Henja
 * @author Mattias Hellborg Arthursson
 * @author Ngoc Nhan
 */
public final class LikeFilter extends CompareFilter {

	private static final String EQUALS_SIGN = "=";

	/**
	 * Convenience constructor for {@code String} value.
	 * @param attribute The name of attribute in filter.
	 * @param value The value of the attribute in the filter.
	 */
	public LikeFilter(String attribute, @Nullable String value) {
		super(attribute, EQUALS_SIGN, value, encodeValueInternal(value));
	}

	private static String encodeValueInternal(@Nullable String value) {
		// just return if blank string
		if (value == null) {
			return "";
		}

		String[] substrings = value.split("\\*", -2);

		if (substrings.length == 1) {
			return LdapEncoder.filterEncode(substrings[0]);
		}

		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < substrings.length; i++) {
			buff.append(LdapEncoder.filterEncode(substrings[i]));
			if (i < substrings.length - 1) {
				buff.append("*");
			}
		}

		return buff.toString();
	}

}
