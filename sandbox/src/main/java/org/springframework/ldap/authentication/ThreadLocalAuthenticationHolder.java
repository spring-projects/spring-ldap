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

/**
 * Holder for ThreadLocal values of principal and credentials, used by
 * {@link ThreadLocalAuthenticationSource} to get its values.
 * 
 * @author Mattias Arthursson
 * 
 */
public class ThreadLocalAuthenticationHolder {
    private static ThreadLocal threadPrincipal = new ThreadLocal();

    private static ThreadLocal threadCredentials = new ThreadLocal();

    /**
     * Not to be instantiated.
     */
    private ThreadLocalAuthenticationHolder() {
    }

    /**
     * Set the ThreadLocal principal DN.
     * 
     * @param principal
     *            the principal to be returned in this thread by
     *            {@link ThreadLocalAuthenticationSource#getPrincipal()}.
     */
    public static void setPrincipal(String principal) {
        threadPrincipal.set(principal);
    }

    /**
     * Set the ThreadLocal credentials.
     * 
     * @param credentials
     *            the credentials to be returned in this thread by
     *            {@link ThreadLocalAuthenticationSource#getCredentials()}.
     */
    public static void setCredentials(String credentials) {
        threadCredentials.set(credentials);
    }

    /**
     * Get the currently bound principal.
     * 
     * @return the currently bound principal.
     */
    public static String getPrincipal() {
        return (String) threadPrincipal.get();
    }

    /**
     * Get the currently bound credentials.
     * 
     * @return the currently bound credentials.
     */
    public static String getCredentials() {
        return (String) threadCredentials.get();
    }
}
