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
import org.springframework.ldap.support.DirContextOperations;

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

    private DirContextOperations dirContextOperations;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            The {@link LdapOperations} to use for performing the rollback
     *            operation.
     * @param dirContextOperations
     *            a {@link DirContextOperations} instance to be used for
     *            obtaining the DN of the affected entry and to be used when
     *            performing the rollback, binding it to the DN.
     */
    public BindRollbackOperation(LdapOperations ldapOperations,
            DirContextOperations dirContextOperations) {
        this.ldapOperations = ldapOperations;
        this.dirContextOperations = dirContextOperations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRollbackOperation#rollback()
     */
    public void rollback() {
        Name dn = dirContextOperations.getDn();
        try {
            ldapOperations.bind(dn, dirContextOperations, null);
        } catch (Exception e) {
            log.warn("Filed to rollback unbind operation, dn: " + dn);
        }
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    DirContextOperations getDirContextOperations() {
        return dirContextOperations;
    }

}
