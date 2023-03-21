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
 * And/or filter builder support for LdapQuery.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public interface ContainerCriteria extends LdapQuery {

	/**
	 * Append a logical And condition to the currently built filter.
	 * @param attribute Name of the attribute to specify a condition for.
	 * @return A ConditionCriteria instance for specifying the compare operation.
	 * @throws IllegalStateException if {@link #or(String)} has previously been called on
	 * this instance.
	 */
	ConditionCriteria and(String attribute);

	/**
	 * Append a logical Or condition to the currently built filter.
	 * @param attribute Name of the attribute to specify a condition for.
	 * @return A ConditionCriteria instance for specifying the compare operation.
	 * @throws IllegalStateException if {@link #and(String)} has previously been called on
	 * this instance.
	 */
	ConditionCriteria or(String attribute);

	/**
	 * Append an And condition for a nested criterion. Use
	 * {@link org.springframework.ldap.query.LdapQueryBuilder#query()} to start the nested
	 * condition. Any base query information on the nested builder instance will not be
	 * considered.
	 * @param nested the nested criterion.
	 * @return A ConditionCriteria instance for specifying the compare operation.
	 */
	ContainerCriteria and(ContainerCriteria nested);

	/**
	 * Append an Or condition for a nested criterion. Use
	 * {@link org.springframework.ldap.query.LdapQueryBuilder#query()} to start the nested
	 * condition. Any base query information on the nested builder instance will not be
	 * considered.
	 * @param nested the nested criterion.
	 * @return A ConditionCriteria instance for specifying the compare operation.
	 */
	ContainerCriteria or(ContainerCriteria nested);

}
