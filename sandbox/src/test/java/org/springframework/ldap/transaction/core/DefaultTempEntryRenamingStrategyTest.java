package org.springframework.ldap.transaction.core;

import javax.naming.Name;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.transaction.core.DefaultTempEntryRenamingStrategy;

import junit.framework.TestCase;

public class DefaultTempEntryRenamingStrategyTest extends TestCase {

    public void testGetTemporaryName() {
        DistinguishedName expectedOriginalName = new DistinguishedName(
                "cn=john doe, ou=somecompany, c=SE");
        DefaultTempEntryRenamingStrategy tested = new DefaultTempEntryRenamingStrategy();

        Name result = tested.getTemporaryName(expectedOriginalName);
        assertEquals("cn=john doe_temp, ou=somecompany, c=SE", result
                .toString());
        assertNotSame(expectedOriginalName, result);
    }

    public void testGetTemporaryDN_MultivalueDN() {
        DistinguishedName expectedOriginalName = new DistinguishedName(
                "cn=john doe+sn=doe, ou=somecompany, c=SE");
        DefaultTempEntryRenamingStrategy tested = new DefaultTempEntryRenamingStrategy();

        Name result = tested.getTemporaryName(expectedOriginalName);
        assertEquals("cn=john doe_temp+sn=doe, ou=somecompany, c=SE", result
                .toString());
    }


}
