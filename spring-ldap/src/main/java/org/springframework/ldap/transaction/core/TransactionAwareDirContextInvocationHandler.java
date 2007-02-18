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
package org.springframework.ldap.transaction.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.naming.directory.DirContext;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.support.LdapUtils;

/**
 * Proxy implementation for DirContext, making sure that the instance is not
 * closed during a transaction, and that all modifying operations are recorded,
 * storing compensating rollback operations for them.
 * 
 * @author Mattias Arthursson
 */
public class TransactionAwareDirContextInvocationHandler implements
        InvocationHandler {

    private DirContext target;

    private ContextSource contextSource;

    /**
     * Constructor.
     * 
     * @param target
     *            The target DirContext.
     * @param contextSource
     *            The transactional ContextSource, needed to get hold of the
     *            current transaction's {@link DirContextHolder}.
     */
    public TransactionAwareDirContextInvocationHandler(DirContext target,
            ContextSource contextSource) {
        this.target = target;
        this.contextSource = contextSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {

        String methodName = method.getName();
        if (methodName.equals("getTargetContext")) {
            return target;
        } else if (methodName.equals("equals")) {
            // Only consider equal when proxies are identical.
            return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
        } else if (methodName.equals("hashCode")) {
            // Use hashCode of Connection proxy.
            return new Integer(proxy.hashCode());
        } else if (methodName.equals("close")) {
            LdapUtils.doCloseConnection(target, contextSource);
            return null;
        } else if (LdapUtils.isSupportedWriteTransactionOperation(methodName)) {
            // Store transaction data and allow operation to proceed.
            LdapUtils.performOperation(contextSource, method, args);
            return null;
        } else {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}