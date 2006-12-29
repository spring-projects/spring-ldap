package org.springframework.ldap.support.transaction;

import org.springframework.ldap.LdapOperations;

public class ModifyAttributesRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private LdapOperations ldapOperations;
    
    public ModifyAttributesRollbackOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    public void rollback() {
        throw new UnsupportedOperationException("Not implemented");
    }

}
