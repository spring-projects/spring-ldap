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

import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingException;

import org.springframework.util.Assert;

/**
 * A CollectingNameClassPairCallbackHandler to wrap a ContextMapper. That is, the found
 * object is extracted from each {@link Binding}, and then passed to the specified
 * ContextMapper for translation.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 * @since 1.2
 */
public class ContextMapperCallbackHandler<T> extends CollectingNameClassPairCallbackHandler<T> {

	private ContextMapper<T> mapper;

	/**
	 * Constructs a new instance wrapping the supplied {@link ContextMapper}.
	 * @param mapper the mapper to be called for each entry.
	 */
	public ContextMapperCallbackHandler(ContextMapper<T> mapper) {
		Assert.notNull(mapper, "Mapper must not be empty");
		this.mapper = mapper;
	}

	/**
	 * Cast the NameClassPair to a {@link Binding} and pass its object to the
	 * ContextMapper.
	 * @param nameClassPair a Binding instance.
	 * @return the Object returned from the mapper.
	 * @throws NamingException if an error occurs.
	 * @throws ObjectRetrievalException if the object of the nameClassPair is null.
	 */
	public T getObjectFromNameClassPair(NameClassPair nameClassPair) throws NamingException {
		if (!(nameClassPair instanceof Binding)) {
			throw new IllegalArgumentException("Parameter must be an instance of Binding");
		}

		Binding binding = (Binding) nameClassPair;
		Object object = binding.getObject();
		if (object == null) {
			throw new ObjectRetrievalException("Binding did not contain any object.");
		}
		return this.mapper.mapFromContext(object);
	}

}