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

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.AuthenticationSource;

/**
 * An AuthenticationSource to retrieve authentication information stored in
 * Acegi's SecurityContextHolder. Use Acegi's LdapAuthenticationProvider have a
 * LdapUserDetails object placed in the authentication.
 * 
 * @author Mattias Arthursson
 * 
 */
public class AcegiAuthenticationSource implements AuthenticationSource {
    private static final Log log = LogFactory
            .getLog(AcegiAuthenticationSource.class);

    /**
     * Get the principals of the logged in user, in this case the distinguished
     * name.
     * 
     * @return the distinguished name of the logged in user.
     */
    public String getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof LdapUserDetails) {
                LdapUserDetails details = (LdapUserDetails) principal;
                return details.getDn();
            } else if (authentication instanceof AnonymousAuthenticationToken) {
                if (log.isDebugEnabled()) {
                    log
                            .debug("Anonymous Authentication, returning empty String as Principal");
                }
                return "";
            } else {
                throw new IllegalArgumentException(
                        "The principal property of the authentication object -"
                                + "needs to be a LdapUserDetails.");
            }
        } else {
            log.warn("No Authentication object set in SecurityContext - "
                    + "returning empty String as Principal");
            return "";
        }
    }

    /*
     * @see org.springframework.ldap.core.AuthenticationSource#getCredentials()
     */
    public String getCredentials() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication != null) {
            return (String) authentication.getCredentials();
        } else {
            log.warn("No Authentication object set in SecurityContext - "
                    + "returning empty String as Credentials");
            return "";
        }
    }

}
