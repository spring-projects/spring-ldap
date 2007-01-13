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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapOperations;

/**
 * A {@link CompensatingTransactionRollbackOperation} to rollback an unbind
 * operation. This implementation performs a bind operation using the
 * {@link DirContextOperations} instance supplied on construction.
 * 
 * @author Mattias Arthursson
 */
public class BindRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private static Log log = LogFactory.getLog(BindRollbackOperation.class);

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
    public BindRollbackOperation(LdapOperations ldapOperations,
            Name originalDn, Name temporaryDn) {
        this.ldapOperations = ldapOperations;
        this.originalDn = originalDn;
        this.temporaryDn = temporaryDn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#rollback()
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
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#commit()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#commit()
     */
    public void commit() {
        log.debug("Committing unbind operation - unbinding temporary entry");
        ldapOperations.unbind(temporaryDn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#performOperation()
     */
    public void performOperation() {
        log.debug("Nothing to do in performOperation for unbind");
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
