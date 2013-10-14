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

package org.springframework.ldap.repository.query;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.query.LdapQuery;

/**
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
abstract class AbstractLdapRepositoryQuery implements RepositoryQuery {
    private final LdapQueryMethod queryMethod;
    private final Class<?> clazz;
    private final LdapOperations ldapOperations;

    public AbstractLdapRepositoryQuery(LdapQueryMethod queryMethod,
                                       Class<?> clazz,
                                       LdapOperations ldapOperations) {
        this.queryMethod = queryMethod;
        this.clazz = clazz;
        this.ldapOperations = ldapOperations;
    }

    @Override
    public final Object execute(Object[] parameters) {
        LdapQuery query = createQuery(parameters);

        if(queryMethod.isCollectionQuery()) {
            return ldapOperations.find(query, clazz);
        } else {
            try {
                return ldapOperations.findOne(query, clazz);
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
    }

    protected abstract LdapQuery createQuery(Object[] parameters);

    Class<?> getClazz() {
        return clazz;
    }

    @Override
    public final QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
