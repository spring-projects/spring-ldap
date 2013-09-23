/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.query;

import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;

/**
 * Builder of LdapQueries. Start with a call to {@link #query()}, proceed with specifying the
 * basic search configuration (e.g. search base, time limit, etc.), finally specify the actual query.
 * Example:
 * <pre>
 * import static org.springframework.ldap.query.LdapQueryBuilder.query;
 * ...
 *
 * LdapQuery query = query()
 *  .base("dc=261consulting, dc=com")
 *  .searchScope(SearchScope.ONELEVEL)
 *  .timeLimit(200)
 *  .countLimit(221)
 *  .where("objectclass").is("person").and("cn").is("John Doe");
 * </pre>
 * <p>
 *     Default configuration is that base path is {@link org.springframework.ldap.support.LdapUtils#emptyLdapName()}.
 *     All other parameters are undefined, meaning that (in the case of base search parameters), the LdapTemplate
 *     defaults will be used. Filter conditions must always be specified.
 * </p>
 * @author Mattias Hellborg Arthursson
 *
 * @see javax.naming.directory.SearchControls
 * @see org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.AttributesMapper)
 * @see org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)
 * @see org.springframework.ldap.core.LdapOperations#searchForObject(LdapQuery, org.springframework.ldap.core.ContextMapper)
 * @see org.springframework.ldap.core.LdapOperations#searchForContext(LdapQuery)
 */
public class LdapQueryBuilder implements LdapQuery {
    private Name base = LdapUtils.emptyLdapName();
    private SearchScope searchScope = null;
    private Integer countLimit = null;
    private Integer timeLimit = null;
    private String[] attributes = null;

    private DefaultContainerCriteria rootContainer = null;

    /**
     * Not to be instantiated directly - use static query() method.
     */
    private LdapQueryBuilder() {

    }

    /**
     * Construct a new LdapQueryBuilder.
     *
     * @return a new instance.
     */
    public static LdapQueryBuilder query() {
        return new LdapQueryBuilder();
    }

    /**
     * Set the base search path for the query.
     * Default is {@link org.springframework.ldap.support.LdapUtils#emptyLdapName()}.
     *
     * @param baseDn the base search path.
     * @return this instance.
     */
    public LdapQueryBuilder base(String baseDn) {
        assertFilterNotStarted();
        this.base = LdapUtils.newLdapName(baseDn);
        return this;
    }

    /**
     * Set the base search path for the query.
     * Default is {@link org.springframework.ldap.support.LdapUtils#emptyLdapName()}.
     *
     * @param baseDn the base search path.
     * @return this instance.
     */
    public LdapQueryBuilder base(Name baseDn) {
        assertFilterNotStarted();
        this.base = LdapUtils.newLdapName(baseDn);
        return this;
    }

    /**
     * Set the search scope for the query.
     * Default is {@link SearchScope#SUBTREE}.
     *
     * @param searchScope the search scope.
     * @return this instance.
     */
    public LdapQueryBuilder searchScope(SearchScope searchScope) {
        assertFilterNotStarted();
        this.searchScope = searchScope;
        return this;
    }

    /**
     * Set the count limit for the query.
     * Default is 0 (no limit).
     *
     * @param countLimit the count limit.
     * @return this instance.
     */
    public LdapQueryBuilder countLimit(int countLimit) {
        assertFilterNotStarted();
        this.countLimit = countLimit;
        return this;
    }

    public LdapQueryBuilder attributes(String... attributesToReturn) {
        assertFilterNotStarted();
        this.attributes = attributesToReturn;
        return this;
    }

    /**
     * Set the time limit for the query.
     * Default is 0 (no limit).
     *
     * @param timeLimit the time limit.
     * @return this instance.
     */
    public LdapQueryBuilder timeLimit(int timeLimit) {
        assertFilterNotStarted();
        this.timeLimit = timeLimit;
        return this;
    }

    /**
     * Start specifying the filter conditions in this query.
     *
     * @param attribute The attribute that the first part of the filter should test against.
     * @return A ConditionCriteria instance for specifying the compare operation.
     */
    public ConditionCriteria where(String attribute) {
        assertFilterNotStarted();
        rootContainer = new DefaultContainerCriteria(this);
        return new DefaultConditionCriteria(rootContainer, attribute);
    }

    private void assertFilterNotStarted() {
        if(rootContainer != null) {
            throw new IllegalStateException("Invalid operation - filter condition specification already started");
        }
    }

    @Override
    public Name base() {
        return base;
    }

    @Override
    public SearchScope searchScope() {
        return searchScope;
    }

    @Override
    public Integer countLimit() {
        return countLimit;
    }

    @Override
    public Integer timeLimit() {
        return timeLimit;
    }

    @Override
    public String[] attributes() {
        return attributes;
    }


    @Override
    public Filter filter() {
        if(rootContainer == null) {
            throw new IllegalStateException("No filter conditions have been specified specified");
        }
        return rootContainer.filter();
    }
}