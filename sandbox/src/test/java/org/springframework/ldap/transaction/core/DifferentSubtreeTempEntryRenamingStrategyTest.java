package org.springframework.ldap.transaction.core;

import javax.naming.Name;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.transaction.core.DifferentSubtreeTempEntryRenamingStrategy;

import junit.framework.TestCase;

public class DifferentSubtreeTempEntryRenamingStrategyTest extends TestCase {

    public void testGetTemporaryName() {
        DistinguishedName originalName = new DistinguishedName(
                "cn=john doe, ou=somecompany, c=SE");
        DifferentSubtreeTempEntryRenamingStrategy tested = new DifferentSubtreeTempEntryRenamingStrategy(
                new DistinguishedName("ou=tempEntries"));

        int nextSequenceNo = tested.getNextSequenceNo();

        // Perform test
        Name result = tested.getTemporaryName(originalName);

        // Verify result
        assertEquals("cn=john doe" + nextSequenceNo + ", ou=tempEntries",
                result.toString());
    }

}
