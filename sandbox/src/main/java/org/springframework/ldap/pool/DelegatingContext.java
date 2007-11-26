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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.lang.Validate;
import org.apache.commons.pool.KeyedObjectPool;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 */
public class DelegatingContext implements Context {
    private KeyedObjectPool keyedObjectPool;
    private Context delegateContext;
    private final DirContextType dirContextType;
    

    public DelegatingContext(KeyedObjectPool keyedObjectPool, Context delegateContext, DirContextType dirContextType) {
        Validate.notNull(keyedObjectPool, "keyedObjectPool may not be null");
        Validate.notNull(delegateContext, "delegateContext may not be null");
        Validate.notNull(dirContextType, "dirContextType may not be null");
        
        this.keyedObjectPool = keyedObjectPool;
        this.delegateContext = delegateContext;
        this.dirContextType = dirContextType;
    }
    
    
    //***** Helper Methods *****//
    
    public Context getDelegateContext() {
        return this.delegateContext;
    }
    
    public Context getInnermostDelegateContext() {
        final Context delegateContext = this.getDelegateContext();
        
        if (delegateContext instanceof DelegatingContext) {
            return ((DelegatingContext)delegateContext).getInnermostDelegateContext();
        }
        else {
            return delegateContext;
        }
    }
    
    protected void assertOpen() throws NamingException {
        if (this.delegateContext == null) {
            throw new NamingException("Context is closed.");
        }
    }

    
    //***** Object methods *****//

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Context)) {
            return false;
        }
        
        final Context thisContext = this.getInnermostDelegateContext();
        Context otherContext = (Context)obj;
        if (otherContext instanceof DelegatingContext) {
            otherContext = ((DelegatingContext)otherContext).getInnermostDelegateContext();
        }
        
        return thisContext == otherContext || (thisContext != null && thisContext.equals(otherContext));
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final Context context = this.getInnermostDelegateContext();
        return (context != null ? context.hashCode() : 0);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final Context context = this.getInnermostDelegateContext();
        return (context != null ? context.toString() : "Context is closed");
    }
    
    
    //***** Context Interface Delegates *****//

    /**
     * @see javax.naming.Context#addToEnvironment(java.lang.String, java.lang.Object)
     */
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        throw new UnsupportedOperationException("Cannot call addToEnvironment on a pooled context");
    }

    /**
     * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
     */
    public void bind(Name name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("Cannot call bind on a pooled context");        
    }

    /**
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    public void bind(String name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("Cannot call bind on a pooled context");        
    }

    /**
     * @see javax.naming.Context#close()
     */
    public void close() throws NamingException {
        final Context context = this.getInnermostDelegateContext();
        if (context == null) {
            return;
        }
        
        //Get a local reference so the member can be nulled earlier
        this.delegateContext = null;

        //Return the object to the Pool and then null the pool reference
        try {
            this.keyedObjectPool.returnObject(this.dirContextType, context);
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
     * @see javax.naming.Context#composeName(javax.naming.Name, javax.naming.Name)
     */
    public Name composeName(Name name, Name prefix) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().composeName(name, prefix);
    }

    /**
     * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
     */
    public String composeName(String name, String prefix) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().composeName(name, prefix);
    }

    /**
     * @see javax.naming.Context#createSubcontext(javax.naming.Name)
     */
    public Context createSubcontext(Name name) throws NamingException {
        throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
    }

    /**
     * @see javax.naming.Context#createSubcontext(java.lang.String)
     */
    public Context createSubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
    }

    /**
     * @see javax.naming.Context#destroySubcontext(javax.naming.Name)
     */
    public void destroySubcontext(Name name) throws NamingException {
        throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
    }

    /**
     * @see javax.naming.Context#destroySubcontext(java.lang.String)
     */
    public void destroySubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException("Cannot call createSubcontext on a pooled context");
    }

    /**
     * @see javax.naming.Context#getEnvironment()
     */
    public Hashtable getEnvironment() throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().getEnvironment();
    }

    /**
     * @see javax.naming.Context#getNameInNamespace()
     */
    public String getNameInNamespace() throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().getNameInNamespace();
    }

    /**
     * @see javax.naming.Context#getNameParser(javax.naming.Name)
     */
    public NameParser getNameParser(Name name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().getNameParser(name);
    }

    /**
     * @see javax.naming.Context#getNameParser(java.lang.String)
     */
    public NameParser getNameParser(String name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().getNameParser(name);
    }

    /**
     * @see javax.naming.Context#list(javax.naming.Name)
     */
    public NamingEnumeration list(Name name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().list(name);
    }

    /**
     * @see javax.naming.Context#list(java.lang.String)
     */
    public NamingEnumeration list(String name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().list(name);
    }

    /**
     * @see javax.naming.Context#listBindings(javax.naming.Name)
     */
    public NamingEnumeration listBindings(Name name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().listBindings(name);
    }

    /**
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    public NamingEnumeration listBindings(String name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().listBindings(name);
    }

    /**
     * @see javax.naming.Context#lookup(javax.naming.Name)
     */
    public Object lookup(Name name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().lookup(name);
    }

    /**
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(String name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().lookup(name);
    }

    /**
     * @see javax.naming.Context#lookupLink(javax.naming.Name)
     */
    public Object lookupLink(Name name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().lookupLink(name);
    }

    /**
     * @see javax.naming.Context#lookupLink(java.lang.String)
     */
    public Object lookupLink(String name) throws NamingException {
        this.assertOpen();
        return this.getDelegateContext().lookupLink(name);
    }

    /**
     * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
     */
    public void rebind(Name name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("Cannot call rebind on a pooled context");
    }

    /**
     * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
     */
    public void rebind(String name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("Cannot call rebind on a pooled context");
    }

    /**
     * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
     */
    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new UnsupportedOperationException("Cannot call removeFromEnvironment on a pooled context");
    }

    /**
     * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
     */
    public void rename(Name oldName, Name newName) throws NamingException {
        this.assertOpen();
        this.getDelegateContext().rename(oldName, newName);
    }

    /**
     * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
     */
    public void rename(String oldName, String newName) throws NamingException {
        this.assertOpen();
        this.getDelegateContext().rename(oldName, newName);
    }

    /**
     * @see javax.naming.Context#unbind(javax.naming.Name)
     */
    public void unbind(Name name) throws NamingException {
        throw new UnsupportedOperationException("Cannot call unbind on a pooled context");
    }

    /**
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    public void unbind(String name) throws NamingException {
        throw new UnsupportedOperationException("Cannot call unbind on a pooled context");
    }
}
