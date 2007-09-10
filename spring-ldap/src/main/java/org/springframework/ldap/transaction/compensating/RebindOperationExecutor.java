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
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

/**
 * A {@link CompensatingTransactionOperationExecutor} to manage a rebind
 * operation. The methods in this class do not behave as expected, since it
 * might be impossible to retrieve all the original attributes from the entry.
 * Instead this class performs a <b>rename</b> in {@link #performOperation()},
 * a negating rename in {@link #rollback()}, and the {@link #commit()}
 * operation unbinds the original entry from its temporary location and binds a
 * new entry to the original location using the attributes supplied to the
 * original rebind opertaion.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class RebindOperationExecutor implements
        CompensatingTransactionOperationExecutor {

    private static Log log = LogFactory.getLog(RebindOperationExecutor.class);

    private LdapOperations ldapOperations;

    private Name originalDn;

    private Name temporaryDn;

    private Object originalObject;

    private Attributes originalAttributes;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            the {@link LdapOperations} to use to perform the rollback.
     * @param originalDn
     *            The original DN of the entry to bind.
     * @param temporaryDn
     *            The temporary DN of the entry.
     * @param originalObject
     *            Original 'object' parameter sent to the rebind operation.
     * @param originalAttributes
     *            Original 'attributes' parameter sent to the rebind operation
     */
    public RebindOperationExecutor(LdapOperations ldapOperations,
            Name originalDn, Name temporaryDn, Object originalObject,
            Attributes originalAttributes) {
        this.ldapOperations = ldapOperations;
        this.originalDn = originalDn;
        this.temporaryDn = temporaryDn;
        this.originalObject = originalObject;
        this.originalAttributes = originalAttributes;
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
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#rollback()
     */
    public void rollback() {
        log.debug("Rolling back rebind operation");
        try {
            ldapOperations.unbind(originalDn);
            ldapOperations.rename(temporaryDn, originalDn);
        } catch (Exception e) {
            log.warn("Failed to rollback operation, dn: " + originalDn
                    + "; temporary DN: " + temporaryDn, e);
        }
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#commit()
     */
    public void commit() {
        log.debug("Committing rebind operation");
        ldapOperations.unbind(temporaryDn);
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#performOperation()
     */
    public void performOperation() {
        log.debug("Performing rebind operation - "
                + "renaming original entry and "
                + "binding new contents to entry.");
        ldapOperations.rename(originalDn, temporaryDn);
        ldapOperations.bind(originalDn, originalObject, originalAttributes);
    }

    Attributes getOriginalAttributes() {
        return originalAttributes;
    }

    Name getOriginalDn() {
        return originalDn;
    }

    Object getOriginalObject() {
        return originalObject;
    }

    Name getTemporaryDn() {
        return temporaryDn;
    }
}
