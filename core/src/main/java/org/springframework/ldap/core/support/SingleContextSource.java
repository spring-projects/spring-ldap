/*
 * Copyright 2005-2013 the original author or authors.
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
package org.springframework.ldap.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.directory.DirContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A {@link ContextSource} to be used as a decorator around a target ContextSource
 * to make sure the target is never actually closed. Useful when working with e.g. paged results,
 * as these require the same target to be used.
 *
 * @author Mattias Hellborg Arthursson
 */
public class SingleContextSource implements ContextSource, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(SingleContextSource.class);
    private static final boolean DONT_USE_READ_ONLY = false;
    private static final boolean DONT_IGNORE_PARTIAL_RESULT = false;
    private static final boolean DONT_IGNORE_NAME_NOT_FOUND = false;

    private final DirContext ctx;

    /**
     * Constructor.
     *
     * @param ctx the target DirContext.
     */
    public SingleContextSource(DirContext ctx) {
        this.ctx = ctx;
    }

    /*
      * @see org.springframework.ldap.ContextSource#getReadOnlyContext()
      */
    public DirContext getReadOnlyContext() {
        return getNonClosingDirContextProxy(ctx);
    }

    /*
      * @see org.springframework.ldap.ContextSource#getReadWriteContext()
      */
    public DirContext getReadWriteContext() {
        return getNonClosingDirContextProxy(ctx);
    }

    private DirContext getNonClosingDirContextProxy(DirContext context) {
        return (DirContext) Proxy.newProxyInstance(DirContextProxy.class
                .getClassLoader(), new Class<?>[]{
                LdapUtils.getActualTargetClass(context),
                DirContextProxy.class},
                new SingleContextSource.NonClosingDirContextInvocationHandler(
                        context));

    }

    public DirContext getContext(String principal, String credentials) {
        throw new UnsupportedOperationException(
                "Not a valid operation for this type of ContextSource");
    }

    /**
     * Destroy method that allows the target DirContext to be cleaned up when
     * the SingleContextSource is not going to be used any more.
     */
    public void destroy() {
        try {
            ctx.close();
        }
        catch (javax.naming.NamingException e) {
            LOG.warn("Error when closing", e);
        }
    }

    /**
     * Construct a SingleContextSource and execute the LdapOperationsCallback using the created instance.
     * This makes sure the same connection will be used for all operations inside the LdapOperationsCallback,
     * which is particularly useful when working with e.g. Paged Results as these typically require the exact
     * same connection to be used for all requests involving the same cookie.
     * The SingleContextSource instance will be properly disposed of once the operation has been completed.
     * <p>By default, the {@link org.springframework.ldap.core.ContextSource#getReadWriteContext()} method
     * will be used to create the DirContext instance to operate on.</p>
     *
     * @param contextSource The target ContextSource to retrieve a DirContext from.
     * @param callback the callback to perform the Ldap operations.
     * @return the result returned from the callback.
     * @see #doWithSingleContext(org.springframework.ldap.core.ContextSource, LdapOperationsCallback, boolean, boolean, boolean)
     * @since 2.0
     */
    public static <T> T doWithSingleContext(ContextSource contextSource, LdapOperationsCallback<T> callback) {
        return doWithSingleContext(contextSource, callback, DONT_USE_READ_ONLY, DONT_IGNORE_PARTIAL_RESULT, DONT_IGNORE_NAME_NOT_FOUND);

    }

    /**
     * Construct a SingleContextSource and execute the LdapOperationsCallback using the created instance.
     * This makes sure the same connection will be used for all operations inside the LdapOperationsCallback,
     * which is particularly useful when working with e.g. Paged Results as these typically require the exact
     * same connection to be used for all requests involving the same cookie..
     * The SingleContextSource instance will be properly disposed of once the operation has been completed.
     *
     * @param contextSource The target ContextSource to retrieve a DirContext from
     * @param callback the callback to perform the Ldap operations
     * @param useReadOnly if <code>true</code>, use the {@link org.springframework.ldap.core.ContextSource#getReadOnlyContext()}
     *                    method on the target ContextSource to get the actual DirContext instance, if <code>false</code>,
     *                    use {@link org.springframework.ldap.core.ContextSource#getReadWriteContext()}.
     * @param ignorePartialResultException Used for populating this property on the created LdapTemplate instance.
     * @param ignoreNameNotFoundException Used for populating this property on the created LdapTemplate instance.
     * @return the result returned from the callback.
     * @since 2.0
     */
    public static <T> T doWithSingleContext(ContextSource contextSource,
                                            LdapOperationsCallback<T> callback,
                                            boolean useReadOnly,
                                            boolean ignorePartialResultException,
                                            boolean ignoreNameNotFoundException) {
        SingleContextSource singleContextSource;
        if (useReadOnly) {
            singleContextSource = new SingleContextSource(contextSource.getReadOnlyContext());
        } else {
            singleContextSource = new SingleContextSource(contextSource.getReadWriteContext());
        }

        LdapTemplate ldapTemplate = new LdapTemplate(singleContextSource);
        ldapTemplate.setIgnorePartialResultException(ignorePartialResultException);
        ldapTemplate.setIgnoreNameNotFoundException(ignoreNameNotFoundException);

        try {
            return callback.doWithLdapOperations(ldapTemplate);
        } finally {
            singleContextSource.destroy();
        }
    }

    /**
     * A proxy for DirContext forwarding all operation to the target DirContext,
     * but making sure that no <code>close</code> operations will be performed.
     *
     * @author Mattias Hellborg Arthursson
     */
    public static class NonClosingDirContextInvocationHandler implements
            InvocationHandler {

        private DirContext target;

        public NonClosingDirContextInvocationHandler(DirContext target) {
            this.target = target;
        }

        /*
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         * java.lang.reflect.Method, java.lang.Object[])
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
                return proxy.hashCode();
            } else if (methodName.equals("close")) {
                // Never close the target context, as this class will only be
                // used for operations concerning the compensating transactions.
                return null;
            }

            try {
                return method.invoke(target, args);
            }
            catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
