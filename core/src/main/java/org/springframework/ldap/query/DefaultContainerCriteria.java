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

package org.springframework.ldap.query;

import org.springframework.ldap.filter.Filter;

import javax.naming.Name;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.ldap.query.CriteriaContainerType.AND;
import static org.springframework.ldap.query.CriteriaContainerType.OR;

/**
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
class DefaultContainerCriteria implements AppendableContainerCriteria {

	private final Set<Filter> filters = new LinkedHashSet<Filter>();

	private final LdapQuery topQuery;

	private CriteriaContainerType type;

	DefaultContainerCriteria(LdapQuery topQuery) {
		this.topQuery = topQuery;
	}

	@Override
	public DefaultContainerCriteria append(Filter filter) {
		this.filters.add(filter);
		return this;
	}

	DefaultContainerCriteria withType(CriteriaContainerType newType) {
		this.type = newType;
		return this;
	}

	@Override
	public ConditionCriteria and(String attribute) {
		AND.validateSameType(this.type);
		this.type = AND;

		return new DefaultConditionCriteria(this, attribute);
	}

	@Override
	public ConditionCriteria or(String attribute) {
		OR.validateSameType(this.type);
		this.type = OR;

		return new DefaultConditionCriteria(this, attribute);
	}

	@Override
	public ContainerCriteria and(ContainerCriteria nested) {
		if (this.type == OR) {
			return new DefaultContainerCriteria(this.topQuery).withType(AND).append(this.filter())
					.append(nested.filter());
		}
		else {
			this.type = AND;
			this.filters.add(nested.filter());
			return this;
		}
	}

	@Override
	public ContainerCriteria or(ContainerCriteria nested) {
		if (this.type == AND) {
			return new DefaultContainerCriteria(this.topQuery).withType(OR).append(this.filter())
					.append(nested.filter());
		}
		else {
			this.type = OR;
			this.filters.add(nested.filter());
			return this;
		}
	}

	@Override
	public Filter filter() {
		if (this.filters.size() == 1) {
			// No need to wrap in And/OrFilter if there's just one condition.
			return this.filters.iterator().next();
		}

		return this.type.constructFilter().appendAll(this.filters);
	}

	@Override
	public Name base() {
		return this.topQuery.base();
	}

	@Override
	public SearchScope searchScope() {
		return this.topQuery.searchScope();
	}

	@Override
	public Integer timeLimit() {
		return this.topQuery.timeLimit();
	}

	@Override
	public Integer countLimit() {
		return this.topQuery.countLimit();
	}

	@Override
	public String[] attributes() {
		return this.topQuery.attributes();
	}

}
