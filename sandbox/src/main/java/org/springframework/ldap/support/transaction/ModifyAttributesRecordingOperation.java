package org.springframework.ldap.support.transaction;

import java.util.HashSet;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.springframework.ldap.AttributesMapper;
import org.springframework.ldap.LdapOperations;
import org.springframework.util.Assert;

public class ModifyAttributesRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    private LdapOperations ldapOperations;

    public ModifyAttributesRecordingOperation(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    public CompensatingTransactionRollbackOperation performOperation(
            Object[] args) {
        Assert.notNull(args);
        Name dn = LdapUtils.getFirstArgumentAsName(args);
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

        return new ModifyAttributesRollbackOperation(ldapOperations, dn,
                rollbackItems);
    }

    AttributesMapper getAttributesMapper() {
        return new AttributesMapper() {
            public Object mapFromAttributes(Attributes attributes)
                    throws NamingException {
                return attributes;
            }
        };
    }

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
