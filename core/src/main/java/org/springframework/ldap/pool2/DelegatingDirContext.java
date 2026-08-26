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

package org.springframework.ldap.pool2;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.pool2.KeyedObjectPool;

import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.util.Assert;

/**
 * Used by {@link PooledContextSource} to wrap a {@link DirContext}, delegating most
 * methods to the underlying context. This class extends {@link DelegatingContext} which
 * handles returning the context to the pool on a call to {@link #close()}
 *
 * @author Eric Dalquist
 * @author Anindya Chatterjee
 * @since 2.0
 */
public class DelegatingDirContext extends DelegatingContext implements DirContext, DirContextProxy {

	private DirContext delegateDirContext;

	/**
	 * Create a new delegating dir context for the specified pool, context and context
	 * type.
	 * @param keyedObjectPool The pool the delegate context was checked out from.
	 * @param delegateDirContext The dir context to delegate operations to.
	 * @param dirContextType The type of context, used as a key for the pool.
	 * @throws IllegalArgumentException if any of the arguments are null
	 */
	public DelegatingDirContext(KeyedObjectPool<Object, Object> keyedObjectPool, DirContext delegateDirContext,
			DirContextType dirContextType) {
		super(keyedObjectPool, delegateDirContext, dirContextType);
		Assert.notNull(delegateDirContext, "delegateDirContext may not be null");

		this.delegateDirContext = delegateDirContext;
	}

	// ***** Helper Methods *****//

	/**
	 * Get the direct delegate for this dir context proxy.
	 * @return The direct delegate for this dir context proxy
	 */
	public DirContext getDelegateDirContext() {
		return this.delegateDirContext;
	}

	public Context getDelegateContext() {
		return this.getDelegateDirContext();
	}

	/**
	 * Recursivley inspect delegates until a non-delegating dir context is found.
	 * @return The innermost (real) DirContext that is being delegated to.
	 */
	public DirContext getInnermostDelegateDirContext() {
		final DirContext delegateDirContext = this.getDelegateDirContext();

		if (delegateDirContext instanceof DelegatingDirContext) {
			return ((DelegatingDirContext) delegateDirContext).getInnermostDelegateDirContext();
		}

		return delegateDirContext;
	}

	protected void assertOpen() throws NamingException {
		if (this.delegateDirContext == null) {
			throw new NamingException("DirContext is closed.");
		}

		super.assertOpen();
	}

	// ***** Object methods *****//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DirContext)) {
			return false;
		}

		final DirContext thisDirContext = this.getInnermostDelegateDirContext();
		DirContext otherDirContext = (DirContext) obj;
		if (otherDirContext instanceof DelegatingDirContext) {
			otherDirContext = ((DelegatingDirContext) otherDirContext).getInnermostDelegateDirContext();
		}

		return thisDirContext == otherDirContext || (thisDirContext != null && thisDirContext.equals(otherDirContext));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final DirContext context = this.getInnermostDelegateDirContext();
		return (context != null) ? context.hashCode() : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final DirContext context = this.getInnermostDelegateDirContext();
		return (context != null) ? context.toString() : "DirContext is closed";
	}

	// ***** DirContextProxy Interface Methods *****//

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.core.DirContextProxy#getTargetContext()
	 */
	public DirContext getTargetContext() {
		return this.getInnermostDelegateDirContext();
	}

	// ***** DirContext Interface Delegates *****//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().bind(name, obj, attrs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void bind(String name, Object obj, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().bind(name, obj, attrs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
		throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
		throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attributes getAttributes(Name name, String[] attrIds) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().getAttributes(name, attrIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attributes getAttributes(Name name) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().getAttributes(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().getAttributes(name, attrIds);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attributes getAttributes(String name) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().getAttributes(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirContext getSchema(Name name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call getSchema on a pooled context");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirContext getSchema(String name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call getSchema on a pooled context");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirContext getSchemaClassDefinition(Name name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call getSchemaClassDefinition on a pooled context");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DirContext getSchemaClassDefinition(String name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call getSchemaClassDefinition on a pooled context");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyAttributes(Name name, int modOp, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().modifyAttributes(name, modOp, attrs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().modifyAttributes(name, mods);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyAttributes(String name, int modOp, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().modifyAttributes(name, modOp, attrs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().modifyAttributes(name, mods);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().rebind(name, obj, attrs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().rebind(name, obj, attrs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn)
			throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, matchingAttributes, attributesToReturn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, matchingAttributes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs,
			SearchControls cons) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, filterExpr, filterArgs, cons);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
			throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, filter, cons);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes,
			String[] attributesToReturn) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, matchingAttributes, attributesToReturn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, matchingAttributes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs,
			SearchControls cons) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, filterExpr, filterArgs, cons);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
			throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, filter, cons);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws NamingException {
		if (this.delegateDirContext == null) {
			return;
		}

		super.close();
		this.delegateDirContext = null;
	}

}
