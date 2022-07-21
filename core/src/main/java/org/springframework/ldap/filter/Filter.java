/*
 * Copyright 2005-2010 the original author or authors.
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
 * Common interface for LDAP filters.
 * 
 * @author Adam Skogman
 * @see <a href="https://www.ietf.org/rfc/rfc1960.txt">RFC 1960: A String
 * Representation of LDAP Search Filters</a>
 */
public interface Filter {

	/**
	 * Encodes the filter to a String.
	 * 
	 * @return The encoded filter in the standard String format
	 */
	String encode();

	/**
	 * Encodes the filter to a StringBuffer.
	 * 
	 * @param buf The StringBuffer to encode the filter to
	 * @return The same StringBuffer as was given
	 */
	StringBuffer encode(StringBuffer buf);

	/**
	 * All filters must implement equals.
	 * 
	 * @param o
	 * @return <code>true</code> if the objects are equal.
	 */
	boolean equals(Object o);

	/**
	 * All filters must implement hashCode.
	 * 
	 * @return the hash code according to the contract in
	 * {@link Object#hashCode()}
	 */
	int hashCode();
}