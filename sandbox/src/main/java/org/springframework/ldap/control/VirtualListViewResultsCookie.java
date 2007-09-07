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

import com.sun.jndi.ldap.ctl.VirtualListViewControl;

/**
 * Wrapper class for the cookie returned when using the
 * {@link VirtualListViewControl}.
 * 
 * @author Ulrik Sandberg
 */
public class VirtualListViewResultsCookie {

    private byte[] cookie;

    private int contentCount;

    private int targetPosition;

    /**
     * Constructor.
     * 
     * @param cookie
     *            the cookie returned by a VirtualListViewResponseControl.
     * @param targetPosition TODO
     * @param contentCount TODO
     */
    public VirtualListViewResultsCookie(byte[] cookie, int targetPosition, int contentCount) {
        this.cookie = cookie;
        this.targetPosition = targetPosition;
        this.contentCount = contentCount;
    }

    /**
     * Get the cookie.
     * 
     * @return the cookie.
     */
    public byte[] getCookie() {
        return cookie;
    }

    public int getContentCount() {
        return contentCount;
    }

    public int getTargetPosition() {
        return targetPosition;
    }
}
