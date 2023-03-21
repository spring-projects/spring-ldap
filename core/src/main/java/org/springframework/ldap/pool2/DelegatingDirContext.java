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

import org.apache.commons.pool2.KeyedObjectPool;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.pool2.DirContextType;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.util.Assert;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

/**
 * Used by {@link PooledContextSource} to wrap a {@link DirContext}, delegating most
 * methods to the underlying context. This class extends {@link DelegatingContext} which
 * handles returning the context to the pool on a call to {@link #close()}
 *
 * @since 2.0
 * @author Eric Dalquist
 * @author Anindya Chatterjee
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
	 * @see Object#equals(Object)
	 */
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
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		final DirContext context = this.getInnermostDelegateDirContext();
		return (context != null ? context.hashCode() : 0);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		final DirContext context = this.getInnermostDelegateDirContext();
		return (context != null ? context.toString() : "DirContext is closed");
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
	 * @see DirContext#bind(Name, Object, Attributes)
	 */
	public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().bind(name, obj, attrs);
	}

	/**
	 * @see DirContext#bind(String, Object, Attributes)
	 */
	public void bind(String name, Object obj, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().bind(name, obj, attrs);
	}

	/**
	 * @see DirContext#createSubcontext(Name, Attributes)
	 */
	public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
		throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
	}

	/**
	 * @see DirContext#createSubcontext(String, Attributes)
	 */
	public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
		throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
	}

	/**
	 * @see DirContext#getAttributes(Name, String[])
	 */
	public Attributes getAttributes(Name name, String[] attrIds) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().getAttributes(name, attrIds);
	}

	/**
	 * @see DirContext#getAttributes(Name)
	 */
	public Attributes getAttributes(Name name) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().getAttributes(name);
	}

	/**
	 * @see DirContext#getAttributes(String, String[])
	 */
	public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().getAttributes(name, attrIds);
	}

	/**
	 * @see DirContext#getAttributes(String)
	 */
	public Attributes getAttributes(String name) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().getAttributes(name);
	}

	/**
	 * @see DirContext#getSchema(Name)
	 */
	public DirContext getSchema(Name name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call getSchema on a pooled context");
	}

	/**
	 * @see DirContext#getSchema(String)
	 */
	public DirContext getSchema(String name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call getSchema on a pooled context");
	}

	/**
	 * @see DirContext#getSchemaClassDefinition(Name)
	 */
	public DirContext getSchemaClassDefinition(Name name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call getSchemaClassDefinition on a pooled context");
	}

	/**
	 * @see DirContext#getSchemaClassDefinition(String)
	 */
	public DirContext getSchemaClassDefinition(String name) throws NamingException {
		throw new UnsupportedOperationException("Cannot call getSchemaClassDefinition on a pooled context");
	}

	/**
	 * @see DirContext#modifyAttributes(Name, int, Attributes)
	 */
	public void modifyAttributes(Name name, int modOp, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().modifyAttributes(name, modOp, attrs);
	}

	/**
	 * @see DirContext#modifyAttributes(Name, ModificationItem[])
	 */
	public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().modifyAttributes(name, mods);
	}

	/**
	 * @see DirContext#modifyAttributes(String, int, Attributes)
	 */
	public void modifyAttributes(String name, int modOp, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().modifyAttributes(name, modOp, attrs);
	}

	/**
	 * @see DirContext#modifyAttributes(String, ModificationItem[])
	 */
	public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().modifyAttributes(name, mods);
	}

	/**
	 * @see DirContext#rebind(Name, Object, Attributes)
	 */
	public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().rebind(name, obj, attrs);
	}

	/**
	 * @see DirContext#rebind(String, Object, Attributes)
	 */
	public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
		this.assertOpen();
		this.getDelegateDirContext().rebind(name, obj, attrs);
	}

	/**
	 * @see DirContext#search(Name, Attributes, String[])
	 */
	public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn)
			throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, matchingAttributes, attributesToReturn);
	}

	/**
	 * @see DirContext#search(Name, Attributes)
	 */
	public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, matchingAttributes);
	}

	/**
	 * @see DirContext#search(Name, String, Object[], SearchControls)
	 */
	public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs,
			SearchControls cons) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, filterExpr, filterArgs, cons);
	}

	/**
	 * @see DirContext#search(Name, String, SearchControls)
	 */
	public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
			throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, filter, cons);
	}

	/**
	 * @see DirContext#search(String, Attributes, String[])
	 */
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes,
			String[] attributesToReturn) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, matchingAttributes, attributesToReturn);
	}

	/**
	 * @see DirContext#search(String, Attributes)
	 */
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, matchingAttributes);
	}

	/**
	 * @see DirContext#search(String, String, Object[], SearchControls)
	 */
	public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs,
			SearchControls cons) throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, filterExpr, filterArgs, cons);
	}

	/**
	 * @see DirContext#search(String, String, SearchControls)
	 */
	public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
			throws NamingException {
		this.assertOpen();
		return this.getDelegateDirContext().search(name, filter, cons);
	}

	/**
	 * @see DelegatingContext#close()
	 */
	public void close() throws NamingException {
		if (this.delegateDirContext == null) {
			return;
		}

		super.close();
		this.delegateDirContext = null;
	}

}
