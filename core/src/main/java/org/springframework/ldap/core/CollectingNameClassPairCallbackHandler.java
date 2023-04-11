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

import java.util.LinkedList;
import java.util.List;

import javax.naming.NameClassPair;
import javax.naming.NamingException;

/**
 * A NameClassPairCallbackHandler to collect all results in an internal List.
 *
 * @see LdapTemplate
 * @author Mattias Hellborg Arthursson
 */
public abstract class CollectingNameClassPairCallbackHandler<T> implements NameClassPairCallbackHandler {

	private List<T> list = new LinkedList<T>();

	/**
	 * Get the assembled list.
	 * @return the list of all assembled objects.
	 */
	public List<T> getList() {
		return this.list;
	}

	/**
	 * Pass on the supplied NameClassPair to
	 * {@link #getObjectFromNameClassPair(NameClassPair)} and add the result to the
	 * internal list.
	 */
	public final void handleNameClassPair(NameClassPair nameClassPair) throws NamingException {
		this.list.add(getObjectFromNameClassPair(nameClassPair));
	}

	/**
	 * Handle a NameClassPair and transform it to an Object of the desired type and with
	 * data from the NameClassPair.
	 * @param nameClassPair a NameClassPair from a search operation.
	 * @return an object constructed from the data in the NameClassPair.
	 * @throws NamingException if an error occurs.
	 */
	public abstract T getObjectFromNameClassPair(NameClassPair nameClassPair) throws NamingException;

}
