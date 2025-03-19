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

/**
 * Constructs a conditional LDAP filter based on the attribute specified in the previous
 * builder step.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 * @see LdapQueryBuilder#where(String)
 * @see ContainerCriteria#and(String)
 * @see ContainerCriteria#or(String)
 */
public interface ConditionCriteria {

	/**
	 * Appends an {@link org.springframework.ldap.filter.EqualsFilter}.
	 * @param value the value to compare with.
	 * @return an ContainerCriteria instance that can be used to continue append more
	 * criteria or as the LdapQuery instance to be used as instance to e.g.
	 * {@link org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)}.
	 *
	 * @see org.springframework.ldap.filter.EqualsFilter
	 */
	ContainerCriteria is(String value);

	/**
	 * Appends an {@link org.springframework.ldap.filter.GreaterThanOrEqualsFilter}.
	 * @param value the value to compare with.
	 * @return an ContainerCriteria instance that can be used to continue append more
	 * criteria or as the LdapQuery instance to be used as instance to e.g.
	 * {@link org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)}.
	 *
	 * @see org.springframework.ldap.filter.GreaterThanOrEqualsFilter
	 */
	ContainerCriteria gte(String value);

	/**
	 * Appends a {@link org.springframework.ldap.filter.LessThanOrEqualsFilter}.
	 * @param value the value to compare with.
	 * @return an ContainerCriteria instance that can be used to continue append more
	 * criteria or as the LdapQuery instance to be used as instance to e.g.
	 * {@link org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)}.
	 *
	 * @see org.springframework.ldap.filter.LessThanOrEqualsFilter
	 */
	ContainerCriteria lte(String value);

	/**
	 * Appends a {@link org.springframework.ldap.filter.LikeFilter}.
	 * @param value the value to compare with.
	 * @return an ContainerCriteria instance that can be used to continue append more
	 * criteria or as the LdapQuery instance to be used as instance to e.g.
	 * {@link org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)}.
	 *
	 * @see org.springframework.ldap.filter.LikeFilter
	 */
	ContainerCriteria like(String value);

	/**
	 * Appends a {@link org.springframework.ldap.filter.WhitespaceWildcardsFilter}.
	 * @param value the value to compare with.
	 * @return an ContainerCriteria instance that can be used to continue append more
	 * criteria or as the LdapQuery instance to be used as instance to e.g.
	 * {@link org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)}.
	 *
	 * @see org.springframework.ldap.filter.WhitespaceWildcardsFilter
	 */
	ContainerCriteria whitespaceWildcardsLike(String value);

	/**
	 * Appends a {@link org.springframework.ldap.filter.PresentFilter}.
	 * @return an ContainerCriteria instance that can be used to continue append more
	 * criteria or as the LdapQuery instance to be used as instance to e.g.
	 * {@link org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)}.
	 *
	 * @see org.springframework.ldap.filter.PresentFilter
	 */
	ContainerCriteria isPresent();

	/**
	 * Negates the currently constructed operation. In effect this means that the
	 * resulting filter will be wrapped in a
	 * {@link org.springframework.ldap.filter.NotFilter}.
	 * @return an ContainerCriteria instance that can be used to continue append more
	 * criteria or as the LdapQuery instance to be used as instance to e.g.
	 * {@link org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)}.
	 *
	 * @see org.springframework.ldap.filter.NotFilter
	 */
	ConditionCriteria not();

	/**
	 * Appends an {@link org.springframework.ldap.filter.ProximityFilter}.
	 * @param value the value to compare with.
	 * @return an ContainerCriteria instance that can be used to continue append more
	 * criteria or as the LdapQuery instance to be used as instance to e.g.
	 * {@link org.springframework.ldap.core.LdapOperations#search(LdapQuery, org.springframework.ldap.core.ContextMapper)}.
	 * @since 3.3
	 * @see org.springframework.ldap.filter.EqualsFilter
	 */
	default ContainerCriteria near(String value) {
		throw new UnsupportedOperationException();
	}

}
