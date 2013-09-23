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

import javax.naming.directory.SearchControls;

/**
 * Type safe definitions of search scopes.
 *
 * @author Mattias Hellborg Arthursson
 */
public enum SearchScope {
    /**
     * Corresponds to {@link SearchControls#OBJECT_SCOPE}
     */
    OBJECT(SearchControls.OBJECT_SCOPE),
    /**
     * Corresponds to {@link SearchControls#ONELEVEL_SCOPE}
     */
    ONELEVEL(SearchControls.ONELEVEL_SCOPE),
    /**
     * Corresponds to {@link SearchControls#SUBTREE_SCOPE}
     */
    SUBTREE(SearchControls.SUBTREE_SCOPE);

    private final int id;

    private SearchScope(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
