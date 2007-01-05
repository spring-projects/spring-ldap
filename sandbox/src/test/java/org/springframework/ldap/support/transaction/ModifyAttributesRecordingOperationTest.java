package org.springframework.ldap.support.transaction;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.easymock.MockControl;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.DistinguishedName;

import junit.framework.TestCase;

public class ModifyAttributesRecordingOperationTest extends TestCase {
    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    private MockControl attributesMapperControl;

    private AttributesMapper attributesMapperMock;

    private ModifyAttributesRecordingOperation tested;

    protected void setUp() throws Exception {
        ldapOperationsControl = MockControl.createControl(LdapOperations.class);
        ldapOperationsMock = (LdapOperations) ldapOperationsControl.getMock();

        attributesMapperControl = MockControl
                .createControl(AttributesMapper.class);
        attributesMapperMock = (AttributesMapper) attributesMapperControl
                .getMock();

        tested = new ModifyAttributesRecordingOperation(ldapOperationsMock);
    }

    protected void tearDown() throws Exception {
        ldapOperationsControl = null;
        ldapOperationsMock = null;

        attributesMapperControl = null;
        attributesMapperMock = null;

        tested = null;
    }

    protected void replay() {
        ldapOperationsControl.replay();
        attributesMapperControl.replay();
    }

    protected void verify() {
        ldapOperationsControl.verify();
        attributesMapperControl.verify();
    }

    public void testPerformOperation() {
        final ModificationItem incomingItem = new ModificationItem(
                DirContext.ADD_ATTRIBUTE, new BasicAttribute("attribute1"));
        ModificationItem[] incomingMods = new ModificationItem[] { incomingItem };
        final ModificationItem compensatingItem = new ModificationItem(
                DirContext.ADD_ATTRIBUTE, new BasicAttribute("attribute2"));

        final Attributes expectedAttributes = new BasicAttributes();

        tested = new ModifyAttributesRecordingOperation(ldapOperationsMock) {
            AttributesMapper getAttributesMapper() {
                return attributesMapperMock;
            }

            protected ModificationItem getCompensatingModificationItem(
                    Attributes originalAttributes,
                    ModificationItem modificationItem) {
                assertSame(expectedAttributes, originalAttributes);
                assertSame(incomingItem, modificationItem);
                return compensatingItem;
            }
        };

        DistinguishedName expectedName = new DistinguishedName("cn=john doe");
        ldapOperationsControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
        ldapOperationsControl.expectAndReturn(ldapOperationsMock.lookup(
                expectedName, new String[] { "attribute1" },
                attributesMapperMock), expectedAttributes);

        replay();
        // Perform test
        CompensatingTransactionRollbackOperation operation = tested
                .performOperation(new Object[] { expectedName, incomingMods });
        verify();

        // Verify outcome
        assertTrue(operation instanceof ModifyAttributesRollbackOperation);
        ModifyAttributesRollbackOperation rollbackOperation = (ModifyAttributesRollbackOperation) operation;
        assertSame(expectedName, rollbackOperation.getDn());
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertEquals(1, rollbackOperation.getModificationItems().length);
        assertSame(compensatingItem,
                rollbackOperation.getModificationItems()[0]);
    }

    public void testGetCompensatingModificationItem_RemoveFullExistingAttribute()
            throws NamingException {
        BasicAttribute attribute = new BasicAttribute("someattr");
        attribute.add("value1");
        attribute.add("value2");
        Attributes attributes = new BasicAttributes();
        attributes.put(attribute);

        ModificationItem originalItem = new ModificationItem(
                DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("someattr"));

        // Perform test
        ModificationItem result = tested.getCompensatingModificationItem(
                attributes, originalItem);

        // Verify result
        assertEquals(DirContext.ADD_ATTRIBUTE, result.getModificationOp());
        Attribute resultAttribute = result.getAttribute();
        assertEquals("someattr", resultAttribute.getID());
        Object object = resultAttribute.get(0);
        assertEquals("value1", object);
        assertEquals("value2", resultAttribute.get(1));
    }

    public void testGetCompensatingModificationItem_RemoveTwoAttributeValues()
            throws NamingException {
        BasicAttribute attribute = new BasicAttribute("someattr");
        attribute.add("value1");
        attribute.add("value2");
        attribute.add("value3");
        Attributes attributes = new BasicAttributes();
        attributes.put(attribute);

        BasicAttribute modificationAttribute = new BasicAttribute("someattr");
        modificationAttribute.add("value1");
        modificationAttribute.add("value2");
        ModificationItem originalItem = new ModificationItem(
                DirContext.REMOVE_ATTRIBUTE, modificationAttribute);

        // Perform test
        ModificationItem result = tested.getCompensatingModificationItem(
                attributes, originalItem);

        // Verify result
        assertEquals(DirContext.ADD_ATTRIBUTE, result.getModificationOp());
        Attribute resultAttribute = result.getAttribute();
        assertEquals("someattr", resultAttribute.getID());
        Object object = resultAttribute.get(0);
        assertEquals("value1", object);
        assertEquals("value2", resultAttribute.get(1));
    }

    public void testGetCompensatingModificationItem_ReplaceExistingAttribute()
            throws NamingException {
        BasicAttribute attribute = new BasicAttribute("someattr");
        attribute.add("value1");
        attribute.add("value2");
        Attributes attributes = new BasicAttributes();
        attributes.put(attribute);

        BasicAttribute modificationAttribute = new BasicAttribute("someattr");
        modificationAttribute.add("newvalue1");
        modificationAttribute.add("newvalue2");
        ModificationItem originalItem = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("someattr"));

        // Perform test
        ModificationItem result = tested.getCompensatingModificationItem(
                attributes, originalItem);

        // Verify result
        assertEquals(DirContext.REPLACE_ATTRIBUTE, result.getModificationOp());
        Attribute resultAttribute = result.getAttribute();
        assertEquals("someattr", resultAttribute.getID());
        Object object = resultAttribute.get(0);
        assertEquals("value1", object);
        assertEquals("value2", resultAttribute.get(1));
    }

    public void testGetCompensatingModificationItem_ReplaceNonExistingAttribute()
            throws NamingException {
        Attributes attributes = new BasicAttributes();

        BasicAttribute modificationAttribute = new BasicAttribute("someattr");
        modificationAttribute.add("newvalue1");
        modificationAttribute.add("newvalue2");
        ModificationItem originalItem = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, modificationAttribute);

        // Perform test
        ModificationItem result = tested.getCompensatingModificationItem(
                attributes, originalItem);

        // Verify result
        assertEquals(DirContext.REMOVE_ATTRIBUTE, result.getModificationOp());
        Attribute resultAttribute = result.getAttribute();
        assertEquals("someattr", resultAttribute.getID());
        assertEquals(0, resultAttribute.size());
    }

    public void testGetCompensatingModificationItem_AddNonExistingAttribute()
            throws NamingException {
        Attributes attributes = new BasicAttributes();

        BasicAttribute modificationAttribute = new BasicAttribute("someattr");
        modificationAttribute.add("newvalue1");
        modificationAttribute.add("newvalue2");
        ModificationItem originalItem = new ModificationItem(
                DirContext.ADD_ATTRIBUTE, modificationAttribute);

        // Perform test
        ModificationItem result = tested.getCompensatingModificationItem(
                attributes, originalItem);

        // Verify result
        assertEquals(DirContext.REMOVE_ATTRIBUTE, result.getModificationOp());
        Attribute resultAttribute = result.getAttribute();
        assertEquals("someattr", resultAttribute.getID());
        assertEquals(0, resultAttribute.size());
    }

    public void testGetCompensatingModificationItem_AddExistingAttribute()
            throws NamingException {
        BasicAttribute attribute = new BasicAttribute("someattr");
        attribute.add("value1");
        attribute.add("value2");
        Attributes attributes = new BasicAttributes();
        attributes.put(attribute);

        BasicAttribute modificationAttribute = new BasicAttribute("someattr");
        modificationAttribute.add("newvalue1");
        modificationAttribute.add("newvalue2");
        ModificationItem originalItem = new ModificationItem(
                DirContext.ADD_ATTRIBUTE, new BasicAttribute("someattr"));

        // Perform test
        ModificationItem result = tested.getCompensatingModificationItem(
                attributes, originalItem);

        // Verify result
        assertEquals(DirContext.REPLACE_ATTRIBUTE, result.getModificationOp());
        Attribute resultAttribute = result.getAttribute();
        assertEquals("someattr", resultAttribute.getID());
        assertEquals("value1", result.getAttribute().get(0));
        assertEquals("value2", result.getAttribute().get(1));
    }
}
