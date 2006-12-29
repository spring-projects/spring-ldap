package org.springframework.ldap.support.transaction;

import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.springframework.ldap.LdapOperations;

public class ModifyAttributesRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    private LdapOperations ldapOperations;

    public ModifyAttributesRecordingOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    public CompensatingTransactionRollbackOperation performOperation(
            Object[] args) {
        throw new UnsupportedOperationException("Not implemented");
    }

    protected ModificationItem getCompensatingModificationItem(
            Attributes originalAttributes, ModificationItem modificationItem)
            throws NamingException {
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
            }
        }

        return null;
    }
}
