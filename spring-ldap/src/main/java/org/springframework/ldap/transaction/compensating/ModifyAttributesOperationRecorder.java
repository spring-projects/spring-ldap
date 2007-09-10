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

import java.util.HashSet;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;
import org.springframework.util.Assert;

/**
 * A {@link CompensatingTransactionOperationRecorder} keeping track of
 * modifyAttributes operations, creating corresponding
 * {@link ModifyAttributesOperationExecutor} instances for rollback.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class ModifyAttributesOperationRecorder implements
        CompensatingTransactionOperationRecorder {

    private LdapOperations ldapOperations;

    public ModifyAttributesOperationRecorder(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    /*
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionOperationRecorder#recordOperation(java.lang.Object[])
     */
    public CompensatingTransactionOperationExecutor recordOperation(
            Object[] args) {
        Assert.notNull(args);
        Name dn = LdapTransactionUtils.getFirstArgumentAsName(args);
        if (args.length != 2 || !(args[1] instanceof ModificationItem[])) {
            throw new IllegalArgumentException(
                    "Unexpected arguments to ModifyAttributes operation");
        }

        ModificationItem[] incomingModifications = (ModificationItem[]) args[1];

        Set set = new HashSet();
        for (int i = 0; i < incomingModifications.length; i++) {
            set.add(incomingModifications[i].getAttribute().getID());
        }

        // Get the current values of all referred Attributes.
        String[] attributeNameArray = (String[]) set.toArray(new String[set
                .size()]);
        Attributes currentAttributes = (Attributes) ldapOperations.lookup(dn,
                attributeNameArray, getAttributesMapper());

        // Get a compensating ModificationItem for each of the incoming
        // modification.
        ModificationItem[] rollbackItems = new ModificationItem[incomingModifications.length];
        for (int i = 0; i < incomingModifications.length; i++) {
            rollbackItems[i] = getCompensatingModificationItem(
                    currentAttributes, incomingModifications[i]);
        }

        return new ModifyAttributesOperationExecutor(ldapOperations, dn,
                incomingModifications, rollbackItems);
    }

    /**
     * Get an {@link AttributesMapper} that just returns the supplied
     * Attributes.
     * 
     * @return the {@link AttributesMapper} to use for getting the current
     *         Attributes of the target DN.
     */
    AttributesMapper getAttributesMapper() {
        return new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                return attributes;
            }
        };
    }

    /**
     * Get a ModificationItem to use for rollback of the supplied modification.
     * 
     * @param originalAttributes
     *            All Attributes of the target DN that are affected of any of
     *            the ModificationItems.
     * @param modificationItem
     *            the ModificationItem to create a rollback item for.
     * @return A ModificationItem to use for rollback of the supplied
     *         ModificationItem.
     */
    protected ModificationItem getCompensatingModificationItem(
            Attributes originalAttributes, ModificationItem modificationItem) {
        Attribute modificationAttribute = modificationItem.getAttribute();
        Attribute originalAttribute = originalAttributes
                .get(modificationAttribute.getID());

        if (modificationItem.getModificationOp() == DirContext.REMOVE_ATTRIBUTE) {
            if (modificationAttribute.size() == 0) {
                // If the modification attribute size it means that the
                // Attribute should be removed entirely - we should store a
                // ModificationItem to restore all present values for rollback.
                return new ModificationItem(DirContext.ADD_ATTRIBUTE,
                        (Attribute) originalAttribute.clone());
            } else {
                // The rollback modification will be to re-add the removed
                // attribute values.
                return new ModificationItem(DirContext.ADD_ATTRIBUTE,
                        (Attribute) modificationAttribute.clone());
            }
        } else if (modificationItem.getModificationOp() == DirContext.REPLACE_ATTRIBUTE) {
            if (originalAttribute != null) {
                return new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                        (Attribute) originalAttribute.clone());
            } else {
                // The attribute doesn't previously exist - the rollback
                // operation will be to remove the attribute.
                return new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(modificationAttribute.getID()));
            }
        } else {
            // An ADD_ATTRIBUTE operation
            if (originalAttribute == null) {
                // The attribute doesn't previously exist - the rollback
                // operation will be to remove the attribute.
                return new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(modificationAttribute.getID()));
            } else {
                // The attribute does exist before - we should store the
                // previous value and it should be used for replacing in
                // rollback.
                return new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                        (Attribute) originalAttribute.clone());
            }
        }
    }

    LdapOperations getLdapOperations() {
        return ldapOperations;
    }
}
