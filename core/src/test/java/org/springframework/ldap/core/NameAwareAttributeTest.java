package org.springframework.ldap.core;

import org.junit.Test;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mattias Hellborg Arthursson
 */
public class NameAwareAttributeTest {
    @Test
    public void testEqualsWithIdNotSame() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        NameAwareAttribute attr2 = new NameAwareAttribute("someOtherAttribute");

        assertFalse(attr1.equals(attr2));
    }

    @Test
    public void testEqualsWithSameIdNoValues() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");

        assertTrue(attr1.equals(attr2));
        assertEquals(attr1.hashCode(), attr2.hashCode());
    }

    @Test
    public void testEqualsUnorderedWithIdenticalAttributes() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add("value1");
        attr1.add("value2");
        attr1.add("value3");
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add("value1");
        attr2.add("value2");
        attr2.add("value3");

        assertTrue(attr1.equals(attr2));
        assertEquals(attr1.hashCode(), attr2.hashCode());
    }

    @Test
    public void testEqualsUnorderedWithIdenticalArrayAttributes() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(new byte[]{1, 2, 3});
        attr1.add(new byte[]{3, 2, 1});
        attr1.add(new byte[]{1});
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(new byte[]{1, 2, 3});
        attr2.add(new byte[]{3, 2, 1});
        attr2.add(new byte[]{1});

        assertTrue(attr1.equals(attr2));
        assertEquals(attr1.hashCode(), attr2.hashCode());
    }

    @Test
    public void testEqualsUnorderedWithDifferentOrderArrayAttributes() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(new byte[]{1, 2, 3});
        attr1.add(new byte[]{3, 2, 1});
        attr1.add(new byte[]{1});
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(new byte[]{3, 2, 1});
        attr2.add(new byte[]{1});
        attr2.add(new byte[]{1, 2, 3});

        assertTrue(attr1.equals(attr2));
        assertEquals(attr1.hashCode(), attr2.hashCode());
    }

    @Test
    public void testEqualsUnorderedWithDifferentArrayAttributes() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(new byte[]{1, 2, 3});
        attr1.add(new byte[]{3, 2, 2});
        attr1.add(new byte[]{1});
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(new byte[]{1, 2, 3});
        attr2.add(new byte[]{3, 2, 1});
        attr2.add(new byte[]{1});

        assertFalse(attr1.equals(attr2));
    }

    @Test
    public void testEqualsUnorderedWithDifferentNumberOfArrayAttributes() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(new byte[]{1, 2, 3});
        attr1.add(new byte[]{3, 2, 1});
        attr1.add(new byte[]{1});
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(new byte[]{1, 2, 3});
        attr2.add(new byte[]{1});

        assertFalse(attr1.equals(attr2));
    }

    @Test
    public void testEqualsOrderedWithIdenticalArrayAttributes() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute", true);
        attr1.add(new byte[]{1, 2, 3});
        attr1.add(new byte[]{3, 2, 1});
        attr1.add(new byte[]{1});
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute", true);
        attr2.add(new byte[]{1, 2, 3});
        attr2.add(new byte[]{3, 2, 1});
        attr2.add(new byte[]{1});

        assertTrue(attr1.equals(attr2));
        assertEquals(attr1.hashCode(), attr2.hashCode());
    }

    @Test
    public void testEqualsOrderedWithArrayAttributes() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute", true);
        attr1.add(new byte[]{1, 2, 3});
        attr1.add(new byte[]{3, 2, 1});
        attr1.add(new byte[]{1});
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute", true);
        attr2.add(new byte[]{1, 2, 3});
        attr2.add(new byte[]{3, 2, 1});
        attr2.add(new byte[]{1});

        assertTrue(attr1.equals(attr2));
        assertEquals(attr1.hashCode(), attr2.hashCode());
    }

    @Test
    public void testEqualsOrderedWithDifferentOrderArrayAttributes() {
        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute", true);
        attr1.add(new byte[]{1, 2, 3});
        attr1.add(new byte[]{3, 2, 1});
        attr1.add(new byte[]{1});
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute", true);
        attr2.add(new byte[]{3, 2, 1});
        attr2.add(new byte[]{1});
        attr2.add(new byte[]{1, 2, 3});

        assertFalse(attr1.equals(attr2));
    }

    @Test
    public void testSameDistinguishedNameValue() throws NamingException {
        String expectedName = "cn=John Doe,ou=People";

        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(LdapUtils.newLdapName(expectedName));
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(LdapUtils.newLdapName(expectedName));

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertEquals(expectedName, attr1.get());
        assertEquals(expectedName, attr2.get());
    }

    @Test
    public void testEqualDistinguishedNameValue() throws NamingException {
        // The names here are syntactically equal, but differ in exact string representation
        String expectedName1 = "cn=John Doe, OU=People";
        String expectedName2 = "cn=John Doe,ou=People";

        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(LdapUtils.newLdapName(expectedName1));
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(LdapUtils.newLdapName(expectedName2));

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertEquals(expectedName1, attr1.get());
        assertEquals(expectedName2, attr2.get());
    }

    @Test
    public void testEqualDistinguishedNameValueUninitialized() throws NamingException {
        // The names here are syntactically equal, but differ in exact string representation
        String expectedName1 = "cn=John Doe, OU=People";
        String expectedName2 = "cn=John Doe,ou=People";

        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(expectedName1);
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(LdapUtils.newLdapName(expectedName2));

        assertFalse(attr1.equals(attr2));
        assertEquals(expectedName1, attr1.get());
        assertEquals(expectedName2, attr2.get());
    }

    @Test
    public void testEqualDistinguishedNameValueManuallyInitialized() throws NamingException {
        // The names here are syntactically equal, but differ in exact string representation
        String expectedName1 = "cn=John Doe, OU=People";
        String expectedName2 = "cn=John Doe,ou=People";

        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(expectedName1);
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(LdapUtils.newLdapName(expectedName2));

        attr1.initValuesAsNames();

        assertTrue(attr1.equals(attr2));
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertEquals(expectedName1, attr1.get());
        assertEquals(expectedName2, attr2.get());
    }

    @Test
    public void testUnequalDistinguishedNameValue() throws NamingException {
        // The names here are syntactically equal, but differ in exact string representation
        String expectedName1 = "cn=Jane Doe,ou=People";
        String expectedName2 = "cn=John Doe,ou=People";

        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(LdapUtils.newLdapName(expectedName1));
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(LdapUtils.newLdapName(expectedName2));

        assertFalse(attr1.equals(attr2));
        assertEquals(expectedName1, attr1.get());
        assertEquals(expectedName2, attr2.get());
    }

    @Test
    public void testComparingWDistinguishedNameValueWithInvalidName() throws NamingException {
        // The names here are syntactically equal, but differ in exact string representation
        String expectedName1 = "cn=Jane Doe,ou=People";
        String expectedValue2 = "thisisnotavaliddn";

        NameAwareAttribute attr1 = new NameAwareAttribute("someAttribute");
        attr1.add(LdapUtils.newLdapName(expectedName1));
        NameAwareAttribute attr2 = new NameAwareAttribute("someAttribute");
        attr2.add(expectedValue2);

        assertFalse(attr1.equals(attr2));
        assertEquals(expectedName1, attr1.get());
        assertEquals(expectedValue2, attr2.get());
    }
}
