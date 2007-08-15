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

import junit.framework.TestCase;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.easymock.MockControl;
import org.springframework.ldap.authentication.AcegiAuthenticationSource;

public class AcegiAuthenticationSourceTest extends TestCase {

    private MockControl authenticationControl;

    private Authentication authenticationMock;

    private MockControl ldapUserDetailsControl;

    private LdapUserDetails ldapUserDetailsMock;

    private AcegiAuthenticationSource tested;

    protected void setUp() throws Exception {
        super.setUp();

        authenticationControl = MockControl.createControl(Authentication.class);
        authenticationMock = (Authentication) authenticationControl.getMock();

        ldapUserDetailsControl = MockControl
                .createControl(LdapUserDetails.class);
        ldapUserDetailsMock = (LdapUserDetails) ldapUserDetailsControl
                .getMock();

        tested = new AcegiAuthenticationSource();
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        authenticationControl = null;
        authenticationMock = null;

        ldapUserDetailsControl = null;
        ldapUserDetailsMock = null;

        tested = null;
    }

    protected void replay() {
        authenticationControl.replay();
        ldapUserDetailsControl.replay();
    }

    protected void verify() {
        authenticationControl.verify();
        ldapUserDetailsControl.verify();
    }

    public void testGetPrincipalAndCredentials() {
        authenticationControl.expectAndReturn(
                authenticationMock.getPrincipal(), ldapUserDetailsMock);
        authenticationControl.expectAndReturn(authenticationMock
                .getCredentials(), "secret");

        ldapUserDetailsControl.expectAndDefaultReturn(ldapUserDetailsMock
                .getDn(), "cn=Manager");

        SecurityContextHolder.getContext()
                .setAuthentication(authenticationMock);

        replay();

        String principal = tested.getPrincipal();
        String credentials = tested.getCredentials();

        verify();

        assertEquals("secret", credentials);
        assertEquals("cn=Manager", principal);
    }

    public void testGetPrincipalAndCredentials_nullAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);

        replay();

        assertEquals("", tested.getPrincipal());
        assertEquals("", tested.getCredentials());
        verify();
    }

    public void testGetPrincipal_InvalidPrincipal() {
        authenticationControl.expectAndReturn(
                authenticationMock.getPrincipal(), new User("dummy", "dummy",
                        true, true, true, true, new GrantedAuthority[0]));

        SecurityContextHolder.getContext()
                .setAuthentication(authenticationMock);

        replay();

        try {
            tested.getPrincipal();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        verify();
    }

    public void testGetPrincipalWithAnonymousAuthenticationToken() {

        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("dummy", "dummy",
                        new GrantedAuthority[] { new DummyAuthoroty() }));

        assertEquals("", tested.getPrincipal());
    }

    private static class DummyAuthoroty implements GrantedAuthority {
        public String getAuthority() {
            return null;
        }
    }
}
