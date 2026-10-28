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

import java.util.regex.Pattern;

import org.springframework.util.Assert;

/**
 * Validates the attribute names that filters write into the filter string unescaped.
 * <p>
 * A valid name is an attribute description as defined by
 * <a href="https://datatracker.ietf.org/doc/html/rfc4512#section-2.5">RFC 4512, section
 * 2.5</a>: either a descriptor (a letter followed by letters, digits or hyphens) or a
 * numeric OID, optionally followed by {@code ;option} suffixes.
 *
 * @author Josh Cummings
 * @since 4.0.6
 */
final class AttributeDescriptions {

	private static final String KEYCHARS = "[A-Za-z0-9-]";

	private static final String NUMBER = "(?:0|[1-9][0-9]*)";

	private static final Pattern ATTRIBUTE_DESCRIPTION = Pattern
		.compile("(?:[A-Za-z][A-Za-z0-9-]*|" + NUMBER + "(?:\\." + NUMBER + ")+)(?:;" + KEYCHARS + "+)*");

	private AttributeDescriptions() {
	}

	/**
	 * Ensure that the given attribute name cannot change the structure of a filter.
	 * @param attribute the attribute name to validate
	 * @return the validated attribute name
	 * @throws IllegalArgumentException if the name is not a valid attribute description
	 */
	static String validate(String attribute) {
		Assert.notNull(attribute, "attribute must not be null");
		Assert.isTrue(ATTRIBUTE_DESCRIPTION.matcher(attribute).matches(),
				() -> "'" + attribute + "' is not a valid attribute description");
		return attribute;
	}

}
