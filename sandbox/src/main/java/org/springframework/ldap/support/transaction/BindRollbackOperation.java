package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.support.DirContextOperations;

public class BindRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private static Log log = LogFactory.getLog(BindRollbackOperation.class);

    private LdapOperations ldapOperations;

    private DirContextOperations dirContextOperations;

    public BindRollbackOperation(LdapOperations ldapOperations,
            DirContextOperations dirContextOperations) {
        this.ldapOperations = ldapOperations;
        this.dirContextOperations = dirContextOperations;
    }

    public void rollback() {
        Name dn = dirContextOperations.getDn();
        try {
            ldapOperations.bind(dn, dirContextOperations, null);
        } catch (Exception e) {
            log.warn("Filed to rollback unbind operation, dn: " + dn);
        }
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    DirContextOperations getDirContextOperations() {
        return dirContextOperations;
    }

}
