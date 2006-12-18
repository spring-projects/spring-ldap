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

package org.springframework.ldap.support.control;

import com.sun.jndi.ldap.ctl.VirtualListViewControl;

/**
 * Wrapper class for the cookie returned when using the
 * {@link VirtualListViewControl}.
 * 
 * @author Ulrik Sandberg
 */
public class VirtualListViewResultsCookie {

    private byte[] cookie;

    /**
     * Constructor.
     * 
     * @param cookie
     *            the cookie returned by a VirtualListViewResponseControl.
     */
    public VirtualListViewResultsCookie(byte[] cookie) {
        this.cookie = cookie;
    }

    /**
     * Get the cookie.
     * 
     * @return the cookie.
     */
    public byte[] getCookie() {
        return cookie;
    }
}
