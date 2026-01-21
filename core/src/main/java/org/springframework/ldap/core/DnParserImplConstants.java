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

package org.springframework.ldap.core;

import org.jspecify.annotations.NullUnmarked;

import org.springframework.ldap.support.LdapUtils;

/**
 * @deprecated As of 2.0 it is recommended to use {@link javax.naming.ldap.LdapName} along
 * with utility methods in {@link LdapUtils} instead.
 */
@Deprecated
@NullUnmarked
public interface DnParserImplConstants {

	/** End of File. */
	int EOF = 0;

	/** RegularExpression Id. */
	int ALPHA = 1;

	/** RegularExpression Id. */
	int DIGIT = 2;

	/** RegularExpression Id. */
	int LEADCHAR = 3;

	/** RegularExpression Id. */
	int STRINGCHAR = 4;

	/** RegularExpression Id. */
	int TRAILCHAR = 5;

	/** RegularExpression Id. */
	int SPECIAL = 6;

	/** RegularExpression Id. */
	int HEXCHAR = 7;

	/** RegularExpression Id. */
	int HEXPAIR = 8;

	/** RegularExpression Id. */
	int BACKSLASHCHAR = 9;

	/** RegularExpression Id. */
	int PAIR = 10;

	/** RegularExpression Id. */
	int ESCAPEDSTART = 11;

	/** RegularExpression Id. */
	int QUOTECHAR = 12;

	/** RegularExpression Id. */
	int HASHCHAR = 13;

	/** RegularExpression Id. */
	int ATTRIBUTE_TYPE_STRING = 14;

	/** RegularExpression Id. */
	int LDAP_OID = 15;

	/** RegularExpression Id. */
	int SPACE = 16;

	/** RegularExpression Id. */
	int ATTRVALUE = 17;

	/** RegularExpression Id. */
	int SPACED_EQUALS = 18;

	/** Lexical state. */
	int DEFAULT = 0;

	/** Lexical state. */
	int ATTRVALUE_S = 1;

	/** Lexical state. */
	int SPACED_EQUALS_S = 2;

	/** Literal token values. */
	String[] tokenImage = { "<EOF>", "<ALPHA>", "<DIGIT>", "<LEADCHAR>", "<STRINGCHAR>", "<TRAILCHAR>", "<SPECIAL>",
			"<HEXCHAR>", "<HEXPAIR>", "\"\\\\\"", "<PAIR>", "<ESCAPEDSTART>", "\"\\\"\"", "\"#\"",
			"<ATTRIBUTE_TYPE_STRING>", "<LDAP_OID>", "\" \"", "<ATTRVALUE>", "<SPACED_EQUALS>", "\",\"", "\";\"",
			"\"+\"", };

}
