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
import org.springframework.ldap.LdapOperations;

/**
 * A {@link CompensatingTransactionRollbackOperation} to rollback a bind
 * operation. Unbinds the entry using the supplied DN.
 * 
 * @author Mattias Arthursson
 */
public class UnbindRollbackOperation implements
        CompensatingTransactionRollbackOperation {
    private static Log log = LogFactory.getLog(UnbindRollbackOperation.class);

    private LdapOperations ldapOperations;

    private Name dn;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            {@link LdapOperations} to use for performing the rollback
     *            operation.
     * @param dn
     *            DN of the entry to be unbound.
     */
    public UnbindRollbackOperation(LdapOperations ldapOperations, Name dn) {
        this.ldapOperations = ldapOperations;
        this.dn = dn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#rollback()
     */
    public void rollback() {
        try {
            ldapOperations.unbind(dn);
        } catch (Exception e) {
            log.warn("Failed to rollback, dn:" + dn.toString(), e);
        }
    }

    /**
     * Get the DN. Package private for testing purposes.
     * 
     * @return the target DN.
     */
    Name getDn() {
        return dn;
    }

    /**
     * Get the LdapOperations. Package private for testing purposes.
     * 
     * @return the LdapOperations.
     */
    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

}
