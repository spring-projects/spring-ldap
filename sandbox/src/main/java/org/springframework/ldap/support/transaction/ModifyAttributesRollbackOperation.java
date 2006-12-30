/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.support.transaction;

import javax.naming.Name;
import javax.naming.directory.ModificationItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.LdapOperations;

/**
 * A {@link CompensatingTransactionRollbackOperation} to rollback a
 * <code>modifyAttributes</code> operation. Performs a
 * <code>modifyAttributes</code> operation using the DN of the target entry
 * and ModificationItems undoing the modifications of the recorded operation.
 * 
 * @author Mattias Arthursson
 */
public class ModifyAttributesRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private static Log log = LogFactory
            .getLog(ModifyAttributesRollbackOperation.class);

    private LdapOperations ldapOperations;

    private Name dn;

    private ModificationItem[] modificationItems;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            The {@link LdapOperations} to use to perform the rollback
     *            operation.
     * @param dn
     *            the DN of the target entry.
     * @param modificationItems
     *            the ModificationItems to undo the recorded operation.
     */
    public ModifyAttributesRollbackOperation(LdapOperations ldapOperations,
            Name dn, ModificationItem[] modificationItems) {
        this.ldapOperations = ldapOperations;
        this.dn = dn;
        this.modificationItems = modificationItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#rollback()
     */
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
