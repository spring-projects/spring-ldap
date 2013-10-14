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

import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.repository.Query;
import org.springframework.util.Assert;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Handles queries for repository methods annotated with {@link org.springframework.ldap.repository.Query}.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class AnnotatedLdapRepositoryQuery extends AbstractLdapRepositoryQuery {
    private final Query queryAnnotation;

    /**
     * Construct a new instance.
     * @param queryMethod the QueryMethod.
     * @param clazz the managed class.
     * @param ldapOperations the LdapOperations instance to use.
     */
    public AnnotatedLdapRepositoryQuery(LdapQueryMethod queryMethod, Class<?> clazz, LdapOperations ldapOperations) {
        super(queryMethod, clazz, ldapOperations);
        queryAnnotation = queryMethod.getQueryAnnotation();

        Assert.notNull(queryMethod, "Annotation must be present");
        Assert.hasLength(queryAnnotation.value(), "Query filter must be specified");
    }

    @Override
    protected LdapQuery createQuery(Object[] parameters) {
        return query()
                .base(queryAnnotation.base())
                .searchScope(queryAnnotation.searchScope())
                .countLimit(queryAnnotation.countLimit())
                .timeLimit(queryAnnotation.timeLimit())
                .filter(queryAnnotation.value(), parameters);
    }
}
