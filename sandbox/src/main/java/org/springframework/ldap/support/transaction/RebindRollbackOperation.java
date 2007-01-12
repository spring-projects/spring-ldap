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
 * A {@link CompensatingTransactionRollbackOperation} to rollback a rebind
 * operation, performing a rebind operation using the supplied
 * {@link DirContextOperations} object.
 * 
 * @author Mattias Arthursson
 */
public class RebindRollbackOperation implements
        CompensatingTransactionRollbackOperation {

    private static Log log = LogFactory.getLog(RebindRollbackOperation.class);

    private LdapOperations ldapOperations;

    private DirContextOperations dirContextOperations;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            the {@link LdapOperations} to use to perform the rollback.
     * @param dirContextOperations
     *            the {@link DirContextOperations} to use as input to the rebind
     *            operation performing the rollback.
     */
    public RebindRollbackOperation(LdapOperations ldapOperations,
            DirContextOperations dirContextOperations) {
        this.ldapOperations = ldapOperations;
        this.dirContextOperations = dirContextOperations;
    }

    /**
     * Get the targegt DirContextOperations. Package private for testing
     * purposes.
     * 
     * @return the DirContextOperations.
     */
    DirContextOperations getDirContextOperations() {
        return dirContextOperations;
    }

    /**
     * Get the LdapOperations. Package private for testing purposes.
     * 
     * @return the LdapOperations.
     */
    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#rollback()
     */
    public void rollback() {
        Name dn = dirContextOperations.getDn();
        try {
            ldapOperations.rebind(dn, dirContextOperations, null);
        } catch (Exception e) {
            log.warn("Failed to rollback operation, dn: " + dn, e);
        }
    }

}
