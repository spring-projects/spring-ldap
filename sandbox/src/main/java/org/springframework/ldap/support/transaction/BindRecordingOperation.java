package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.springframework.ldap.LdapOperations;

public class BindRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    private LdapOperations ldapOperations;

    public BindRecordingOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    public CompensatingTransactionRollbackOperation performOperation(
            Object[] args) {
        Name dn = LdapUtils.getFirstArgumentAsName(args);
        return new UnbindRollbackOperation(ldapOperations, dn);
    }

}
