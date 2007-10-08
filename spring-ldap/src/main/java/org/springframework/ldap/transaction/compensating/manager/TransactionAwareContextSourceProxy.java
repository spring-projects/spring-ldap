/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ldap.transaction.compensating.manager;

import java.lang.reflect.Proxy;

import javax.naming.directory.DirContext;

import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.transaction.compensating.LdapTransactionUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A proxy for ContextSource to make sure that the returned DirContext objects
 * are aware of the surrounding transactions. This makes sure that the
 * DirContext is not closed during the transaction and that all modifying
 * operations are recorded, keeping track of the corresponding rollback
 * operations. All returned DirContext instances will be of the type
 * {@link TransactionAwareDirContextInvocationHandler}.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class TransactionAwareContextSourceProxy implements ContextSource {
    private ContextSource target;

    /**
     * Constructor.
     * 
     * @param target
     *            the target ContextSource.
     */
    public TransactionAwareContextSourceProxy(ContextSource target) {
        this.target = target;
    }

    /**
     * Get the target ContextSource.
     * 
     * @return the target ContextSource.
     */
    public ContextSource getTarget() {
        return target;
    }

    /*
     * @see org.springframework.ldap.core.ContextSource#getReadOnlyContext()
     */
    public DirContext getReadOnlyContext() throws NamingException {
        return getReadWriteContext();
    }

    private DirContext getTransactionAwareDirContextProxy(DirContext context,
            ContextSource target) {
        return (DirContext) Proxy
                .newProxyInstance(DirContextProxy.class.getClassLoader(),
                        new Class[] {
                                LdapTransactionUtils
                                        .getActualTargetClass(context),
                                DirContextProxy.class },
                        new TransactionAwareDirContextInvocationHandler(
                                context, target));

    }

    /*
     * @see org.springframework.ldap.core.ContextSource#getReadWriteContext()
     */
    public DirContext getReadWriteContext() throws NamingException {
        DirContextHolder contextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(target);
        DirContext ctx = null;

        if (contextHolder != null) {
            ctx = contextHolder.getCtx();
        }

        if (ctx == null) {
            ctx = target.getReadWriteContext();
            if (contextHolder != null) {
                contextHolder.setCtx(ctx);
            }
        }
        return getTransactionAwareDirContextProxy(ctx, target);
    }
}
