package org.springframework.ldap.support.transaction;

import java.lang.reflect.Proxy;

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
}
