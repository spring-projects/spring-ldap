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

package org.springframework.ldap.transaction.compensating.support;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;

/**
 * Default implementation of {@link TempEntryRenamingStrategy}. This implementation simply
 * adds "_temp" to the leftmost (least significant part) of the name. For example:
 *
 * <pre>
 * cn=john doe, ou=company1, c=SE
 * </pre>
 *
 * becomes:
 *
 * <pre>
 * cn=john doe_temp, ou=company1, c=SE
 * </pre>
 * <p>
 * Note that using this strategy means that the entry remains in virtually the same
 * location as where it originally resided. This means that searches later in the same
 * transaction might return references to the temporary entry even though it should have
 * been removed or rebound.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class DefaultTempEntryRenamingStrategy implements TempEntryRenamingStrategy {

	/**
	 * The default temp entry suffix, &quot;_temp&quot;.
	 */
	public static final String DEFAULT_TEMP_SUFFIX = "_temp";

	private String tempSuffix = DEFAULT_TEMP_SUFFIX;

	/*
	 * @see org.springframework.ldap.support.transaction.TempEntryRenamingStrategy#
	 * getTemporaryName(javax.naming.Name)
	 */
	public Name getTemporaryName(Name originalName) {
		LdapName temporaryName = LdapUtils.newLdapName(originalName);

		// Add tempSuffix to the leaf node name.
		try {
			String leafNode = (String) temporaryName.remove(temporaryName.size() - 1);
			temporaryName.add(new Rdn(leafNode + this.tempSuffix));
		}
		catch (InvalidNameException ex) {
			throw new org.springframework.ldap.InvalidNameException(ex);
		}

		return temporaryName;
	}

	/**
	 * Get the suffix that will be used for renaming temporary entries.
	 * @return the suffix.
	 */
	public String getTempSuffix() {
		return this.tempSuffix;
	}

	/**
	 * Set the suffix to use for renaming temporary entries. Default value is
	 * {@link #DEFAULT_TEMP_SUFFIX}.
	 * @param tempSuffix the suffix.
	 */
	public void setTempSuffix(String tempSuffix) {
		this.tempSuffix = tempSuffix;
	}

}
