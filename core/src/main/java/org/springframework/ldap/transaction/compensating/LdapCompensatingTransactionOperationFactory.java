/*
 * Copyright 2005-2013 the original author or authors.
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

import javax.naming.directory.DirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.transaction.compensating.CompensatingTransactionOperationFactory;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;
import org.springframework.util.ObjectUtils;

/**
 * {@link CompensatingTransactionOperationRecorder} implementation for LDAP operations.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class LdapCompensatingTransactionOperationFactory implements CompensatingTransactionOperationFactory {

	private static Logger log = LoggerFactory.getLogger(LdapCompensatingTransactionOperationFactory.class);

	private TempEntryRenamingStrategy renamingStrategy;

	/**
	 * Constructor.
	 * @param renamingStrategy the {@link TempEntryRenamingStrategy} to supply to relevant
	 * operations.
	 */
	public LdapCompensatingTransactionOperationFactory(TempEntryRenamingStrategy renamingStrategy) {
		this.renamingStrategy = renamingStrategy;
	}

	/*
	 * @see org.springframework.transaction.compensating.
	 * CompensatingTransactionOperationFactory #createRecordingOperation(java.lang.Object,
	 * java.lang.String)
	 */
	public CompensatingTransactionOperationRecorder createRecordingOperation(Object resource, String operation) {
		if (ObjectUtils.nullSafeEquals(operation, LdapTransactionUtils.BIND_METHOD_NAME)) {
			log.debug("Bind operation recorded");
			return new BindOperationRecorder(createLdapOperationsInstance((DirContext) resource));
		}
		else if (ObjectUtils.nullSafeEquals(operation, LdapTransactionUtils.REBIND_METHOD_NAME)) {
			log.debug("Rebind operation recorded");
			return new RebindOperationRecorder(createLdapOperationsInstance((DirContext) resource),
					this.renamingStrategy);
		}
		else if (ObjectUtils.nullSafeEquals(operation, LdapTransactionUtils.RENAME_METHOD_NAME)) {
			log.debug("Rename operation recorded");
			return new RenameOperationRecorder(createLdapOperationsInstance((DirContext) resource));
		}
		else if (ObjectUtils.nullSafeEquals(operation, LdapTransactionUtils.MODIFY_ATTRIBUTES_METHOD_NAME)) {
			return new ModifyAttributesOperationRecorder(createLdapOperationsInstance((DirContext) resource));
		}
		else if (ObjectUtils.nullSafeEquals(operation, LdapTransactionUtils.UNBIND_METHOD_NAME)) {
			return new UnbindOperationRecorder(createLdapOperationsInstance((DirContext) resource),
					this.renamingStrategy);
		}

		log.warn("No suitable CompensatingTransactionOperationRecorder found for method " + operation
				+ ". Operation will not be transacted.");
		return new NullOperationRecorder();
	}

	LdapOperations createLdapOperationsInstance(DirContext ctx) {
		return new LdapTemplate(new SingleContextSource(ctx));
	}

}
