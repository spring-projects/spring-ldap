/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.core;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.jspecify.annotations.Nullable;

/**
 * Utility that helps with reading all attribute values from Active Directory using
 * <em>Incremental Retrieval of Multi-valued Properties</em>.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.3.2
 * @see <a href="https://tools.ietf.org/html/draft-kashi-incremental-00">Incremental
 * Retrieval of Multi-valued Properties</a>
 * @see org.springframework.ldap.core.support.DefaultIncrementalAttributesMapper
 */
public interface IncrementalAttributesMapper<T extends IncrementalAttributesMapper> extends AttributesMapper<T> {

	/**
	 * Get all of the collected values for the specified attribute.
	 * @param attributeName the attribute to get values for.
	 * @return the collected values for the specified attribute. Will be <code>null</code>
	 * if the requested attribute has not been returned by the server (attribute did not
	 * exist).
	 */
	@Nullable List<Object> getValues(String attributeName);

	/**
	 * Get all collected values for all managed attributes as an Attributes instance.
	 * @return an Attributes instance populated with all collected values.
	 */
	Attributes getCollectedAttributes();

	/**
	 * Check whether another query iteration is required to get all values for all
	 * attributes.
	 * @return <code>true</code> if there are more values for at least one of the managed
	 * attributes, <code>false</code> otherwise.
	 */
	boolean hasMore();

	/**
	 * Get properly formatted attributes for use in the next query. The attribute names
	 * included will include Range specifiers as needed and only the attributes that have
	 * not been retrieved in full will be included.
	 * @return an array of Strings to be used as input to e.g.
	 * {@link org.springframework.ldap.core.LdapTemplate#lookup(javax.naming.Name, String[], org.springframework.ldap.core.AttributesMapper)}
	 * in the next iteration.
	 */
	String[] getAttributesForLookup();

	/**
	 * Goes through all of the attributes to record their values and figure out whether a
	 * new query iteration is needed to get more values.
	 * @param attributes attributes from a SearchResult.
	 * @return this instance.
	 * @throws javax.naming.NamingException
	 */
	T mapFromAttributes(Attributes attributes) throws NamingException;

}
