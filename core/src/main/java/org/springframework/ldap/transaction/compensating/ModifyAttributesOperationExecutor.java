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
import javax.naming.directory.ModificationItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

/**
 * A {@link CompensatingTransactionOperationExecutor} to manage a
 * <code>modifyAttributes</code> operation. Performs a <code>modifyAttributes</code> in
 * {@link #performOperation()}, a negating modifyAttributes in {@link #rollback()}, and
 * nothing in {@link #commit()}.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class ModifyAttributesOperationExecutor implements CompensatingTransactionOperationExecutor {

	private static Logger log = LoggerFactory.getLogger(ModifyAttributesOperationExecutor.class);

	private LdapOperations ldapOperations;

	private Name dn;

	private ModificationItem[] compensatingModifications;

	private ModificationItem[] actualModifications;

	/**
	 * Constructor.
	 * @param ldapOperations The {@link LdapOperations} to use to perform the rollback
	 * operation.
	 * @param dn the DN of the target entry.
	 * @param actualModifications the actual modificationItems that were sent to the
	 * modifyAttributes operation.
	 * @param compensatingModifications the ModificationItems to undo the recorded
	 * operation.
	 */
	public ModifyAttributesOperationExecutor(LdapOperations ldapOperations, Name dn,
			ModificationItem[] actualModifications, ModificationItem[] compensatingModifications) {
		this.ldapOperations = ldapOperations;
		this.dn = dn;
		this.actualModifications = actualModifications.clone();
		this.compensatingModifications = compensatingModifications.clone();
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#rollback()
	 */
	public void rollback() {
		try {
			log.debug("Rolling back modifyAttributes operation");
			this.ldapOperations.modifyAttributes(this.dn, this.compensatingModifications);
		}
		catch (Exception ex) {
			log.warn("Failed to rollback ModifyAttributes operation, dn: " + this.dn);
		}
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#commit()
	 */
	public void commit() {
		log.debug("Nothing to do in commit for modifyAttributes");
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#performOperation()
	 */
	public void performOperation() {
		log.debug("Performing modifyAttributes operation");
		this.ldapOperations.modifyAttributes(this.dn, this.actualModifications);
	}

	Name getDn() {
		return this.dn;
	}

	LdapOperations getLdapOperations() {
		return this.ldapOperations;
	}

	ModificationItem[] getActualModifications() {
		return this.actualModifications;
	}

	ModificationItem[] getCompensatingModifications() {
		return this.compensatingModifications;
	}

}
