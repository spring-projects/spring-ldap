/*
 * Copyright 2005-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.authentication;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.AuthenticationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultValuesAuthenticationSourceDecoratorTest {

    private static final String DEFAULT_PASSWORD = "defaultPassword";

    private static final String DEFAULT_USER = "cn=defaultUser";

    private DefaultValuesAuthenticationSourceDecorator tested;

    private AuthenticationSource authenticationSourceMock;

    @Before
    public void setUp() throws Exception {
        authenticationSourceMock = mock(AuthenticationSource.class);
        tested = new DefaultValuesAuthenticationSourceDecorator();
        tested.setDefaultUser(DEFAULT_USER);
        tested.setDefaultPassword(DEFAULT_PASSWORD);
        tested.setTarget(authenticationSourceMock);
    }

    @Test
    public void testGetPrincipal_TargetHasPrincipal() {
        when(authenticationSourceMock.getPrincipal()).thenReturn("cn=someUser");
        String principal = tested.getPrincipal();

        assertThat(principal).isEqualTo("cn=someUser");
    }

    @Test
    public void testGetPrincipal_TargetHasNoPrincipal() {
        when(authenticationSourceMock.getPrincipal()).thenReturn("");

        String principal = tested.getPrincipal();

        assertThat(principal).isEqualTo(DEFAULT_USER);
    }

    @Test
    public void testGetCredentials_TargetHasPrincipal() {
        when(authenticationSourceMock.getPrincipal()).thenReturn("cn=someUser");
        when(authenticationSourceMock.getCredentials()).thenReturn("somepassword");

        String credentials = tested.getCredentials();

        assertThat(credentials).isEqualTo("somepassword");
    }

    @Test
    public void testGetCredentials_TargetHasNoPrincipal() {
        when(authenticationSourceMock.getPrincipal()).thenReturn("");
        when(authenticationSourceMock.getCredentials()).thenReturn("somepassword");

        String credentials = tested.getCredentials();

        assertThat(credentials).isEqualTo(DEFAULT_PASSWORD);
    }

    @Test
    public void testAfterPropertiesSet_noTarget() throws Exception {
        tested.setTarget(null);
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertThat(true).isTrue();
        }
    }

    @Test
    public void testAfterPropertiesSet_noDefaultUser() throws Exception {
        tested.setDefaultUser(null);
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertThat(true).isTrue();
        }
    }

    @Test
    public void testAfterPropertiesSet_noDefaultPassword() throws Exception {
        tested.setDefaultPassword(null);
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertThat(true).isTrue();
        }
    }
}
