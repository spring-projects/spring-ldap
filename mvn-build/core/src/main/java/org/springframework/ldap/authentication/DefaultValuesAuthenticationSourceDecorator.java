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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.AuthenticationSource;

/**
 * Decorator on AuthenticationSource to have default authentication information
 * be returned should the target return empty principal and credentials. Useful
 * in combination with {@link AcegiAuthenticationSource} if users are to be
 * allowed to read some information even though they are not logged in.
 * <p>
 * <b>Note:</b> The <code>defaultUser</code> should be an non-privileged
 * user. This is important as this is the one that will be used when no user is
 * logged in (i.e. empty principal is returned from the target
 * AuthenticationSource).
 * 
 * @author Mattias Arthursson
 * 
 */
public class DefaultValuesAuthenticationSourceDecorator implements
        AuthenticationSource, InitializingBean {

    private AuthenticationSource target;

    private String defaultUser;

    private String defaultPassword;

    /**
     * Constructor for bean usage.
     */
    public DefaultValuesAuthenticationSourceDecorator() {
    }

    /**
     * Constructor to setup instance directly.
     * 
     * @param target
     *            the target AuthenticationSource.
     * @param defaultUser
     *            dn of the user to use when the target returns an empty
     *            principal.
     * @param defaultPassword
     *            password of the user to use when the target returns an empty
     *            principal.
     */
    public DefaultValuesAuthenticationSourceDecorator(
            AuthenticationSource target, String defaultUser,
            String defaultPassword) {
        this.target = target;
        this.defaultUser = defaultUser;
        this.defaultPassword = defaultPassword;
    }

    /**
     * Checks if the target's principal is not empty; if not, the credentials
     * from the target is returned - otherwise return the
     * <code>defaultPassword</code>.
     * 
     * @return the target's password if the target's principal is not empty, the
     *         <code>defaultPassword</code> otherwise.
     */
    public String getCredentials() {
        if (StringUtils.isNotEmpty(target.getPrincipal())) {
            return target.getCredentials();
        } else {
            return defaultPassword;
        }
    }

    /**
     * Checks if the target's principal is not empty; if not, this is returned -
     * otherwise return the <code>defaultPassword</code>.
     * 
     * @return the target's principal if it is not empty, the
     *         <code>defaultPassword</code> otherwise.
     */
    public String getPrincipal() {
        String principal = target.getPrincipal();
        if (StringUtils.isNotEmpty(principal)) {
            return principal;
        } else {
            return defaultUser;
        }
    }

    /**
     * Set the password of the default user.
     * 
     * @param defaultPassword
     *            the password of the default user.
     */
    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    /**
     * Set the default user DN. This should be a non-privileged user, since it
     * will be used when no authentication information is returned from the
     * target.
     * 
     * @param defaultUser
     *            DN of the default user.
     */
    public void setDefaultUser(String defaultUser) {
        this.defaultUser = defaultUser;
    }

    /**
     * Set the target AuthenticationSource.
     * 
     * @param target
     *            the target AuthenticationSource.
     */
    public void setTarget(AuthenticationSource target) {
        this.target = target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (target == null) {
            throw new IllegalArgumentException(
                    "Property 'target' must be set.'");
        }

        if (defaultUser == null) {
            throw new IllegalArgumentException(
                    "Property 'defaultUser' must be set.'");
        }

        if (defaultPassword == null) {
            throw new IllegalArgumentException(
                    "Property 'defaultPassword' must be set.'");
        }
    }
}
