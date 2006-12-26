package org.springframework.ldap.support.transaction;

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
            return new BindRecordingOperation(ldapOperations);
        } else if (StringUtils.equals(operation, LdapUtils.REBIND_METHOD_NAME)) {
            return new RebindRecordingOperation(ldapOperations);
        }

        log
                .warn("No suitable CompensatingTransactionRecordingOperation found for method "
                        + operation + ". Operation will not be transacted.");
        return new NullRecordingOperation();
    }

    static class SingleContextSource implements ContextSource {
        private DirContext ctx;

        public SingleContextSource(DirContext ctx) {
            this.ctx = ctx;
        }

        public DirContext getReadOnlyContext() throws DataAccessException {
            return ctx;
        }

        public DirContext getReadWriteContext() throws DataAccessException {
            return ctx;
        }

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

}
