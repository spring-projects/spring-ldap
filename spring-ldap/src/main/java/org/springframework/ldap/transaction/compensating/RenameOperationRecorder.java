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
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;
import org.springframework.util.Assert;

/**
 * A {@link CompensatingTransactionOperationRecorder} for keeping track of
 * rename operations. Creates {@link RenameOperationExecutor} objects for
 * rolling back.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class RenameOperationRecorder implements
        CompensatingTransactionOperationRecorder {

    private static Log log = LogFactory.getLog(RenameOperationRecorder.class);

    private LdapOperations ldapOperations;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            The {@link LdapOperations} to supply to the created
     *            {@link RebindOperationExecutor} objects.
     */
    public RenameOperationRecorder(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationRecorder#recordOperation(java.lang.Object[])
     */
    public CompensatingTransactionOperationExecutor recordOperation(
            Object[] args) {
        log.debug("Storing rollback information for rename operation");
        Assert.notEmpty(args);
        if (args.length != 2) {
            // This really shouldn't happen.
            throw new IllegalArgumentException("Illegal argument length");
        }
        Name oldDn = LdapTransactionUtils.getArgumentAsName(args[0]);
        Name newDn = LdapTransactionUtils.getArgumentAsName(args[1]);
        return new RenameOperationExecutor(ldapOperations, oldDn, newDn);
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }
}
