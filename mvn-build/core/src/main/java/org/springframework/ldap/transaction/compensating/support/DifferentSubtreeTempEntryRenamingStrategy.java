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
package org.springframework.ldap.transaction.compensating.support;

import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapRdn;
import org.springframework.ldap.core.LdapRdnComponent;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;

/**
 * A {@link TempEntryRenamingStrategy} that moves the entry to a different
 * subtree than the original entry. The specified subtree needs to be present in
 * the LDAP tree; it will not be created and operations using this strategy will
 * fail if the destination is not in place. However, this strategy is preferable
 * to {@link DefaultTempEntryRenamingStrategy}, as it makes searches have the
 * expected result even though the temporary entry still exists during the
 * transaction.
 * <p>
 * Example: If the specified <code>subtreeNode</code> is
 * <code>ou=tempEntries</code> and the <code>originalName</code> is
 * <code>cn=john doe, ou=company1, c=SE</code>, the result of
 * {@link #getTemporaryName(Name)} will be
 * <code>cn=john doe1, ou=tempEntries</code>. The &quot;1&quot; suffix is a
 * sequence number needed to prevent potential collisions in the temporary
 * storage.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class DifferentSubtreeTempEntryRenamingStrategy implements
        TempEntryRenamingStrategy {

    private Name subtreeNode;

    private static int nextSequenceNo = 1;

    public DifferentSubtreeTempEntryRenamingStrategy(Name subtreeNode) {
        this.subtreeNode = subtreeNode;
    }

    public Name getSubtreeNode() {
        return subtreeNode;
    }

    public void setSubtreeNode(Name subtreeNode) {
        this.subtreeNode = subtreeNode;
    }

    int getNextSequenceNo() {
        return nextSequenceNo;
    }

    /*
     * @see org.springframework.ldap.support.transaction.TempEntryRenamingStrategy#getTemporaryName(javax.naming.Name)
     */
    public Name getTemporaryName(Name originalName) {
        DistinguishedName tempName = new DistinguishedName(originalName);
        List names = tempName.getNames();
        LdapRdn rdn = (LdapRdn) names.get(names.size() - 1);
        LdapRdnComponent component = rdn.getComponent();

        LdapRdn newRdn;
        synchronized (this) {
            newRdn = new LdapRdn(component.getKey(), component.getValue()
                    + nextSequenceNo++);
        }

        DistinguishedName newName = new DistinguishedName(subtreeNode);
        newName.add(newRdn);
        return newName;
    }
}
