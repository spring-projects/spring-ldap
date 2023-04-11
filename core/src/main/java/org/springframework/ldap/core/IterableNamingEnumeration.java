/*
 * Copyright 2005-2023 the original author or authors.
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

import java.util.Iterator;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * @author Mattias Hellborg Arthursson
 */
final class IterableNamingEnumeration<T> implements NamingEnumeration<T> {

	private final Iterator<T> iterator;

	IterableNamingEnumeration(Iterable<T> iterable) {
		this.iterator = iterable.iterator();
	}

	@Override
	public T next() {
		return this.iterator.next();
	}

	@Override
	public boolean hasMore() {
		return this.iterator.hasNext();
	}

	@Override
	public void close() throws NamingException {
	}

	@Override
	public boolean hasMoreElements() {
		return hasMore();
	}

	@Override
	public T nextElement() {
		return next();
	}

}
