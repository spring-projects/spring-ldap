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

import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.DirContextAdapter;

/**
 * {@link CompensatingTransactionRecordingOperation} to keep track of unbind
 * operations. This class creates {@link BindRollbackOperation} objects for
 * rollback.
 * 
 * @author Mattias Arthursson
 */
public class UnbindRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    private LdapOperations ldapOperations;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            {@link LdapOperations} to use for getting the data prior to
     *            unbinding the entry and to supply to the
     *            {@link BindRollbackOperation} for rollback.
     */
    public UnbindRecordingOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRecordingOperation#recordOperation(java.lang.Object[])
     */
    public CompensatingTransactionRollbackOperation recordOperation(
            Object[] args) {
        Name dn = LdapUtils.getFirstArgumentAsName(args);
        DirContextAdapter ctx = (DirContextAdapter) ldapOperations.lookup(dn);
        return new BindRollbackOperation(ldapOperations, ctx);
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }
}
