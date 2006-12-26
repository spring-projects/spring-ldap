package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.support.DirContextOperations;

public class RebindRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private static Log log = LogFactory.getLog(RebindRollbackOperation.class);

    private LdapOperations ldapOperations;

    private DirContextOperations dirContextOperations;

    public RebindRollbackOperation(LdapOperations ldapOperations,
            DirContextOperations dirContextOperations) {
        this.ldapOperations = ldapOperations;
        this.dirContextOperations = dirContextOperations;
    }

    /**
     * Get the targegt DirContextOperations. Package private for testing
     * purposes.
     * 
     * @return the DirContextOperations.
     */
    DirContextOperations getDirContextOperations() {
        return dirContextOperations;
    }

    /**
     * Get the LdapOperations. Package private for testing purposes.
     * 
     * @return the LdapOperations.
     */
    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    public void rollback() {
        Name dn = dirContextOperations.getDn();
        try {
            ldapOperations.rebind(dn, dirContextOperations, null);
        } catch (Exception e) {
            log.warn("Failed to rollback operation, dn: " + dn, e);
        }
    }

}
