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
import org.springframework.ldap.support.DirContextAdapter;

/**
 * A {@link CompensatingTransactionRecordingOperation} keeping track of a rebind
 * operation. Creates {@link RebindRollbackOperation} objects in
 * {@link #performOperation(Object[])}.
 * 
 * @author Mattias Arthursson
 */
public class RebindRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    private static Log log = LogFactory.getLog(RebindRecordingOperation.class);

    private LdapOperations ldapOperations;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            {@link LdapOperations} to use for getting the rollback
     *            information and supply to the {@link RebindRollbackOperation}.
     */
    public RebindRecordingOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRecordingOperation#performOperation(java.lang.Object[])
     */
    public CompensatingTransactionRollbackOperation performOperation(
            Object[] args) {
        Name dn = LdapUtils.getFirstArgumentAsName(args);

        try {
            DirContextAdapter ctx = (DirContextAdapter) ldapOperations
                    .lookup(dn);
            return new RebindRollbackOperation(ldapOperations, ctx);
        } catch (Exception e) {
            log.warn(
                    "Failed to create rollback operation, dn " + dn.toString(),
                    e);
            return new NullRollbackOperation();
        }
    }

    /**
     * Get the LdapOperations. For testing purposes.
     * 
     * @return the LdapOperations.
     */
    LdapOperations getLdapOperations() {
        return ldapOperations;
    }
}
