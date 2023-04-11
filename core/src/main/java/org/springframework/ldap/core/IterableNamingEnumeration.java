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
