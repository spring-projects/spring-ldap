package org.springframework.ldap.support.transaction;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.ContextSource;
import org.springframework.ldap.support.DistinguishedName;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Common helper methods for Ldap operations.
 * 
 * @author Mattias Arthursson
 */
public class LdapUtils {
    private static Log log = LogFactory.getLog(LdapUtils.class);

    public static final String REBIND_METHOD_NAME = "rebind";

    public static final String BIND_METHOD_NAME = "bind";

    public static final String RENAME_METHOD_NAME = "rename";

    /**
     * Not to be instantiated.
     */
    private LdapUtils() {
    }

    /**
     * Get the first parameter in the argument list as a Name.
     * 
     * @param args
     *            arguments supplied to a ldap operation.
     * @return a Name representation of the first argument, or the Name itself
     *         if it is a name.
     */
    public static Name getFirstArgumentAsName(Object[] args) {
        Assert.notEmpty(args);

        Object firstArg = args[0];
        return getArgumentAsName(firstArg);
    }

    /**
     * Get the argument as a Name.
     * 
     * @param arg
     *            an argument supplied to an Ldap operation.
     * @return a Name representation of the argument, or the Name itself if it
     *         is a Name.
     */
    public static Name getArgumentAsName(Object arg) {
        if (arg instanceof String) {
            return new DistinguishedName((String) arg);
        } else if (arg instanceof Name) {
            return (Name) arg;
        } else {
            throw new IllegalArgumentException(
                    "First argument needs to be a Name or a String representation thereof");
        }
    }

    /**
     * Close the supplied context, but only if it is not associated with the
     * current transaction.
     * 
     * @param context
     *            the DirContext to close.
     * @param contextSource
     *            the ContextSource bound to the transaction.
     * @throws NamingException
     */
    public static void doCloseConnection(DirContext context,
            ContextSource contextSource) throws NamingException {
        DirContextHolder transactionContextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(contextSource);
        if (transactionContextHolder == null
                || transactionContextHolder.getCtx() != context) {
            log.debug("Closing context");
            // This is not the transactional context or the transaction is
            // no longer active - we should close it.
            context.close();
        } else {
            log.debug("Leaving transactional context open");
        }
    }

    /**
     * Check whether the supplied method is a method for which transactions is
     * supported (and which should be recorded for possible rollback later).
     * 
     * @param methodName
     *            name of the method to check.
     * @return <code>true</code> if this is a supported transaction operation,
     *         <code>false</code> otherwise.
     */
    public static boolean isSupportedWriteTransactionOperation(String methodName) {
        return (StringUtils.equals(methodName, BIND_METHOD_NAME)
                || StringUtils.equals(methodName, REBIND_METHOD_NAME) || StringUtils
                .equals(methodName, RENAME_METHOD_NAME));

    }

    /**
     * Store compensating transaction data for the supplied operation and
     * arguments.
     * 
     * @param contextSource
     *            the ContextSource we are operating on.
     * @param methodName
     *            name of the method to be invoked.
     * @param args
     *            arguments with which the operation is invoked.
     */
    public static void storeCompensatingTransactionData(
            ContextSource contextSource, String methodName, Object[] args) {
        DirContextHolder transactionContextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(contextSource);
        if (transactionContextHolder != null) {
            CompensatingTransactionDataManager transactionDataManager = transactionContextHolder
                    .getTransactionDataManager();
            transactionDataManager.operationPerformed(methodName, args);
        }
    }
}
