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
package org.springframework.ldap.authentication;

import org.easymock.MockControl;
import org.springframework.ldap.authentication.DefaultValuesAuthenticationSourceDecorator;
import org.springframework.ldap.core.AuthenticationSource;

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
