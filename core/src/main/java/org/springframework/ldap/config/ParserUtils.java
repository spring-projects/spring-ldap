/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.config;

import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Mattias Hellborg Arthursson
 */
final class ParserUtils {
	static final String NAMESPACE = "http://www.springframework.org/schema/ldap";

	/**
	 * Not to be instantiated
	 */
	private ParserUtils() {

	}

	static boolean getBoolean(Element element, String attribute, boolean defaultValue) {
		String theValue = element.getAttribute(attribute);
		if (StringUtils.hasText(theValue)) {
			return Boolean.valueOf(theValue);
		}

		return defaultValue;
	}

	static String getString(Element element, String attribute, String defaultValue) {
		String theValue = element.getAttribute(attribute);
		if (StringUtils.hasText(theValue)) {
			return theValue;
		}

		return defaultValue;
	}

	static int getInt(Element element, String attribute, int defaultValue) {
		String theValue = element.getAttribute(attribute);
		if (StringUtils.hasText(theValue)) {
			return Integer.parseInt(theValue);
		}

		return defaultValue;
	}
}
