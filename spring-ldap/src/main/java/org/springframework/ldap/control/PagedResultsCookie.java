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

package org.springframework.ldap.control;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sun.jndi.ldap.ctl.PagedResultsControl;

/**
 * Wrapper class for the cookie returned when using the
 * {@link PagedResultsControl}.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class PagedResultsCookie {

    private byte[] cookie;

    /**
     * Constructor.
     * 
     * @param cookie
     *            the cookie returned by a PagedResultsResponseControl.
     */
    public PagedResultsCookie(byte[] cookie) {
        this.cookie = ArrayUtils.clone(cookie);
    }

    /**
     * Get the cookie.
     * 
     * @return the cookie.
     */
    public byte[] getCookie() {
        return ArrayUtils.clone(cookie);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj != null && this.getClass().equals(obj.getClass())) {
            PagedResultsCookie that = (PagedResultsCookie) obj;
            return new EqualsBuilder().append(this.cookie, that.cookie)
                    .isEquals();
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder().append(this.cookie).toHashCode();
    }
}
