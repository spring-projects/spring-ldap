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
package org.springframework.ldap.authentication;

import junit.framework.TestCase;

public class ThreadLocalAuthenticationSourceTest extends TestCase {

    private ThreadLocalAuthenticationSource tested;

    protected void setUp() throws Exception {
        ThreadLocalAuthenticationHolder.setPrincipal(null);
        ThreadLocalAuthenticationHolder.setCredentials(null);

        tested = new ThreadLocalAuthenticationSource();
    }

    public void testGetPrincipal() {
        ThreadLocalAuthenticationHolder.setPrincipal("cn=john doe");
        String result = tested.getPrincipal();

        assertEquals("cn=john doe", result);
    }

    public void testGetPrincipal_NoDataBound() {
        String result = tested.getPrincipal();

        assertNull("Should be null", result);
    }

    public void testGetCredentials() {
        ThreadLocalAuthenticationHolder.setCredentials("secret");
        String result = tested.getCredentials();

        assertEquals("secret", result);
    }

    public void testGetCredentials_NoDataBound() {
        String result = tested.getCredentials();

        assertNull("Should be null", result);
    }
}
