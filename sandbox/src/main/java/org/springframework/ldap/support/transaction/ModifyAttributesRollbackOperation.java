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
import org.springframework.ldap.core.LdapOperations;

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

    private ModificationItem[] compensatingModifications;

    private ModificationItem[] actualModifications;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            The {@link LdapOperations} to use to perform the rollback
     *            operation.
     * @param dn
     *            the DN of the target entry.
     * @param actualModifications
     *            the actual modificationItems that were sent to the
     *            modifyAttributes operation.
     * @param compensatingModifications
     *            the ModificationItems to undo the recorded operation.
     */
    public ModifyAttributesRollbackOperation(LdapOperations ldapOperations,
            Name dn, ModificationItem[] actualModifications,
            ModificationItem[] compensatingModifications) {
        this.ldapOperations = ldapOperations;
        this.dn = dn;
        this.actualModifications = actualModifications;
        this.compensatingModifications = compensatingModifications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#rollback()
     */
    public void rollback() {
        try {
            log.debug("Rolling back modifyAttributes operation");
            ldapOperations.modifyAttributes(dn, compensatingModifications);
        } catch (Exception e) {
            log
                    .warn("Failed to rollback ModifyAttributes operation, dn: "
                            + dn);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#commit()
     */
    public void commit() {
        log.debug("Nothing to do in commit for modifyAttributes");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#performOperation()
     */
    public void performOperation() {
        log.debug("Performing modifyAttributes operation");
        ldapOperations.modifyAttributes(dn, actualModifications);
    }

    Name getDn() {
        return dn;
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    ModificationItem[] getActualModifications() {
        return actualModifications;
    }

    ModificationItem[] getCompensatingModifications() {
        return compensatingModifications;
    }

}
