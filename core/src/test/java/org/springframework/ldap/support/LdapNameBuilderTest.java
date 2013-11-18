package org.springframework.ldap.support;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapNameBuilderTest {

    @Test
    public void testAddComponentToEmpty() {
        LdapNameBuilder tested = LdapNameBuilder.newInstance().add("dc", "com").add("dc", "261consulting");
        assertEquals("dc=261consulting,dc=com", tested.build().toString());
    }

    @Test
    public void testAddComponentToBaseString() {
        LdapNameBuilder tested = LdapNameBuilder.newInstance("dc=com").add("dc", "261consulting");
        assertEquals("dc=261consulting,dc=com", tested.build().toString());
    }

    @Test
    public void testAddComponentToBaseName() {
        LdapNameBuilder tested = LdapNameBuilder.newInstance(LdapUtils.newLdapName("dc=com")).add("dc", "261consulting");
        assertEquals("dc=261consulting,dc=com", tested.build().toString());
    }

    @Test
    public void testAddStringNameToBaseString() {
        LdapNameBuilder tested = LdapNameBuilder.newInstance("dc=261consulting,dc=com").add("ou=people");
        assertEquals("ou=people,dc=261consulting,dc=com", tested.build().toString());
    }

    @Test
    public void testAddNameToBaseString() {
        LdapNameBuilder tested = LdapNameBuilder.newInstance("dc=261consulting,dc=com").add(LdapUtils.newLdapName("ou=people"));
        assertEquals("ou=people,dc=261consulting,dc=com", tested.build().toString());
    }

    @Test
    public void testAddNameToEmpty() {
        LdapNameBuilder tested = LdapNameBuilder.newInstance().add(LdapUtils.newLdapName("ou=people"));
        assertEquals("ou=people", tested.build().toString());
    }

    @Test
    public void testAddEmptyToEmpty() {
        LdapNameBuilder tested = LdapNameBuilder.newInstance().add("");
        assertEquals("", tested.build().toString());
    }

}
