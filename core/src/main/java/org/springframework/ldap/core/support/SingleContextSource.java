/*
 * Copyright 2005-2008 the original author or authors.
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
package org.springframework.ldap.core.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextProxy;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.beans.factory.DisposableBean;

/**
 * A {@link ContextSource} to be used as a decorator around a target ContextSource
 * to make sure the target is never actually closed. Useful when working with e.g. paged results,
 * as these require the same target to be used.
 *
 * @author Mattias Arthursson
 */
public class SingleContextSource implements ContextSource, DisposableBean {

    private static final Log log = LogFactory.getLog(SingleContextSource.class);

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
                .getClassLoader(), new Class[]{
                LdapUtils.getActualTargetClass(context),
                DirContextProxy.class},
                new SingleContextSource.NonClosingDirContextInvocationHandler(
                        context));

    }

    public DirContext getContext(String principal, String credentials)
            throws NamingException {
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
            log.warn(e);
        }
    }

    /**
     * A proxy for DirContext forwarding all operation to the target DirContext,
     * but making sure that no <code>close</code> operations will be performed.
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
                return new Integer(proxy.hashCode());
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