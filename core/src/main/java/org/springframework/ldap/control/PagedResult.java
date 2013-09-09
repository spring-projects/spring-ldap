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
package org.springframework.ldap.control;

import java.util.List;

/**
 * Bean to encapsulate a result List and a {@link PagedResultsCookie} to use for
 * returning the results when using {@link PagedResultsRequestControl}.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 * @deprecated
 */
public class PagedResult {

    private List<?> resultList;

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
    public PagedResult(List<?> resultList, PagedResultsCookie cookie) {
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
    public List<?> getResultList() {
        return resultList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PagedResult that = (PagedResult) o;

        if (cookie != null ? !cookie.equals(that.cookie) : that.cookie != null) return false;
        if (resultList != null ? !resultList.equals(that.resultList) : that.resultList != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resultList != null ? resultList.hashCode() : 0;
        result = 31 * result + (cookie != null ? cookie.hashCode() : 0);
        return result;
    }
}
