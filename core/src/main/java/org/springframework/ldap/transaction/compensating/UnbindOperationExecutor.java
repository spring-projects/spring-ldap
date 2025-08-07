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
 * A {@link CompensatingTransactionOperationExecutor} to manage an unbind operation. The
 * methods in this class do not behave as expected, since it might be impossible to
 * retrieve all the original attributes from the entry. Instead this class performs a
 * <b>rename</b> in {@link #performOperation()}, a negating rename in {@link #rollback()},
 * and {@link #commit()} unbinds the entry from its temporary location.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class UnbindOperationExecutor implements CompensatingTransactionOperationExecutor {

	private static Logger log = LoggerFactory.getLogger(UnbindOperationExecutor.class);

	private LdapOperations ldapOperations;

	private Name originalDn;

	private Name temporaryDn;

	/**
	 * Constructor.
	 * @param ldapOperations The {@link LdapOperations} to use for performing the rollback
	 * operation.
	 * @param originalDn The original DN of the entry to be removed.
	 * @param temporaryDn Temporary DN of the entry to be removed; this is where the entry
	 * is temporarily stored during the transaction.
	 */
	public UnbindOperationExecutor(LdapOperations ldapOperations, Name originalDn, Name temporaryDn) {
		this.ldapOperations = ldapOperations;
		this.originalDn = originalDn;
		this.temporaryDn = temporaryDn;
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#rollback()
	 */
	public void rollback() {
		try {
			this.ldapOperations.rename(this.temporaryDn, this.originalDn);
		}
		catch (Exception ex) {
			log.warn("Filed to rollback unbind operation, temporaryDn: " + this.temporaryDn + "; originalDn:this. "
					+ this.originalDn);
		}
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#commit()
	 */
	public void commit() {
		log.debug("Committing unbind operation - unbinding temporary entry");
		this.ldapOperations.unbind(this.temporaryDn);
	}

	/*
	 * @see org.springframework.ldap.support.transaction.
	 * CompensatingTransactionOperationExecutor#performOperation()
	 */
	public void performOperation() {
		log.debug("Performing operation for unbind -" + " renaming to temporary entry.");
		this.ldapOperations.rename(this.originalDn, this.temporaryDn);
	}

	LdapOperations getLdapOperations() {
		return this.ldapOperations;
	}

	Name getOriginalDn() {
		return this.originalDn;
	}

	Name getTemporaryDn() {
		return this.temporaryDn;
	}

}
