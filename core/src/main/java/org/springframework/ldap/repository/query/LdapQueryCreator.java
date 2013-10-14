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

import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQuery;

import java.util.Iterator;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Creator of dynamic queries based on method names.
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class LdapQueryCreator extends AbstractQueryCreator<LdapQuery, ContainerCriteria> {
    private final Class<?> clazz;
    private final ObjectDirectoryMapper mapper;

    /**
     * Construct a new instance.
     */
    public LdapQueryCreator(PartTree tree,
                            Parameters<?, ?> parameters,
                            Class<?> clazz,
                            ObjectDirectoryMapper mapper,
                            Object[] values) {
        super(tree, new ParametersParameterAccessor(parameters, values));
        this.clazz = clazz;
        this.mapper = mapper;
    }

    @Override
    protected ContainerCriteria create(Part part, Iterator<Object> iterator) {
        return query()
                .where(getAttribute(part))
                .is(iterator.next().toString());
    }

    private String getAttribute(Part part) {
        PropertyPath path = part.getProperty();
        if(path.hasNext()) {
            throw new IllegalArgumentException("Nested properties are not supported");
        }

        return mapper.attributeFor(clazz, path.getSegment());
    }

    @Override
    protected ContainerCriteria and(Part part, ContainerCriteria base, Iterator<Object> iterator) {
        return base.and(getAttribute(part)).is(iterator.next().toString());
    }

    @Override
    protected ContainerCriteria or(ContainerCriteria base, ContainerCriteria criteria) {
        return base.or(criteria);
    }

    @Override
    protected LdapQuery complete(ContainerCriteria criteria, Sort sort) {
        return criteria;
    }
}
