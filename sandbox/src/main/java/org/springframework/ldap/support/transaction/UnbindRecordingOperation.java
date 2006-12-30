package org.springframework.ldap.support.transaction;

import javax.naming.Name;

import org.springframework.ldap.LdapOperations;
import org.springframework.ldap.support.DirContextAdapter;

public class UnbindRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    private LdapOperations ldapOperations;

    public UnbindRecordingOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    public CompensatingTransactionRollbackOperation performOperation(
            Object[] args) {
        Name dn = LdapUtils.getFirstArgumentAsName(args);
        DirContextAdapter ctx = (DirContextAdapter) ldapOperations.lookup(dn);
        return new BindRollbackOperation(ldapOperations, ctx);
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }
}
