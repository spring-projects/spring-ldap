package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.support.DirContextAdapter;

public class RebindRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    private static Log log = LogFactory.getLog(RebindRecordingOperation.class);

    private LdapOperations ldapOperations;

    public RebindRecordingOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    public CompensatingTransactionRollbackOperation performOperation(
            Object[] args) {
        Name dn = LdapUtils.getFirstArgumentAsName(args);

        try {
            DirContextAdapter ctx = (DirContextAdapter) ldapOperations
                    .lookup(dn);
            return new RebindRollbackOperation(ldapOperations, ctx);
        } catch (Exception e) {
            log.warn(
                    "Failed to create rollback operation, dn " + dn.toString(),
                    e);
            return new NullRollbackOperation();
        }
    }
}
