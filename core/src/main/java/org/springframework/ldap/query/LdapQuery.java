/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.query;

import org.springframework.ldap.filter.Filter;

import javax.naming.Name;

/**
 * Holds all information regarding a Ldap query to be performed. Contains information regarding search base,
 * search scope, time and count limits, and search filter.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 * @see LdapQueryBuilder
 *
 * @see org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.AttributesMapper)
 * @see org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)
 * @see org.springframework.ldap.core.LdapOperations#searchForObject(LdapQuery, org.springframework.ldap.core.ContextMapper)
 * @see org.springframework.ldap.core.LdapOperations#searchForContext(LdapQuery)
 */
public interface LdapQuery {
    /**
     * Get the search base. Default is {@link org.springframework.ldap.support.LdapUtils#emptyLdapName()}.
     *
     * @return the search base.
     */
    Name base();

    /**
     * Get the search scope. Default is <code>null</code>, indicating that the LdapTemplate default should be used.
     *
     * @return the search scope.
     */
    SearchScope searchScope();

    /**
     * Get the time limit. Default is <code>null</code>, indicating that the LdapTemplate default should be used.
     *
     * @return the time limit.
     */
    Integer timeLimit();

    /**
     * Get the count limit. Default is <code>null</code>, indicating that the LdapTemplate default should be used.
     * @return the count limit.
     */
    Integer countLimit();

    /**
     * Get the attributes to return. Default is <code>null</code>, indicating that all attributes should be returned.
     *
     * @return the attributes to return.
     */
    String[] attributes();

    /**
     * Get the filter.
     *
     * @return the filter.
     */
    Filter filter();
}
