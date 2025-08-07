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

/**
 * A parser for RFC2253-compliant Distinguished Names.
 *
 * @author Mattias Hellborg Arthursson
 * @deprecated {@link DistinguishedName} and associated classes are deprecated as of 2.0.
 */
public interface DnParser {

	/**
	 * Parse a full Distinguished Name.
	 * @return the <code>DistinguishedName</code> corresponding to the parsed stream.
	 */
	DistinguishedName dn() throws ParseException;

	/**
	 * Parse a Relative Distinguished Name.
	 * @return the next rdn on the stream.
	 */
	LdapRdn rdn() throws ParseException;

}
