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
package org.springframework.ldap.support.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.directory.DirContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;

/**
 * A {@link CompensatingTransactionDataManager} for LDAP operations. Called by
 * {@link TransactionAwareDirContextInvocationHandler} to record LDAP operations
 * and keep track of rollback operations. Manages <code>bind</code>,
 * <code>rebind</code>, <code>unbind</code>, <code>rename</code> and
 * <code>modifyAttributes</code> operations. All created
 * {@link CompensatingTransactionRecordingOperation} objects (and consequently,
 * all {@link CompensatingTransactionRollbackOperation} objects created by
 * these) will use the transactional DirContext instance to perform all their
 * necessary operations, via an internal {@link LdapTemplate} instance referring
 * a special ContextSource implementation..
 * 
 * @author Mattias Arthursson
 */
public class LdapCompensatingTransactionDataManager extends
        AbstractCompensatingTransactionDataManager {
    private static Log log = LogFactory
            .getLog(LdapCompensatingTransactionDataManager.class);

    private LdapOperations ldapOperations;

    /**
     * Constructor.
     * 
     * @param ctx
     *            The transactional DirContext.
     */
    public LdapCompensatingTransactionDataManager(DirContext ctx) {
        this.ldapOperations = new LdapTemplate(new SingleContextSource(ctx));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.AbstractCompensatingTransactionDataManager#getRecordingOperation(java.lang.String)
     */
    protected CompensatingTransactionRecordingOperation getRecordingOperation(
            String operation) {
        if (StringUtils.equals(operation, LdapUtils.BIND_METHOD_NAME)) {
            log.debug("Bind operation recorded");
            return new BindRecordingOperation(ldapOperations);
        } else if (StringUtils.equals(operation, LdapUtils.REBIND_METHOD_NAME)) {
            log.debug("Rebind operation recorded");
            return new RebindRecordingOperation(ldapOperations);
        } else if (StringUtils.equals(operation, LdapUtils.RENAME_METHOD_NAME)) {
            log.debug("Rename operation recorded");
            return new RenameRecordingOperation(ldapOperations);
        } else if (StringUtils.equals(operation,
                LdapUtils.MODIFY_ATTRIBUTES_METHOD_NAME)) {
            return new ModifyAttributesRecordingOperation(ldapOperations);
        } else if (StringUtils.equals(operation, LdapUtils.UNBIND_METHOD_NAME)) {
            return new UnbindRecordingOperation(ldapOperations);
        }

        log
                .warn("No suitable CompensatingTransactionRecordingOperation found for method "
                        + operation + ". Operation will not be transacted.");
        return new NullRecordingOperation();
    }

    /**
     * Set the LdapOperations to use. For testing purposes only.
     * 
     * @param ldapOperations
     *            the LdapOperations to use.
     */
    void setLdapOperations(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
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
         * (non-Javadoc)
         * 
         * @see org.springframework.ldap.core.ContextSource#getReadOnlyContext()
         */
        public DirContext getReadOnlyContext() throws DataAccessException {
            return getNonClosingDirContextProxy(ctx);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.ldap.core.ContextSource#getReadWriteContext()
         */
        public DirContext getReadWriteContext() throws DataAccessException {
            return getNonClosingDirContextProxy(ctx);
        }

        private DirContext getNonClosingDirContextProxy(DirContext context) {
            return (DirContext) Proxy.newProxyInstance(DirContextProxy.class
                    .getClassLoader(), new Class[] { DirContextProxy.class },
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
