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

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;

import java.util.List;

/**
 * Base repository implementation for QueryDSL support.
 *
 * @author Mattias Hellborg Arthursson
 * @author Eddu Melendez
 * @since 2.0
 */
public class QueryDslLdapRepository<T> extends SimpleLdapRepository<T> implements QueryDslPredicateExecutor<T> {

    public QueryDslLdapRepository(LdapOperations ldapOperations,
                                  ObjectDirectoryMapper odm,
                                  Class<T> clazz) {
        super(ldapOperations, odm, clazz);
    }

    @Override
    public T findOne(Predicate predicate) {
        return queryFor(predicate).uniqueResult();
    }

    @Override
    public List<T> findAll(Predicate predicate) {
        return queryFor(predicate).list();
    }

    @Override
    public long count(Predicate predicate) {
        return findAll(predicate).size();
    }

    public boolean exists(Predicate predicate) {
    	return count(predicate) > 0;
    }

    public Iterable<T> findAll(Predicate predicate, Sort sort) {
        throw new UnsupportedOperationException();
    }

    private QueryDslLdapQuery<T> queryFor(Predicate predicate) {
        return new QueryDslLdapQuery<T>(getLdapOperations(), getClazz())
                .where(predicate);
    }

    public Iterable<T> findAll(OrderSpecifier<?>... orders) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<T> findAll(Predicate predicate, Pageable pageable) {
        throw new UnsupportedOperationException();
    }
}
