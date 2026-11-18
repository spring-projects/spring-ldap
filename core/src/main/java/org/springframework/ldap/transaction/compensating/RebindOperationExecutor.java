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

import org.jspecify.annotations.Nullable;
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
 * <p>
 * Instances are not thread-safe. Like the rest of the compensating transaction support,
 * an instance is confined to the thread that owns the transaction, from
 * {@link #performOperation()} until {@link #commit()} or {@link #rollback()}.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class RebindOperationExecutor implements CompensatingTransactionOperationExecutor {

	private static Logger log = LoggerFactory.getLogger(RebindOperationExecutor.class);

	private LdapOperations ldapOperations;

	private Name originalDn;

	private Name temporaryDn;

	private @Nullable Object originalObject;

	private @Nullable Attributes originalAttributes;

	private boolean renamed;

	private boolean bound;

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
			@Nullable Object originalObject, @Nullable Attributes originalAttributes) {
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
			// Only steps that actually completed are compensated, in reverse order
			maybeRollbackBind();
			maybeRollbackRename();
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
		performRename();
		try {
			performBind();
		}
		catch (RuntimeException ex) {
			// This executor is only recorded for rollback if this method returns
			// normally, so restore the original entry here. The temporary DN is only
			// renamed back (never unbound), and only because this executor renamed the
			// entry there; the rename itself fails if something occupies the original DN.
			try {
				maybeRollbackRename();
			}
			catch (Exception rollbackEx) {
				log.warn("Failed to restore entry after failed rebind, dn: " + this.originalDn + "; temporary DN: "
						+ this.temporaryDn, rollbackEx);
			}
			throw ex;
		}
	}

	// Each flag is read and written only by its perform/rollback pair below, so that the
	// flags and the operations they describe cannot get out of sync.

	private void performRename() {
		this.ldapOperations.rename(this.originalDn, this.temporaryDn);
		this.renamed = true;
	}

	private void performBind() {
		this.ldapOperations.bind(this.originalDn, this.originalObject, this.originalAttributes);
		this.bound = true;
	}

	private void maybeRollbackRename() {
		if (this.renamed) {
			this.ldapOperations.rename(this.temporaryDn, this.originalDn);
			this.renamed = false;
		}
	}

	private void maybeRollbackBind() {
		if (this.bound) {
			this.ldapOperations.unbind(this.originalDn);
			this.bound = false;
		}
	}

	@Nullable Attributes getOriginalAttributes() {
		return this.originalAttributes;
	}

	Name getOriginalDn() {
		return this.originalDn;
	}

	@Nullable Object getOriginalObject() {
		return this.originalObject;
	}

	Name getTemporaryDn() {
		return this.temporaryDn;
	}

}
