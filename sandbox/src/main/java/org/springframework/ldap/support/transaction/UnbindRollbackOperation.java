package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.LdapOperations;

public class UnbindRollbackOperation implements
        CompensatingTransactionRollbackOperation {
    private static Log log = LogFactory.getLog(UnbindRollbackOperation.class);

    private LdapOperations ldapOperations;

    private Name dn;

    public UnbindRollbackOperation(LdapOperations ldapOperations, Name dn) {
        this.ldapOperations = ldapOperations;
        this.dn = dn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#rollback()
     */
    public void rollback() {
        try {
            ldapOperations.unbind(dn);
        } catch (Exception e) {
            log.warn("Failed to rollback, dn:" + dn.toString(), e);
        }
    }

    /**
     * Get the DN. Package private for testing purposes.
     * 
     * @return the target DN.
     */
    Name getDn() {
        return dn;
    }

    /**
     * Get the LdapOperations. Package private for testing purposes.
     * 
     * @return the LdapOperations.
     */
    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

}
