/**
 * 
 */
package org.springframework.ldap.support.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.ContextSource;

public class TransactionAwareDirContextInvocationHandler implements
        InvocationHandler {
    private static Log log = LogFactory
            .getLog(TransactionAwareDirContextInvocationHandler.class);

    private DirContext target;

    private ContextSource contextSource;

    public TransactionAwareDirContextInvocationHandler(DirContext target,
            ContextSource contextSource) {
        this.target = target;
        this.contextSource = contextSource;
    }

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
            LdapUtils.storeCompensatingTransactionData(contextSource, methodName, args);
        }

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}