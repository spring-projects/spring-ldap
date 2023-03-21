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

package org.springframework.ldap.core;

import org.springframework.ldap.support.LdapUtils;

import javax.naming.NameClassPair;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

/**
 * A CollectingNameClassPairCallbackHandler to wrap an {@link AttributesMapper}. That is,
 * the found object is extracted from the {@link Attributes} of each {@link SearchResult},
 * and then passed to the specified {@link AttributesMapper} for translation.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 * @since 1.2
 */
public class AttributesMapperCallbackHandler<T> extends CollectingNameClassPairCallbackHandler<T> {

	private AttributesMapper<T> mapper;

	/**
	 * Constructs a new instance around the specified {@link AttributesMapper}.
	 * @param mapper the target mapper.
	 */
	public AttributesMapperCallbackHandler(AttributesMapper<T> mapper) {
		this.mapper = mapper;
	}

	/**
	 * Cast the NameClassPair to a SearchResult and pass its attributes to the
	 * {@link AttributesMapper}.
	 * @param nameClassPair a <code> SearchResult</code> instance.
	 * @return the Object returned from the mapper.
	 */
	public T getObjectFromNameClassPair(NameClassPair nameClassPair) {
		if (!(nameClassPair instanceof SearchResult)) {
			throw new IllegalArgumentException("Parameter must be an instance of SearchResult");
		}

		SearchResult searchResult = (SearchResult) nameClassPair;
		Attributes attributes = searchResult.getAttributes();
		try {
			return mapper.mapFromAttributes(attributes);
		}
		catch (javax.naming.NamingException e) {
			throw LdapUtils.convertLdapException(e);
		}
	}

}