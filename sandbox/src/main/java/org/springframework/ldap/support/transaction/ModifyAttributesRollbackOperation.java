package org.springframework.ldap.support.transaction;

import javax.naming.Name;
import javax.naming.directory.ModificationItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.LdapOperations;

public class ModifyAttributesRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private static Log log = LogFactory
            .getLog(ModifyAttributesRollbackOperation.class);

    private LdapOperations ldapOperations;

    private Name dn;

    private ModificationItem[] modificationItems;

    public ModifyAttributesRollbackOperation(LdapOperations ldapOperations,
            Name dn, ModificationItem[] modificationItems) {
        this.ldapOperations = ldapOperations;
        this.dn = dn;
        this.modificationItems = modificationItems;
    }

    public void rollback() {
        try {
            ldapOperations.modifyAttributes(dn, modificationItems);
        } catch (Exception e) {
            log
                    .warn("Failed to rollback ModifyAttributes operation, dn: "
                            + dn);
        }
    }

    Name getDn() {
        return dn;
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    ModificationItem[] getModificationItems() {
        return modificationItems;
    }

}
