/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ldap.support.filter;

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

    /**
     * Prints the query with LDAP encoding to a stringbuffer
     * 
     * @param buff
     *            The stringbuffer
     * @return The very same stringbuffer
     */
    public abstract StringBuffer encode(StringBuffer buff);

    /**
     * Encodes the filter to a string using the (@link #encode(StringBuffer)
     * method.
     * 
     * @return The encoded filter
     */
    public String encode() {
        StringBuffer buff = new StringBuffer(256);
        buff = encode(buff);
        return buff.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return encode();
    }
}
