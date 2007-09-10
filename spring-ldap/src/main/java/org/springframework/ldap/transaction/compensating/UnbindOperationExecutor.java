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
 * A {@link CompensatingTransactionOperationExecutor} to manage an unbind
 * operation. The methods in this class do not behave as expected, since it
 * might be impossible to retrieve all the original attributes from the entry.
 * Instead this class performs a <b>rename</b> in {@link #performOperation()},
 * a negating rename in {@link #rollback()}, and {@link #commit()} unbinds the
 * entry from its temporary location.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class UnbindOperationExecutor implements
        CompensatingTransactionOperationExecutor {

    private static Log log = LogFactory.getLog(UnbindOperationExecutor.class);

    private LdapOperations ldapOperations;

    private Name originalDn;

    private Name temporaryDn;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            The {@link LdapOperations} to use for performing the rollback
     *            operation.
     * @param originalDn
     *            The original DN of the entry to be removed.
     * @param temporaryDn
     *            Temporary DN of the entry to be removed; this is where the
     *            entry is temporarily stored during the transaction.
     */
    public UnbindOperationExecutor(LdapOperations ldapOperations,
            Name originalDn, Name temporaryDn) {
        this.ldapOperations = ldapOperations;
        this.originalDn = originalDn;
        this.temporaryDn = temporaryDn;
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#rollback()
     */
    public void rollback() {
        try {
            ldapOperations.rename(temporaryDn, originalDn);
        } catch (Exception e) {
            log.warn("Filed to rollback unbind operation, temporaryDn: "
                    + temporaryDn + "; originalDn: " + originalDn);
        }
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#commit()
     */
    public void commit() {
        log.debug("Committing unbind operation - unbinding temporary entry");
        ldapOperations.unbind(temporaryDn);
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#performOperation()
     */
    public void performOperation() {
        log.debug("Performing operation for unbind -"
                + " renaming to temporary entry.");
        ldapOperations.rename(originalDn, temporaryDn);
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    Name getOriginalDn() {
        return originalDn;
    }

    Name getTemporaryDn() {
        return temporaryDn;
    }
}
