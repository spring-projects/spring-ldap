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

package org.springframework.ldap.transaction.compensating.support;

import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link TempEntryRenamingStrategy} that moves the entry to a different subtree than
 * the original entry. The specified subtree needs to be present in the LDAP tree; it will
 * not be created and operations using this strategy will fail if the destination is not
 * in place. However, this strategy is preferable to
 * {@link DefaultTempEntryRenamingStrategy}, as it makes searches have the expected result
 * even though the temporary entry still exists during the transaction.
 * <p>
 * Example: If the specified <code>subtreeNode</code> is <code>ou=tempEntries</code> and
 * the <code>originalName</code> is <code>cn=john doe, ou=company1, c=SE</code>, the
 * result of {@link #getTemporaryName(Name)} will be
 * <code>cn=john doe1, ou=tempEntries</code>. The &quot;1&quot; suffix is a sequence
 * number needed to prevent potential collisions in the temporary storage.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public class DifferentSubtreeTempEntryRenamingStrategy implements TempEntryRenamingStrategy {

	private Name subtreeNode;

	private static final AtomicInteger NEXT_SEQUENCE_NO = new AtomicInteger(1);

	public DifferentSubtreeTempEntryRenamingStrategy(Name subtreeNode) {
		this.subtreeNode = subtreeNode;
	}

	public DifferentSubtreeTempEntryRenamingStrategy(String subtreeNode) {
		this(LdapUtils.newLdapName(subtreeNode));
	}

	public Name getSubtreeNode() {
		return this.subtreeNode;
	}

	public void setSubtreeNode(Name subtreeNode) {
		this.subtreeNode = subtreeNode;
	}

	int getNextSequenceNo() {
		return NEXT_SEQUENCE_NO.get();
	}

	/*
	 * @see org.springframework.ldap.support.transaction.TempEntryRenamingStrategy#
	 * getTemporaryName(javax.naming.Name)
	 */
	public Name getTemporaryName(Name originalName) {
		int thisSequenceNo = NEXT_SEQUENCE_NO.getAndIncrement();

		LdapName tempName = LdapUtils.newLdapName(originalName);
		try {
			String leafNode = tempName.get(tempName.size() - 1) + thisSequenceNo;
			LdapName newName = LdapUtils.newLdapName(this.subtreeNode);
			newName.add(leafNode);

			return newName;
		}
		catch (InvalidNameException e) {
			throw new org.springframework.ldap.InvalidNameException(e);
		}
	}

}
