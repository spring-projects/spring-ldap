/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.query.ConditionCriteria;
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
        String base = clazz.getAnnotation(Entry.class).base();
        ConditionCriteria criteria = query().base(base).where(getAttribute(part));

        return appendCondition(part, iterator, criteria);
    }

    private ContainerCriteria appendCondition(Part part, Iterator<Object> iterator, ConditionCriteria criteria) {
        Part.Type type = part.getType();

        String value = null;
        if(iterator.hasNext()){
            value = iterator.next().toString();
        }
        switch (type) {
            case NEGATING_SIMPLE_PROPERTY:
                return criteria.not().is(value);
            case SIMPLE_PROPERTY:
                return criteria.is(value);
            case STARTING_WITH:
                return criteria.like(value + "*");
            case ENDING_WITH:
                return criteria.like("*" + value);
            case CONTAINING:
                return criteria.like("*" + value + "*");
            case LIKE:
                return criteria.like(value);
            case NOT_LIKE:
                return criteria.not().like(value);
            case GREATER_THAN_EQUAL:
                return criteria.gte(value);
            case LESS_THAN_EQUAL:
                return criteria.lte(value);
            case IS_NOT_NULL:
                return criteria.isPresent();
            case IS_NULL:
                return criteria.not().isPresent();
        }

        throw new IllegalArgumentException(String.format("%s queries are not supported for LDAP repositories", type));
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
        ConditionCriteria criteria = base.and(getAttribute(part));

        return appendCondition(part, iterator, criteria);
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
