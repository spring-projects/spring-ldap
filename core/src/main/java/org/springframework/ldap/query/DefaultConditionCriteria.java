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

import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.GreaterThanOrEqualsFilter;
import org.springframework.ldap.filter.LessThanOrEqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.NotFilter;
import org.springframework.ldap.filter.PresentFilter;
import org.springframework.ldap.filter.WhitespaceWildcardsFilter;

/**
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
class DefaultConditionCriteria implements ConditionCriteria {
    private final DefaultContainerCriteria parent;
    private final String attribute;
    private boolean negated = false;

    DefaultConditionCriteria(DefaultContainerCriteria parent, String attribute) {
        this.parent = parent;
        this.attribute = attribute;
    }

    @Override
    public ContainerCriteria is(String value) {
        return appendToParent(new EqualsFilter(attribute, value));
    }

    @Override
    public ContainerCriteria gte(String value) {
        return appendToParent(new GreaterThanOrEqualsFilter(attribute, value));
    }

    @Override
    public ContainerCriteria lte(String value) {
        return appendToParent(new LessThanOrEqualsFilter(attribute, value));
    }

    @Override
    public ContainerCriteria like(String value) {
        return appendToParent(new LikeFilter(attribute, value));
    }

    @Override
    public ContainerCriteria whitespaceWildcardsLike(String value) {
        return appendToParent(new WhitespaceWildcardsFilter(attribute, value));
    }

    @Override
    public ContainerCriteria isPresent() {
        return appendToParent(new PresentFilter(attribute));
    }

    private ContainerCriteria appendToParent(Filter filter) {
        return parent.append(negateIfApplicable(filter));
    }

    private Filter negateIfApplicable(Filter myFilter) {
        if (negated) {
            return new NotFilter(myFilter);
        }

        return myFilter;
    }

    @Override
    public DefaultConditionCriteria not() {
        negated = !negated;
        return this;
    }
}
