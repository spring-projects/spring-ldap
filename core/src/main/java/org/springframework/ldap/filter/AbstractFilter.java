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
 * Convenience class that implements most of the methods in the Filter
 * interface.
 * 
 * @author Adam Skogman
 */
public abstract class AbstractFilter implements Filter {

	protected AbstractFilter() {
		super();
	}

	/*
	 * @see org.springframework.ldap.filter.Filter#encode(java.lang.StringBuffer)
	 */
	public abstract StringBuffer encode(StringBuffer buff);

	/*
	 * @see org.springframework.ldap.filter.Filter#encode()
	 */
	public String encode() {
		StringBuffer buf = new StringBuffer(256);
		buf = encode(buf);
		return buf.toString();
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return encode();
	}
}
