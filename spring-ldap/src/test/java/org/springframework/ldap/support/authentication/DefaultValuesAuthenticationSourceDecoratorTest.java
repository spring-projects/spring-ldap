package org.springframework.ldap.support.authentication;

import org.easymock.MockControl;
import org.springframework.ldap.AuthenticationSource;
import org.springframework.ldap.support.authentication.DefaultValuesAuthenticationSourceDecorator;

import junit.framework.TestCase;

public class DefaultValuesAuthenticationSourceDecoratorTest extends TestCase {

    private static final String DEFAULT_PASSWORD = "defaultPassword";

    private static final String DEFAULT_USER = "cn=defaultUser";

    private DefaultValuesAuthenticationSourceDecorator tested;

    private MockControl authenticationSourceControl;

    private AuthenticationSource authenticationSourceMock;

    protected void setUp() throws Exception {
        super.setUp();

        authenticationSourceControl = MockControl
                .createControl(AuthenticationSource.class);
        authenticationSourceMock = (AuthenticationSource) authenticationSourceControl
                .getMock();
        tested = new DefaultValuesAuthenticationSourceDecorator();
        tested.setDefaultUser(DEFAULT_USER);
        tested.setDefaultPassword(DEFAULT_PASSWORD);
        tested.setTarget(authenticationSourceMock);
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        tested = null;
        authenticationSourceControl = null;
        authenticationSourceMock = null;
    }

    public void testGetPrincipal_TargetHasPrincipal() {
        authenticationSourceControl.expectAndDefaultReturn(
                authenticationSourceMock.getPrincipal(), "cn=someUser");
        authenticationSourceControl.replay();

        String principal = tested.getPrincipal();

        authenticationSourceControl.verify();
        assertEquals("cn=someUser", principal);
    }

    public void testGetPrincipal_TargetHasNoPrincipal() {
        authenticationSourceControl.expectAndDefaultReturn(
                authenticationSourceMock.getPrincipal(), "");
        authenticationSourceControl.replay();

        String principal = tested.getPrincipal();

        authenticationSourceControl.verify();
        assertEquals(DEFAULT_USER, principal);
    }

    public void testGetCredentials_TargetHasPrincipal() {
        authenticationSourceControl.expectAndDefaultReturn(
                authenticationSourceMock.getPrincipal(), "cn=someUser");
        authenticationSourceControl.expectAndDefaultReturn(
                authenticationSourceMock.getCredentials(), "somepassword");
        authenticationSourceControl.replay();

        String credentials = tested.getCredentials();

        authenticationSourceControl.verify();
        assertEquals("somepassword", credentials);
    }

    public void testGetCredentials_TargetHasNoPrincipal() {
        authenticationSourceControl.expectAndDefaultReturn(
                authenticationSourceMock.getPrincipal(), "");
        authenticationSourceControl.expectAndDefaultReturn(
                authenticationSourceMock.getCredentials(), "somepassword");
        authenticationSourceControl.replay();

        String credentials = tested.getCredentials();

        authenticationSourceControl.verify();
        assertEquals(DEFAULT_PASSWORD, credentials);
    }

    public void testAfterPropertiesSet_noTarget() throws Exception {
        tested.setTarget(null);
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    public void testAfterPropertiesSet_noDefaultUser() throws Exception {
        tested.setDefaultUser(null);
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    public void testAfterPropertiesSet_noDefaultPassword() throws Exception {
        tested.setDefaultPassword(null);
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }
}
