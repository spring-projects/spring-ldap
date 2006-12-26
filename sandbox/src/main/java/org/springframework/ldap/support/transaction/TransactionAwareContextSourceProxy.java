package org.springframework.ldap.support.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.dao.DataAccessException;
import org.springframework.ldap.ContextSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionAwareContextSourceProxy implements ContextSource {

    private ContextSource target;

    public TransactionAwareContextSourceProxy(ContextSource target) {
        this.target = target;
    }

    public DirContext getReadOnlyContext() throws DataAccessException {
        DirContextHolder contextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(target);
        DirContext ctx = null;

        if (contextHolder != null) {
            ctx = contextHolder.getCtx();
        }

        if (ctx == null) {
            ctx = target.getReadOnlyContext();
            if (contextHolder != null) {
                contextHolder.setCtx(ctx);
            }
        }
        return getTransactionAwareDirContextProxy(ctx, target);
    }

    private DirContext getTransactionAwareDirContextProxy(DirContext context,
            ContextSource target) {
        return (DirContext) Proxy
                .newProxyInstance(DirContextProxy.class.getClassLoader(),
                        new Class[] { DirContextProxy.class },
                        new TransactionAwareDirContextInvocationHandler(
                                context, target));

    }

    public DirContext getReadWriteContext() throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    static class TransactionAwareDirContextInvocationHandler implements
            InvocationHandler {
        private DirContext target;

        private ContextSource contextSource;

        public TransactionAwareDirContextInvocationHandler(DirContext target,
                ContextSource contextSource) {
            this.target = target;
            this.contextSource = contextSource;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {

            if (method.getName().equals("getTargetContext")) {
                return target;
            } else if (method.getName().equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
            } else if (method.getName().equals("hashCode")) {
                // Use hashCode of Connection proxy.
                return new Integer(hashCode());
            } else if (method.getName().equals("close")) {
                doCloseConnection(target, contextSource);
                return null;
            }

            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        private void doCloseConnection(DirContext context,
                ContextSource contextSource) throws NamingException {
            DirContextHolder transactionContextHolder = (DirContextHolder) TransactionSynchronizationManager
                    .getResource(contextSource);
            if (transactionContextHolder == null
                    || transactionContextHolder.getCtx() != context) {
                // This is not the transactional context or the transaction is
                // no longer active - we should close it.
                context.close();
            }
        }
    }
}
