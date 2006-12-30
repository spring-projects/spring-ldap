package org.springframework.ldap.support.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Stack;

import javax.naming.directory.DirContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.ContextSource;
import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.LdapTemplate;

public class LdapCompensatingTransactionDataManager implements
        CompensatingTransactionDataManager {
    private static Log log = LogFactory
            .getLog(LdapCompensatingTransactionDataManager.class);

    private Stack rollbackOperations = new Stack();

    private LdapOperations ldapOperations;

    public LdapCompensatingTransactionDataManager(DirContext ctx) {
        this.ldapOperations = new LdapTemplate(new SingleContextSource(ctx));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.CompensatingTransactionDataManager#operationPerformed(java.lang.String,
     *      java.lang.Object[])
     */
    public void operationPerformed(String operation, Object[] params) {
        CompensatingTransactionRecordingOperation recordingOperation = getRecordingOperation(operation);
        rollbackOperations.push(recordingOperation.performOperation(params));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.CompensatingTransactionDataManager#rollback()
     */
    public void rollback() {
        log.debug("Performing rollback");
        while (!rollbackOperations.isEmpty()) {
            CompensatingTransactionRollbackOperation rollbackOperation = (CompensatingTransactionRollbackOperation) rollbackOperations
                    .pop();
            rollbackOperation.rollback();
        }
    }

    /**
     * Get the rollback operations. Package protected for testing purposes.
     * 
     * @return the rollback operations.
     */
    Stack getRollbackOperations() {
        return rollbackOperations;
    }

    /**
     * Set the rollback operations. Package protected - for testing purposes
     * only.
     * 
     * @param rollbackOperations
     *            the rollback operations.
     */
    void setRollbackOperations(Stack rollbackOperations) {
        this.rollbackOperations = rollbackOperations;
    }

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

    static class SingleContextSource implements ContextSource {
        private DirContext ctx;

        public SingleContextSource(DirContext ctx) {
            this.ctx = ctx;
        }

        public DirContext getReadOnlyContext() throws DataAccessException {
            return getNonClosingDirContextProxy(ctx);
        }

        public DirContext getReadWriteContext() throws DataAccessException {
            return getNonClosingDirContextProxy(ctx);
        }

        private DirContext getNonClosingDirContextProxy(DirContext context) {
            return (DirContext) Proxy.newProxyInstance(DirContextProxy.class
                    .getClassLoader(), new Class[] { DirContextProxy.class },
                    new NonClosingDirContextInvocationHandler(context));

        }
    }

    public static class NonClosingDirContextInvocationHandler implements
            InvocationHandler {

        private DirContext target;

        public NonClosingDirContextInvocationHandler(DirContext target) {
            this.target = target;
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
