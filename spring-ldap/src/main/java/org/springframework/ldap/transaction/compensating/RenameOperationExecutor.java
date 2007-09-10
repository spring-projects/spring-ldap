/*
 * Copyright 2005-2007 the original author or authors.
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
package org.springframework.ldap.transaction.compensating;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

/**
 * A {@link CompensatingTransactionOperationExecutor} to manage a rename
 * operation. Performs a rename operation in {@link #performOperation()}, a
 * negating rename in {@link #rollback()}, and nothing in {@link #commit()}.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class RenameOperationExecutor implements
        CompensatingTransactionOperationExecutor {

    private static Log log = LogFactory.getLog(RenameOperationExecutor.class);

    private LdapOperations ldapOperations;

    private Name newDn;

    private Name originalDn;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            The {@link LdapOperations} to use for performing the rollback
     *            operation.
     * @param originalDn
     *            DN that the entry was moved from in the recorded operation.
     * @param newDn
     *            DN that the entry has been moved to in the recorded operation.
     */
    public RenameOperationExecutor(LdapOperations ldapOperations,
            Name originalDn, Name newDn) {
        this.ldapOperations = ldapOperations;
        this.originalDn = originalDn;
        this.newDn = newDn;
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#rollback()
     */
    public void rollback() {
        log.debug("Rolling back rename operation");
        try {
            ldapOperations.rename(newDn, originalDn);
        } catch (Exception e) {
            log.warn("Unable to rollback rename operation. " + "originalDn: "
                    + newDn + "; newDn: " + originalDn);
        }
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#commit()
     */
    public void commit() {
        log.debug("Nothing to do in commit for rename operation");
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#performOperation()
     */
    public void performOperation() {
        log.debug("Performing rename operation");
        ldapOperations.rename(originalDn, newDn);
    }

    Name getNewDn() {
        return newDn;
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    Name getOriginalDn() {
        return originalDn;
    }
}
