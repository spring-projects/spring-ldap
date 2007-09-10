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
package org.springframework.ldap.transaction.compensating;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.directory.DirContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.transaction.compensating.CompensatingTransactionOperationFactory;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;

/**
 * {@link CompensatingTransactionOperationRecorder} implementation for LDAP
 * operations.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class LdapCompensatingTransactionOperationFactory implements
        CompensatingTransactionOperationFactory {
    private static Log log = LogFactory
            .getLog(LdapCompensatingTransactionOperationFactory.class);

    private TempEntryRenamingStrategy renamingStrategy;

    /**
     * Constructor.
     * 
     * @param renamingStrategy
     *            the {@link TempEntryRenamingStrategy} to supply to relevant
     *            operations.
     */
    public LdapCompensatingTransactionOperationFactory(
            TempEntryRenamingStrategy renamingStrategy) {
        this.renamingStrategy = renamingStrategy;
    }

    /*
     * @see org.springframework.transaction.compensating.CompensatingTransactionOperationFactory#createRecordingOperation(java.lang.Object,
     *      java.lang.String)
     */
    public CompensatingTransactionOperationRecorder createRecordingOperation(
            Object resource, String operation) {
        if (StringUtils
                .equals(operation, LdapTransactionUtils.BIND_METHOD_NAME)) {
            log.debug("Bind operation recorded");
            return new BindOperationRecorder(
                    createLdapOperationsInstance((DirContext) resource));
        } else if (StringUtils.equals(operation,
                LdapTransactionUtils.REBIND_METHOD_NAME)) {
            log.debug("Rebind operation recorded");
            return new RebindOperationRecorder(
                    createLdapOperationsInstance((DirContext) resource),
                    renamingStrategy);
        } else if (StringUtils.equals(operation,
                LdapTransactionUtils.RENAME_METHOD_NAME)) {
            log.debug("Rename operation recorded");
            return new RenameOperationRecorder(
                    createLdapOperationsInstance((DirContext) resource));
        } else if (StringUtils.equals(operation,
                LdapTransactionUtils.MODIFY_ATTRIBUTES_METHOD_NAME)) {
            return new ModifyAttributesOperationRecorder(
                    createLdapOperationsInstance((DirContext) resource));
        } else if (StringUtils.equals(operation,
                LdapTransactionUtils.UNBIND_METHOD_NAME)) {
            return new UnbindOperationRecorder(
                    createLdapOperationsInstance((DirContext) resource),
                    renamingStrategy);
        }

        log
                .warn("No suitable CompensatingTransactionOperationRecorder found for method "
                        + operation + ". Operation will not be transacted.");
        return new NullOperationRecorder();
    }

    LdapOperations createLdapOperationsInstance(DirContext ctx) {
        return new LdapTemplate(new SingleContextSource(ctx));
    }

    /**
     * A {@link ContextSource} implementation using returning
     * {@link NonClosingDirContextInvocationHandler} proxies on the same
     * DirContext instance for each call.
     * 
     * @author Mattias Arthursson
     */
    static class SingleContextSource implements ContextSource {
        private DirContext ctx;

        /**
         * Constructor.
         * 
         * @param ctx
         *            the target DirContext.
         */
        public SingleContextSource(DirContext ctx) {
            this.ctx = ctx;
        }

        /*
         * @see org.springframework.ldap.ContextSource#getReadOnlyContext()
         */
        public DirContext getReadOnlyContext() throws NamingException {
            return getNonClosingDirContextProxy(ctx);
        }

        /*
         * @see org.springframework.ldap.ContextSource#getReadWriteContext()
         */
        public DirContext getReadWriteContext() throws NamingException {
            return getNonClosingDirContextProxy(ctx);
        }

        private DirContext getNonClosingDirContextProxy(DirContext context) {
            return (DirContext) Proxy.newProxyInstance(DirContextProxy.class
                    .getClassLoader(), new Class[] {
                    LdapTransactionUtils.getActualTargetClass(context),
                    DirContextProxy.class },
                    new NonClosingDirContextInvocationHandler(context));

        }
    }

    /**
     * A proxy for DirContext forwarding all operation to the target DirContext,
     * but making sure that no <code>close</code> operations will be
     * performed.
     * 
     * @author Mattias Arthursson
     */
    public static class NonClosingDirContextInvocationHandler implements
            InvocationHandler {

        private DirContext target;

        public NonClosingDirContextInvocationHandler(DirContext target) {
            this.target = target;
        }

        /*
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
                // Never close the target context, as this class will only be
                // used for operations concerning the compensating transactions.
                return null;
            }

            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
