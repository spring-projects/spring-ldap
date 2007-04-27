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
 * A {@link CompensatingTransactionOperationExecutor} to manage a bind
 * operation. Performs a bind in {@link #performOperation()}, a corresponding
 * unbind in {@link #rollback()}, and nothing in {@link #commit()}.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class BindOperationExecutor implements
        CompensatingTransactionOperationExecutor {
    private static Log log = LogFactory.getLog(BindOperationExecutor.class);

    private LdapOperations ldapOperations;

    private Name dn;

    private Object originalObject;

    private Attributes originalAttributes;

    /**
     * Constructor.
     * 
     * @param ldapOperations
     *            {@link LdapOperations} to use for performing the rollback
     *            operation.
     * @param dn
     *            DN of the entry to be unbound.
     * @param originalObject
     *            original value sent to the 'object' parameter of the bind
     *            operation.
     * @param originalAttributes
     *            original value sent to the 'attributes' parameter of the bind
     *            operation.
     */
    public BindOperationExecutor(LdapOperations ldapOperations, Name dn,
            Object originalObject, Attributes originalAttributes) {
        this.ldapOperations = ldapOperations;
        this.dn = dn;
        this.originalObject = originalObject;
        this.originalAttributes = originalAttributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#rollback()
     */
    public void rollback() {
        try {
            ldapOperations.unbind(dn);
        } catch (Exception e) {
            log.warn("Failed to rollback, dn:" + dn.toString(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#commit()
     */
    public void commit() {
        log.debug("Nothing to do in commit for bind operation");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationExecutor#performOperation()
     */
    public void performOperation() {
        log.debug("Performing bind operation");
        ldapOperations.bind(dn, originalObject, originalAttributes);
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

    Attributes getOriginalAttributes() {
        return originalAttributes;
    }

    Object getOriginalObject() {
        return originalObject;
    }

}
