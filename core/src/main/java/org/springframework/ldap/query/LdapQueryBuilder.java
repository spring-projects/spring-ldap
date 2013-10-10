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
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import java.text.MessageFormat;

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
 * @since 2.0
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
     * @throws IllegalStateException if a filter has already been specified.
     */
    public ConditionCriteria where(String attribute) {
        initRootContainer();
        return new DefaultConditionCriteria(rootContainer, attribute);
    }

    private void initRootContainer() {
        assertFilterNotStarted();
        rootContainer = new DefaultContainerCriteria(this);
    }

    /**
     * Specify a hardcoded filter. Please note that using this method, the filter string will not be
     * validated or escaped in any way. <b>Never</b> use direct user input and use it concatenating strings
     * to use as LDAP filters. Doing so opens up for &quot;LDAP injection&quot;, where malicious user
     * may inject specifically constructed data to form filters at their convenience. When user input is used
     * consider using {@link #where(String)} or {@link #filter(String, String...)} instead.
     *
     * @param hardcodedFilter The hardcoded filter string to use in the search.
     * @return this instance.
     * @throws IllegalStateException if a filter has already been specified.
     */
    public LdapQuery filter(String hardcodedFilter) {
        initRootContainer();
        rootContainer.append(new HardcodedFilter(hardcodedFilter));
        return this;
    }

    public LdapQuery filter(Filter filter) {
        initRootContainer();
        rootContainer.append(filter);
        return this;
    }

    /**
     * Specify a hardcoded filter using the specified parameters. The parameters will be properly encoded using
     * {@link LdapEncoder#filterEncode(String)} to make sure no malicious data gets through. The <code>filterFormat</code>
     * String should be formatted for input to {@link MessageFormat#format(String, Object...)}.
     *
     * @param filterFormat the filter format string, formatted for input to {@link MessageFormat#format(String, Object...)}.
     * @param params the parameters that will be used for building the final filter. All parameters will be properly encoded.
     * @return this instance.
     * @throws IllegalStateException if a filter has already been specified.
     */
    public LdapQuery filter(String filterFormat, String... params) {
        Object[] encodedParams = new String[params.length];

        for (int i=0; i < params.length; i++) {
            encodedParams[i] = LdapEncoder.filterEncode(params[i]);
        }

        return filter(MessageFormat.format(filterFormat, encodedParams));
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