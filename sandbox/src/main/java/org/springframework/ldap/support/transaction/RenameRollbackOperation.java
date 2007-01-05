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
import org.springframework.ldap.core.LdapOperations;

/**
 * A {@link CompensatingTransactionRollbackOperation} to roll back a previous
 * rename operation. Has a fromDn and a toDn, representing from and to in the
 * rename operation to be rolled back.
 * 
 * @author Mattias Arthursson
 * 
 */
public class RenameRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private static Log log = LogFactory.getLog(RenameRollbackOperation.class);

    private LdapOperations ldapOperations;

    private Name fromDn;

    private Name toDn;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            The {@link LdapOperations} to use for performing the rollback
     *            operation.
     * @param fromDn
     *            DN that the entry has been moved to in the recorded operation.
     * @param toDn
     *            DN that the entry was moved from in the recorded operation.
     */
    public RenameRollbackOperation(LdapOperations ldapOperations, Name fromDn,
            Name toDn) {
        this.ldapOperations = ldapOperations;
        this.fromDn = fromDn;
        this.toDn = toDn;
    }

    public void rollback() {
        log.debug("Rolling back rename operation");
        try {
            ldapOperations.rename(fromDn, toDn);
        } catch (Exception e) {
            log.warn("Unable to rollback rename operation. " + "fromDn: "
                    + fromDn + "; toDn: " + toDn);
        }
    }

    Name getFromDn() {
        return fromDn;
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    Name getToDn() {
        return toDn;
    }

}
