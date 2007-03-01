package org.springframework.ldap.authentication;

import junit.framework.TestCase;

public class ThreadLocalAuthenticationSourceTest extends TestCase {

    private ThreadLocalAuthenticationSource tested;

    protected void setUp() throws Exception {
        ThreadLocalAuthenticationHolder.setPrincipal(null);
        ThreadLocalAuthenticationHolder.setCredentials(null);

        tested = new ThreadLocalAuthenticationSource();
    }

    public void testGetPrincipal() {
        ThreadLocalAuthenticationHolder.setPrincipal("cn=john doe");
        String result = tested.getPrincipal();

        assertEquals("cn=john doe", result);
    }

    public void testGetPrincipal_NoDataBound() {
        String result = tested.getPrincipal();

        assertNull("Should be null", result);
    }

    public void testGetCredentials() {
        ThreadLocalAuthenticationHolder.setCredentials("secret");
        String result = tested.getCredentials();

        assertEquals("secret", result);
    }

    public void testGetCredentials_NoDataBound() {
        String result = tested.getCredentials();

        assertNull("Should be null", result);
    }
}
