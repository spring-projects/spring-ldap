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

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Bean to encapsulate a result List and a {@link PagedResultsCookie} to use for
 * returning the results when using {@link PagedResultsRequestControl}.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class PagedResult {

    private List resultList;

    private PagedResultsCookie cookie;

    /**
     * Constructs a PagedResults using the supplied List and
     * {@link PagedResultsCookie}.
     * 
     * @param resultList
     *            the result list.
     * @param cookie
     *            the cookie.
     */
    public PagedResult(List resultList, PagedResultsCookie cookie) {
        this.resultList = resultList;
        this.cookie = cookie;
    }

    /**
     * Get the cookie.
     * 
     * @return the cookie.
     */
    public PagedResultsCookie getCookie() {
        return cookie;
    }

    /**
     * Get the result list.
     * 
     * @return the result list.
     */
    public List getResultList() {
        return resultList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj != null && this.getClass().equals(obj.getClass())) {
            PagedResult that = (PagedResult) obj;
            return new EqualsBuilder().append(this.resultList, that.resultList)
                    .append(this.cookie, that.cookie).isEquals();
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder().append(this.resultList)
                .append(this.cookie).toHashCode();
    }
}
