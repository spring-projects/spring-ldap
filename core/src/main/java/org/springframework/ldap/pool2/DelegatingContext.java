/*
 * Copyright 2005-2015 the original author or authors.
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
package org.springframework.ldap.pool2;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.pool2.KeyedObjectPool;

import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.util.Assert;

/**
 * Used by {@link PooledContextSource} to wrap a {@link Context}, delegating most methods
 * to the underlying context, retains a reference to the pool the context was checked out
 * from and returns itself to the pool when {@link #close()} is called.
 *
 * @since 2.0
 * @author Eric Dalquist
 */
public class DelegatingContext implements Context {

	private KeyedObjectPool<Object, Object> keyedObjectPool;

	private Context delegateContext;

	private final DirContextType dirContextType;

	/**
	 * Create a new delegating context for the specified pool, context and context type.
	 * @param keyedObjectPool The pool the delegate context was checked out from.
	 * @param delegateContext The context to delegate operations to.
	 * @param dirContextType The type of context, used as a key for the pool.
	 * @throws IllegalArgumentException if any of the arguments are null
	 */
	public DelegatingContext(KeyedObjectPool<Object, Object> keyedObjectPool, Context delegateContext,
			DirContextType dirContextType) {
		Assert.notNull(keyedObjectPool, "keyedObjectPool may not be null");
		Assert.notNull(delegateContext, "delegateContext may not be null");
		Assert.notNull(dirContextType, "dirContextType may not be null");

		this.keyedObjectPool = keyedObjectPool;
		this.delegateContext = delegateContext;
		this.dirContextType = dirContextType;
	}

	// ***** Helper Methods *****//

	/**
	 * @return The direct delegate for this context proxy
	 */
	public Context getDelegateContext() {
		return this.delegateContext;
	}

	/**
	 * Recursivley inspect delegates until a non-delegating context is found.
	 * @return The innermost (real) Context that is being delegated to.
	 */
	public Context getInnermostDelegateContext() {
		final Context delegateContext = this.getDelegateContext();

		if (delegateContext instanceof DelegatingContext) {
			return ((DelegatingContext) delegateContext).getInnermostDelegateContext();
		}

		return delegateContext;
	}

	/**
	 * @throws NamingException If the delegate is null, {@link #close()} has been called.
	 */
	protected void assertOpen() throws NamingException {
		if (this.delegateContext == null) {
			throw new NamingException("Context is closed.");
		}
	}

	// ***** Object methods *****//

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Context)) {
			return false;
		}

		final Context thisContext = this.getInnermostDelegateContext();
		Context otherContext = (Context) obj;
		if (otherContext instanceof DelegatingContext) {
			otherContext = ((DelegatingContext) otherContext).getInnermostDelegateContext();
		}

		return thisContext == otherContext || (thisContext != null && thisContext.equals(otherContext));
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		final Context context = this.getInnermostDelegateContext();
		return (context != null ? context.hashCode() : 0);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		final Context context = this.getInnermostDelegateContext();
		return (context != null ? context.toString() : "Context is closed");
	}

	// ***** Context Interface Delegates *****//

	/**
	 * @see Context#addToEnvironment(String, Object)
	 */
	public Object addToEnvironment(String propName, Object propVal) throws NamingException {
		throw new UnsupportedOperationException("Cannot call addToEnvironment on a pooled context");
	}

	/**
	 * @see Context#bind(Name, Object)
	 */
	public void bind(Name name, Object obj) throws NamingException {
		this.assertOpen();
		this.getDelegateContext().bind(name, obj);
	}

	/**
	 * @see Context#bind(String, Object)
	 */
	public void bind(String name, Object obj) throws NamingException {
		this.assertOpen();
		this.getDelegateContext().bind(name, obj);
	}

	/**
	 * @see Context#close()
	 */
	public void close() throws NamingException {
		final Context context = this.getInnermostDelegateContext();
		if (context == null) {
			return;
		}

		// Get a local reference so the member can be nulled earlier
		this.delegateContext = null;

		// Return the object to the Pool and then null the pool reference
		try {
			boolean valid = true;

			if (context instanceof FailureAwareContext) {
				FailureAwareContext failureAwareContext = (FailureAwareContext) context;
				if (failureAwareContext.hasFailed()) {
					valid = false;
				}
			}

			if (valid) {
				this.keyedObjectPool.returnObject(this.dirContextType, context);
			}
			else {
				this.keyedObjectPool.invalidateObject(this.dirContextType, context);
			}
		}
		catch (Exception e) {
			final NamingException namingException = new NamingException("Failed to return delegate Context to pool.");
			namingException.setRootCause(e);
			throw namingException;
		}
		finally {
			this.keyedObjectPool = null;
		}
	}

	/**
	 * @see Context#composeName(Name, Name)
	 */
	public Name composeName(Name name, Name prefix) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().composeName(name, prefix);
	}

	/**
	 * @see Context#composeName(String, String)
	 */
	public String composeName(String name, String prefix) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().composeName(name, prefix);
	}

	/**
	 * @see Context#createSubcontext(Name)
	 */
	public Context createSubcontext(Name name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
	}

	/**
	 * @see Context#createSubcontext(String)
	 */
	public Context createSubcontext(String name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
	}

	/**
	 * @see Context#destroySubcontext(Name)
	 */
	public void destroySubcontext(Name name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call destroySubcontext on a pooled context");
	}

	/**
	 * @see Context#destroySubcontext(String)
	 */
	public void destroySubcontext(String name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call destroySubcontext on a pooled context");
	}

	/**
	 * @see Context#getEnvironment()
	 */
	public Hashtable<?, ?> getEnvironment() throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().getEnvironment();
	}

	/**
	 * @see Context#getNameInNamespace()
	 */
	public String getNameInNamespace() throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().getNameInNamespace();
	}

	/**
	 * @see Context#getNameParser(Name)
	 */
	public NameParser getNameParser(Name name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().getNameParser(name);
	}

	/**
	 * @see Context#getNameParser(String)
	 */
	public NameParser getNameParser(String name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().getNameParser(name);
	}

	/**
	 * @see Context#list(Name)
	 */
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().list(name);
	}

	/**
	 * @see Context#list(String)
	 */
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().list(name);
	}

	/**
	 * @see Context#listBindings(Name)
	 */
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().listBindings(name);
	}

	/**
	 * @see Context#listBindings(String)
	 */
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().listBindings(name);
	}

	/**
	 * @see Context#lookup(Name)
	 */
	public Object lookup(Name name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().lookup(name);
	}

	/**
	 * @see Context#lookup(String)
	 */
	public Object lookup(String name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().lookup(name);
	}

	/**
	 * @see Context#lookupLink(Name)
	 */
	public Object lookupLink(Name name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().lookupLink(name);
	}

	/**
	 * @see Context#lookupLink(String)
	 */
	public Object lookupLink(String name) throws NamingException {
		this.assertOpen();
		return this.getDelegateContext().lookupLink(name);
	}

	/**
	 * @see Context#rebind(Name, Object)
	 */
	public void rebind(Name name, Object obj) throws NamingException {
		this.assertOpen();
		this.getDelegateContext().rebind(name, obj);
	}

	/**
	 * @see Context#rebind(String, Object)
	 */
	public void rebind(String name, Object obj) throws NamingException {
		this.assertOpen();
		this.getDelegateContext().rebind(name, obj);
	}

	/**
	 * @see Context#removeFromEnvironment(String)
	 */
	public Object removeFromEnvironment(String propName) throws NamingException {
		throw new UnsupportedOperationException("Cannot call removeFromEnvironment on a pooled context");
	}

	/**
	 * @see Context#rename(Name, Name)
	 */
	public void rename(Name oldName, Name newName) throws NamingException {
		this.assertOpen();
		this.getDelegateContext().rename(oldName, newName);
	}

	/**
	 * @see Context#rename(String, String)
	 */
	public void rename(String oldName, String newName) throws NamingException {
		this.assertOpen();
		this.getDelegateContext().rename(oldName, newName);
	}

	/**
	 * @see Context#unbind(Name)
	 */
	public void unbind(Name name) throws NamingException {
		this.assertOpen();
		this.getDelegateContext().unbind(name);
	}

	/**
	 * @see Context#unbind(String)
	 */
	public void unbind(String name) throws NamingException {
		this.assertOpen();
		this.getDelegateContext().unbind(name);
	}

}
