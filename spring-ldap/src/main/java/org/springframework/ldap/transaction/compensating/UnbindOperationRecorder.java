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

import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;

/**
 * {@link CompensatingTransactionOperationRecorder} to keep track of unbind
 * operations. This class creates {@link UnbindOperationExecutor} objects for
 * rollback.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class UnbindOperationRecorder implements
        CompensatingTransactionOperationRecorder {

    private LdapOperations ldapOperations;

    private TempEntryRenamingStrategy renamingStrategy;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            {@link LdapOperations} to use for getting the data prior to
     *            unbinding the entry and to supply to the
     *            {@link UnbindOperationExecutor} for rollback.
     * @param renamingStrategy
     *            the {@link TempEntryRenamingStrategy} to use when generating
     *            DNs for temporary entries.
     */
    public UnbindOperationRecorder(LdapOperations ldapOperations,
            TempEntryRenamingStrategy renamingStrategy) {
        this.ldapOperations = ldapOperations;
        this.renamingStrategy = renamingStrategy;
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationRecorder#recordOperation(java.lang.Object[])
     */
    public CompensatingTransactionOperationExecutor recordOperation(
            Object[] args) {
        Name dn = LdapTransactionUtils.getFirstArgumentAsName(args);
        Name temporaryDn = renamingStrategy.getTemporaryName(dn);

        return new UnbindOperationExecutor(ldapOperations, dn, temporaryDn);
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }

    public TempEntryRenamingStrategy getRenamingStrategy() {
        return renamingStrategy;
    }
}
