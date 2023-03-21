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
import org.springframework.ldap.pool2.DirContextType;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.util.Assert;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;

/**
 * Used by {@link PooledContextSource} to wrap a {@link LdapContext}, delegating most
 * methods to the underlying context. This class extends {@link DelegatingDirContext}
 * which handles returning the context to the pool on a call to {@link #close()}
 *
 * @since 2.0
 * @author Eric Dalquist
 * @author Anindya Chatterjee
 */
public class DelegatingLdapContext extends DelegatingDirContext implements LdapContext {

	private LdapContext delegateLdapContext;

	/**
	 * Create a new delegating ldap context for the specified pool, context and context
	 * type.
	 * @param keyedObjectPool The pool the delegate context was checked out from.
	 * @param delegateLdapContext The ldap context to delegate operations to.
	 * @param dirContextType The type of context, used as a key for the pool.
	 * @throws IllegalArgumentException if any of the arguments are null
	 */
	public DelegatingLdapContext(KeyedObjectPool<Object, Object> keyedObjectPool, LdapContext delegateLdapContext,
			DirContextType dirContextType) {
		super(keyedObjectPool, delegateLdapContext, dirContextType);
		Assert.notNull(delegateLdapContext, "delegateLdapContext may not be null");

		this.delegateLdapContext = delegateLdapContext;
	}

	// ***** Helper Methods *****//

	/**
	 * @return The direct delegate for this ldap context proxy
	 */
	public LdapContext getDelegateLdapContext() {
		return this.delegateLdapContext;
	}

	// cannot return subtype in overridden method unless Java5
	public DirContext getDelegateDirContext() {
		return this.getDelegateLdapContext();
	}

	/**
	 * Recursivley inspect delegates until a non-delegating ldap context is found.
	 * @return The innermost (real) DirContext that is being delegated to.
	 */
	public LdapContext getInnermostDelegateLdapContext() {
		final LdapContext delegateLdapContext = this.getDelegateLdapContext();

		if (delegateLdapContext instanceof DelegatingLdapContext) {
			return ((DelegatingLdapContext) delegateLdapContext).getInnermostDelegateLdapContext();
		}

		return delegateLdapContext;
	}

	protected void assertOpen() throws NamingException {
		if (this.delegateLdapContext == null) {
			throw new NamingException("LdapContext is closed.");
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
		if (!(obj instanceof LdapContext)) {
			return false;
		}

		final LdapContext thisLdapContext = this.getInnermostDelegateLdapContext();
		LdapContext otherLdapContext = (LdapContext) obj;
		if (otherLdapContext instanceof DelegatingLdapContext) {
			otherLdapContext = ((DelegatingLdapContext) otherLdapContext).getInnermostDelegateLdapContext();
		}

		return thisLdapContext == otherLdapContext
				|| (thisLdapContext != null && thisLdapContext.equals(otherLdapContext));
	}

	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		final LdapContext context = this.getInnermostDelegateLdapContext();
		return (context != null ? context.hashCode() : 0);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		final LdapContext context = this.getInnermostDelegateLdapContext();
		return (context != null ? context.toString() : "LdapContext is closed");
	}

	// ***** LdapContext Interface Delegates *****//

	/**
	 * @see LdapContext#extendedOperation(ExtendedRequest)
	 */
	public ExtendedResponse extendedOperation(ExtendedRequest request) throws NamingException {
		this.assertOpen();
		return this.getDelegateLdapContext().extendedOperation(request);
	}

	/**
	 * @see LdapContext#getConnectControls()
	 */
	public Control[] getConnectControls() throws NamingException {
		this.assertOpen();
		return this.getDelegateLdapContext().getConnectControls();
	}

	/**
	 * @see LdapContext#getRequestControls()
	 */
	public Control[] getRequestControls() throws NamingException {
		this.assertOpen();
		return this.getDelegateLdapContext().getRequestControls();
	}

	/**
	 * @see LdapContext#getResponseControls()
	 */
	public Control[] getResponseControls() throws NamingException {
		this.assertOpen();
		return this.getDelegateLdapContext().getResponseControls();
	}

	/**
	 * @see LdapContext#newInstance(Control[])
	 */
	public LdapContext newInstance(Control[] requestControls) throws NamingException {
		throw new UnsupportedOperationException("Cannot call newInstance on a pooled context");
	}

	/**
	 * @see LdapContext#reconnect(Control[])
	 */
	public void reconnect(Control[] connCtls) throws NamingException {
		throw new UnsupportedOperationException("Cannot call reconnect on a pooled context");
	}

	/**
	 * @see LdapContext#setRequestControls(Control[])
	 */
	public void setRequestControls(Control[] requestControls) throws NamingException {
		throw new UnsupportedOperationException("Cannot call setRequestControls on a pooled context");
	}

	/**
	 * @see DelegatingDirContext#close()
	 */
	public void close() throws NamingException {
		if (this.delegateLdapContext == null) {
			return;
		}

		super.close();
		this.delegateLdapContext = null;
	}

}
