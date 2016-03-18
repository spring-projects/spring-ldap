/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.ldap.repository.support;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.repository.query.AnnotatedLdapRepositoryQuery;
import org.springframework.ldap.repository.query.LdapQueryMethod;
import org.springframework.ldap.repository.query.PartTreeLdapRepositoryQuery;

import java.io.Serializable;
import java.lang.reflect.Method;

import static org.springframework.data.querydsl.QueryDslUtils.QUERY_DSL_PRESENT;

/**
 * Factory to create {@link org.springframework.ldap.repository.LdapRepository} instances.
 * @author Mattias Hellborg Arthursson
 * @author Eddu Melendez
 * @since 2.0
 */
public class LdapRepositoryFactory extends RepositoryFactorySupport {
    private final LdapOperations ldapOperations;

    public LdapRepositoryFactory(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    @Override
    public <T, ID extends Serializable> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object getTargetRepository(RepositoryMetadata metadata) {
        if(!isQueryDslRepository(metadata.getRepositoryInterface())) {
            return new SimpleLdapRepository(
                    ldapOperations,
                    ldapOperations.getObjectDirectoryMapper(),
                    metadata.getDomainType());
        } else {
            return new QueryDslLdapRepository(
                    ldapOperations,
                    ldapOperations.getObjectDirectoryMapper(),
                    metadata.getDomainType());
        }

    }

    protected Object getTargetRepository(RepositoryInformation metadata) {
        return getTargetRepository((RepositoryMetadata) metadata);
    }

    private static boolean isQueryDslRepository(Class<?> repositoryInterface) {
        return QUERY_DSL_PRESENT && QueryDslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
    }


    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if(!isQueryDslRepository(metadata.getRepositoryInterface())) {
            return SimpleLdapRepository.class;
        } else {
            return QueryDslLdapRepository.class;
        }
    }

    @Override
    protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key) {
        return new LdapQueryLookupStrategy();
    }

    private final class LdapQueryLookupStrategy implements QueryLookupStrategy {
        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
            LdapQueryMethod queryMethod = new LdapQueryMethod(method, metadata, factory);
            Class<?> domainType = metadata.getDomainType();

            if(queryMethod.hasQueryAnnotation()) {
                return new AnnotatedLdapRepositoryQuery(queryMethod, domainType, ldapOperations);
            } else {
                return new PartTreeLdapRepositoryQuery(queryMethod, domainType, ldapOperations);
            }
        }
    }
}
