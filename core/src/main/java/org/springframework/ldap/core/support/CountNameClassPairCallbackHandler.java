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
package org.springframework.ldap.core.support;

import javax.naming.NameClassPair;

import org.springframework.ldap.core.NameClassPairCallbackHandler;

/**
 * A {@link NameClassPairCallbackHandler} for counting all returned entries.
 * 
 * @author Mattias Arthursson
 * 
 */
public class CountNameClassPairCallbackHandler implements
        NameClassPairCallbackHandler {

    private int noOfRows = 0;

    /**
     * Get the number of rows that was returned by the search.
     * 
     * @return the number of entries that have been handled.
     */
    public int getNoOfRows() {
        return noOfRows;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.SearchResultCallbackHandler#handleSearchResult(javax.naming.directory.SearchResult)
     */
    public void handleNameClassPair(NameClassPair nameClassPair) {
        noOfRows++;
    }

}
