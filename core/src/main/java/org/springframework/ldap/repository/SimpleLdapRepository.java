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

package org.springframework.ldap.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Persistable;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.util.Assert;

import javax.naming.Name;
import java.util.Iterator;
import java.util.LinkedList;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Base repository implementation for LDAP.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class SimpleLdapRepository<T> implements LdapRepository<T> {
    private static final String OBJECTCLASS_ATTRIBUTE = "objectclass";
    private final LdapOperations ldapOperations;
    private final ObjectDirectoryMapper odm;
    private final Class<T> clazz;

    public SimpleLdapRepository(LdapOperations ldapOperations, ObjectDirectoryMapper odm, Class<T> clazz) {
        this.ldapOperations = ldapOperations;
        this.odm = odm;
        this.clazz = clazz;
    }

    @Override
    public long count() {
        Filter filter = odm.filterFor(clazz, null);
        CountNameClassPairCallbackHandler callback = new CountNameClassPairCallbackHandler();
        LdapQuery query = query().attributes(OBJECTCLASS_ATTRIBUTE).filter(filter);
        ldapOperations.search(query, callback);

        return callback.getNoOfRows();
    }

    private <S extends T> boolean isNew(S entity, Name id) {
        if (entity instanceof Persistable) {
            Persistable persistable = (Persistable) entity;
            return persistable.isNew();
        } else {
            return id == null;
        }
    }

    @Override
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "Entity must not be null");
        Name declaredId = odm.getId(entity);
        Name calculatedId = odm.getCalculatedId(entity);

        if (isNew(entity, declaredId)) {
            if (declaredId == null) {
                if (calculatedId != null) {
                    odm.setId(entity, calculatedId);
                } else {
                    throw new IllegalStateException(String.format("Unable to calculate id of entry of class %s - " +
                            "ID not set and unable to calculate new ID. Missing @DnAttribute annotations with index?",
                            entity.getClass()));
                }
            }

            ldapOperations.create(entity);
        } else {
            ldapOperations.update(entity);
            if (declaredId != calculatedId) {
                odm.setId(entity, calculatedId);
            }
        }

        return entity;
    }

    @Override
    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        return new TransformingIterable<S, S>(entities, new Function<S, S>() {
            @Override
            public S transform(S entry) {
                return save(entry);
            }
        });
    }

    @Override
    public T findOne(Name name) {
        Assert.notNull(name, "Id must not be null");
        try {
            return ldapOperations.findByDn(name, clazz);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public Iterable<T> findAll(LdapQuery ldapQuery) {
        Assert.notNull(ldapQuery, "LdapQuery must not be null");
        return ldapOperations.find(ldapQuery, clazz);
    }

    @Override
    public T findOne(LdapQuery ldapQuery) {
        Assert.notNull(ldapQuery, "LdapQuery must not be null");
        try {
            return ldapOperations.findOne(ldapQuery, clazz);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public boolean exists(Name name) {
        Assert.notNull(name, "Id must not be null");
        return findOne(name) != null;
    }

    @Override
    public Iterable<T> findAll() {
        return ldapOperations.findAll(clazz);
    }

    @Override
    public Iterable<T> findAll(final Iterable<Name> names) {
        Iterable<T> found = new TransformingIterable<Name, T>(names, new Function<Name, T>() {
            @Override
            public T transform(Name name) {
                return findOne(name);
            }
        });

        LinkedList<T> list = new LinkedList<T>();
        for (T entry : found) {
            if (entry != null) {
                list.add(entry);
            }
        }

        return list;
    }

    @Override
    public void delete(Name name) {
        Assert.notNull(name, "Id must not be null");
        ldapOperations.unbind(name);
    }

    @Override
    public void delete(T entity) {
        Assert.notNull(entity, "Entity must not be null");
        ldapOperations.delete(entity);
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        delete(findAll());
    }

    private final static class TransformingIterable<F, T> implements Iterable<T> {
        private final Iterable<F> target;
        private final Function<F, T> function;

        private TransformingIterable(Iterable<F> target, Function<F, T> function) {
            this.target = target;
            this.function = function;
        }

        @Override
        public Iterator<T> iterator() {
            final Iterator<F> targetIterator = target.iterator();
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return targetIterator.hasNext();
                }

                @Override
                public T next() {
                    return function.transform(targetIterator.next());
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Remove is not supported for this iterator");
                }
            };
        }
    }

    private interface Function<F, T> {
        T transform(F entry);
    }
}
