/*
 * Copyright 2006-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

/**
 * A {@link CompensatingTransactionOperationExecutor} to manage a rebind operation. The
 * methods in this class do not behave as expected, since it might be impossible to
 * retrieve all the original attributes from the entry. Instead this class performs a
 * <b>rename</b> in {@link #performOperation()}, a negating rename in {@link #rollback()},
 * and the {@link #commit()} operation unbinds the original entry from its temporary
 * location and binds a new entry to the original location using the attributes supplied
 * to the original rebind opertaion.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class RebindOperationExecutor implements CompensatingTransactionOperationExecutor {

	private static Logger log = LoggerFactory.getLogger(RebindOperationExecutor.class);

	private LdapOperations ldapOperations;

	private Name originalDn;

	private Name temporaryDn;

	private Object originalObject;

	private Attributes originalAttributes;

	/**
	 * Constructor.
	 * @param ldapOperations the {@link LdapOperations} to use to perform the rollback.
	 * @param originalDn The original DN of the entry to bind.
	 * @param temporaryDn The temporary DN of the entry.
	 * @param originalObject Original 'object' parameter sent to the rebind operation.
	 * @param originalAttributes Original 'attributes' parameter sent to the rebind
	 * operation
	 */
	public RebindOperationExecutor(LdapOperations ldapOperations, Name originalDn, Name temporaryDn,
			Object originalObject, Attributes originalAttributes) {
		this.ldapOperations = ldapOperations;
		this.originalDn = originalDn;
		this.temporaryDn = temporaryDn;
		this.originalObject = originalObject;
		this.originalAttributes = originalAttributes;
	}

	/**
	 * Get the LdapOperations. Package private for testing purposes.
	 * @return the LdapOperations.
	 */
	LdapOperations getLdapOperations() {
		return this.ldapOperations;
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#rollback()
	 */
	public void rollback() {
		log.debug("Rolling back rebind operation");
		try {
			this.ldapOperations.unbind(this.originalDn);
			this.ldapOperations.rename(this.temporaryDn, this.originalDn);
		}
		catch (Exception ex) {
			log.warn(
					"Failed to rollback operation, dn: " + this.originalDn + "; temporary DN:this. " + this.temporaryDn,
					ex);
		}
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#commit()
	 */
	public void commit() {
		log.debug("Committing rebind operation");
		this.ldapOperations.unbind(this.temporaryDn);
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#performOperation()
	 */
	public void performOperation() {
		log.debug("Performing rebind operation - " + "renaming original entry and " + "binding new contents to entry.");
		this.ldapOperations.rename(this.originalDn, this.temporaryDn);
		this.ldapOperations.bind(this.originalDn, this.originalObject, this.originalAttributes);
	}

	Attributes getOriginalAttributes() {
		return this.originalAttributes;
	}

	Name getOriginalDn() {
		return this.originalDn;
	}

	Object getOriginalObject() {
		return this.originalObject;
	}

	Name getTemporaryDn() {
		return this.temporaryDn;
	}

}
