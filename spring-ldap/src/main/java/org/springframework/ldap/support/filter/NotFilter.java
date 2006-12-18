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

import org.apache.commons.lang.Validate;

/**
 * A filter for 'not'. The following code:
 * 
 * <pre>
 * Filter filter = new NotFilter(new EqualsFilter(&quot;cn&quot;, &quot;foo&quot;);
 * System.out.println(filter.ecode());
 * </pre>
 * 
 * would result in:
 * 
 * <pre>(!(cn=foo))</pre>
 * 
 * @author Adam Skogman
 */
public class NotFilter extends AbstractFilter {

    private final Filter filter;

    static private final int HASH = "!".hashCode();

    /**
     * Create a filter that negates the outcome of the given <code>filter</code>.
     * 
     * @param filter
     *            The filter that should be negated.
     */
    public NotFilter(Filter filter) {
        Validate.notNull(filter);
        this.filter = filter;
    }

    /**
     * @see org.springframework.ldap.support.filter.Filter#encode(java.lang.StringBuffer)
     */
    public StringBuffer encode(StringBuffer buff) {

        buff.append("(!");
        filter.encode(buff);
        buff.append(')');

        return buff;

    }

    /**
     * Compares key and value before encoding
     * 
     * @see org.springframework.ldap.support.filter.Filter#equals(java.lang.Object)
     */
    public boolean equals(Object o) {

        if (o instanceof NotFilter && o.getClass() == this.getClass()) {
            NotFilter f = (NotFilter) o;
            return this.filter.equals(f.filter);
        }

        return false;
    }

    /**
     * hash attribute and value
     * 
     * @see org.springframework.ldap.support.filter.Filter#hashCode()
     */
    public int hashCode() {
        return HASH ^ filter.hashCode();
    }

}
