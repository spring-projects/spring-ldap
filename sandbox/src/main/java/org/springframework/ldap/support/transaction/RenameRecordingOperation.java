package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.LdapOperations;
import org.springframework.util.Assert;

public class RenameRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    private static Log log = LogFactory.getLog(RenameRecordingOperation.class);

    private LdapOperations ldapOperations;

    public RenameRecordingOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    public CompensatingTransactionRollbackOperation performOperation(
            Object[] args) {
        log.debug("Storing rollback information for rename operation");
        Assert.notEmpty(args);
        if (args.length != 2) {
            // This really shouldn't happen.
            throw new IllegalArgumentException("Illegal argument length");
        }
        Name oldDn = LdapUtils.getArgumentAsName(args[0]);
        Name newDn = LdapUtils.getArgumentAsName(args[1]);
        return new RenameRollbackOperation(ldapOperations, newDn, oldDn);
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

}
