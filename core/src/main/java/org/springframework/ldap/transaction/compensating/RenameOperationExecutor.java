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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

/**
 * A {@link CompensatingTransactionOperationExecutor} to manage a rename operation.
 * Performs a rename operation in {@link #performOperation()}, a negating rename in
 * {@link #rollback()}, and nothing in {@link #commit()}.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class RenameOperationExecutor implements CompensatingTransactionOperationExecutor {

	private static Logger log = LoggerFactory.getLogger(RenameOperationExecutor.class);

	private LdapOperations ldapOperations;

	private Name newDn;

	private Name originalDn;

	/**
	 * Constructor.
	 * @param ldapOperations The {@link LdapOperations} to use for performing the rollback
	 * operation.
	 * @param originalDn DN that the entry was moved from in the recorded operation.
	 * @param newDn DN that the entry has been moved to in the recorded operation.
	 */
	public RenameOperationExecutor(LdapOperations ldapOperations, Name originalDn, Name newDn) {
		this.ldapOperations = ldapOperations;
		this.originalDn = originalDn;
		this.newDn = newDn;
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#rollback()
	 */
	public void rollback() {
		log.debug("Rolling back rename operation");
		try {
			this.ldapOperations.rename(this.newDn, this.originalDn);
		}
		catch (Exception ex) {
			log.warn("Unable to rollback rename operation. " + "originalDn: " + this.newDn + "; newDn:this. "
					+ this.originalDn);
		}
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#commit()
	 */
	public void commit() {
		log.debug("Nothing to do in commit for rename operation");
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#performOperation()
	 */
	public void performOperation() {
		log.debug("Performing rename operation");
		this.ldapOperations.rename(this.originalDn, this.newDn);
	}

	Name getNewDn() {
		return this.newDn;
	}

	LdapOperations getLdapOperations() {
		return this.ldapOperations;
	}

	Name getOriginalDn() {
		return this.originalDn;
	}

}
