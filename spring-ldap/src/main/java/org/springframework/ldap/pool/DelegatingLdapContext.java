/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.pool;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.Validate;
import org.apache.commons.pool.KeyedObjectPool;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DelegatingLdapContext extends DelegatingDirContext implements LdapContext {
    private LdapContext delegateLdapContext;

    public DelegatingLdapContext(KeyedObjectPool keyedObjectPool, LdapContext delegateLdapContext, DirContextType dirContextType) {
        super(keyedObjectPool, delegateLdapContext, dirContextType);
        Validate.notNull(delegateLdapContext, "delegateLdapContext may not be null");

        this.delegateLdapContext = delegateLdapContext;
    }
    
    
    //***** Helper Methods *****//
    
    public LdapContext getDelegateLdapContext() {
        return this.delegateLdapContext;
    }
    
    // cannot return subtype in overriden method unless Java5
    public DirContext getDelegateDirContext() {
        return this.getDelegateLdapContext();
    }

    public LdapContext getInnermostDelegateLdapContext() {
        final LdapContext delegateLdapContext = this.getDelegateLdapContext();

        if (delegateLdapContext instanceof DelegatingLdapContext) {
            return ((DelegatingLdapContext)delegateLdapContext).getInnermostDelegateLdapContext();
        }
        else {
            return delegateLdapContext;
        }
    }

    protected void assertOpen() throws NamingException {
        if (this.delegateLdapContext == null) {
            throw new NamingException("LdapContext is closed.");
        }

        super.assertOpen();
    }

    
    //***** Object methods *****//

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LdapContext)) {
            return false;
        }
        
        final LdapContext thisLdapContext = this.getInnermostDelegateLdapContext();
        LdapContext otherLdapContext = (LdapContext)obj;
        if (otherLdapContext instanceof DelegatingLdapContext) {
            otherLdapContext = ((DelegatingLdapContext)otherLdapContext).getInnermostDelegateLdapContext();
        }
        
        return thisLdapContext == otherLdapContext || (thisLdapContext != null && thisLdapContext.equals(otherLdapContext));
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final LdapContext context = this.getInnermostDelegateLdapContext();
        return (context != null ? context.hashCode() : 0);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final LdapContext context = this.getInnermostDelegateLdapContext();
        return (context != null ? context.toString() : "LdapContext is closed");
    }
    
    
    //***** LdapContext Interface Delegates *****//

    /**
     * @see javax.naming.ldap.LdapContext#extendedOperation(javax.naming.ldap.ExtendedRequest)
     */
    public ExtendedResponse extendedOperation(ExtendedRequest request) throws NamingException {
        this.assertOpen();
        return this.getDelegateLdapContext().extendedOperation(request);
    }

    /**
     * @see javax.naming.ldap.LdapContext#getConnectControls()
     */
    public Control[] getConnectControls() throws NamingException {
        this.assertOpen();
        return this.getDelegateLdapContext().getConnectControls();
    }

    /**
     * @see javax.naming.ldap.LdapContext#getRequestControls()
     */
    public Control[] getRequestControls() throws NamingException {
        this.assertOpen();
        return this.getDelegateLdapContext().getRequestControls();
    }

    /**
     * @see javax.naming.ldap.LdapContext#getResponseControls()
     */
    public Control[] getResponseControls() throws NamingException {
        this.assertOpen();
        return this.getDelegateLdapContext().getResponseControls();
    }

    /**
     * @see javax.naming.ldap.LdapContext#newInstance(javax.naming.ldap.Control[])
     */
    public LdapContext newInstance(Control[] requestControls) throws NamingException {
        throw new UnsupportedOperationException("Cannot call newInstance on a pooled context");
    }

    /**
     * @see javax.naming.ldap.LdapContext#reconnect(javax.naming.ldap.Control[])
     */
    public void reconnect(Control[] connCtls) throws NamingException {
        throw new UnsupportedOperationException("Cannot call reconnect on a pooled context");
    }

    /**
     * @see javax.naming.ldap.LdapContext#setRequestControls(javax.naming.ldap.Control[])
     */
    public void setRequestControls(Control[] requestControls) throws NamingException {
        throw new UnsupportedOperationException("Cannot call setRequestControls on a pooled context");
    }

    /**
     * @see edu.wisc.commons.lcp.pool.DelegatingContext#close()
     */
    public void close() throws NamingException {
        if (this.delegateLdapContext == null) {
            return;
        }

        super.close();
        this.delegateLdapContext = null;
    }
}
