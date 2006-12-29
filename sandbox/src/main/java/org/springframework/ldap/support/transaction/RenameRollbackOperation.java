package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.LdapOperations;

public class RenameRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private static Log log = LogFactory.getLog(RenameRollbackOperation.class);

    private LdapOperations ldapOperations;

    private Name fromDn;

    private Name toDn;

    public RenameRollbackOperation(LdapOperations ldapOperations, Name fromDn,
            Name toDn) {
        this.ldapOperations = ldapOperations;
        this.fromDn = fromDn;
        this.toDn = toDn;
    }

    public void rollback() {
        log.debug("Rolling back rename operation");
        try {
            ldapOperations.rename(fromDn, toDn);
        } catch (Exception e) {
            log.warn("Unable to rollback rename operation. " + "fromDn: "
                    + fromDn + "; toDn: " + toDn);
        }
    }

    Name getFromDn() {
        return fromDn;
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    Name getToDn() {
        return toDn;
    }

}
