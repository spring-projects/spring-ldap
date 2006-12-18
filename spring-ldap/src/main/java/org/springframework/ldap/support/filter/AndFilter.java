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

package org.springframework.ldap.support.filter;

/**
 * A filter for a logical AND. E.g.:
 * 
 * <pre>
 *     AndFilter filter = new AndFilter();
 *     filter.and(new EqualsFilter(&quot;objectclass&quot;, &quot;person&quot;);
 *     filter.and(new EqualsFilter(&quot;cn&quot;, &quot;Some CN&quot;);
 *     System.out.println(filter.ecode());    
 * </pre>
 * 
 * would result in: <code>(&amp;(objectclass=person)(cn=Some CN))</code>
 * 
 * @see org.springframework.ldap.support.filter.EqualsFilter
 * @author Adam Skogman
 * @author Mattias Arthursson
 */
public class AndFilter extends BinaryLogicalFilter {

    private static final String AMPERSAND = "&";

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.filter.BinaryLogicalFilter#getLogicalOperator()
     */
    protected String getLogicalOperator() {
        return AMPERSAND;
    }

    /**
     * Add a query to the and expression
     * 
     * @param query
     *            The query to and with the rest of the and:ed queries.
     * @return This LdapAndQuery
     */
    public AndFilter and(Filter query) {
        queryList.add(query);
        return this;
    }
}
