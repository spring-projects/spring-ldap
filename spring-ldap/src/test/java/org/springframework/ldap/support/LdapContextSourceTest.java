/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ldap.support;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.ldap.Control;
import javax.naming.ldap.ControlFactory;

import junit.framework.TestCase;

import org.springframework.ldap.AuthenticationSource;

import com.sun.jndi.ldap.ctl.ResponseControlFactory;

/**
 * Unit tests for the LdapContextSource class.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class LdapContextSourceTest extends TestCase {

    private LdapContextSource tested;

    protected void setUp() throws Exception {
        tested = new LdapContextSource();
    }

    protected void tearDown() throws Exception {
        tested = null;
    }

    public void testAfterPropertiesSet_NoUrl() throws Exception {
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    public void testAfterPropertiesSet_BaseAndTooEarlyJdk() throws Exception {
        tested = new LdapContextSource() {
            String getJdkVersion() {
                return "1.4.1_03";
            }
        };

        tested.setUrl("http://ldap.example.com:389");
        tested.setBase("dc=jayway,dc=se");
        try {
            tested.afterPropertiesSet();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    public void testGetAnonymousEnv() throws Exception {
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        tested.setUserName("cn=Some User");
        tested.setPassword("secret");
        tested.afterPropertiesSet();
        Hashtable env = tested.getAnonymousEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertNull(env.get(Context.SECURITY_PRINCIPAL));
        assertNull(env.get(Context.SECURITY_CREDENTIALS));

        // Verify that changing values does not change the environment values.
        tested.setBase("dc=other,dc=se");
        tested.setUrl("ldap://ldap2.example.com:389");
        tested.setPooled(false);

        env = tested.getAnonymousEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertNull(env.get(Context.SECURITY_PRINCIPAL));
        assertNull(env.get(Context.SECURITY_CREDENTIALS));
    }

    public void testGetAuthenticatedEnv() throws Exception {
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        tested.setUserName("cn=Some User");
        tested.setPassword("secret");
        tested.afterPropertiesSet();

        Hashtable env = tested.getAuthenticatedEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertEquals("cn=Some User", env.get(Context.SECURITY_PRINCIPAL));
        assertEquals("secret", env.get(Context.SECURITY_CREDENTIALS));
    }

    public void testGetAuthenticatedEnv_DummyAuthenticationProvider()
            throws Exception {
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        DummyAuthenticationProvider authenticationProvider = new DummyAuthenticationProvider();
        tested.setAuthenticationSource(authenticationProvider);
        authenticationProvider.setPrincipal("cn=Some User");
        authenticationProvider.setCredentials("secret");
        tested.afterPropertiesSet();

        Hashtable env = tested.getAuthenticatedEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertEquals("cn=Some User", env.get(Context.SECURITY_PRINCIPAL));
        assertEquals("secret", env.get(Context.SECURITY_CREDENTIALS));
    }

    public void testGetAuthenticatedEnv_DummyAuthenticationProvider_Changed()
            throws Exception {
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        DummyAuthenticationProvider authenticationProvider = new DummyAuthenticationProvider();
        tested.setAuthenticationSource(authenticationProvider);
        authenticationProvider.setPrincipal("cn=Some User");
        authenticationProvider.setCredentials("secret");
        tested.afterPropertiesSet();

        authenticationProvider.setPrincipal("cn=Some Other User");
        authenticationProvider.setCredentials("other secret");

        Hashtable env = tested.getAuthenticatedEnv();
        assertEquals("cn=Some Other User", env.get(Context.SECURITY_PRINCIPAL));
        assertEquals("other secret", env.get(Context.SECURITY_CREDENTIALS));

    }

    public void testGetAnonymousEnv_DontCacheEnv() throws Exception {
        tested.setBase("dc=example,dc=se");
        tested.setUrl("ldap://ldap.example.com:389");
        tested.setPooled(true);
        tested.setUserName("cn=Some User");
        tested.setPassword("secret");
        tested.setCacheEnvironmentProperties(false);
        tested.afterPropertiesSet();
        Hashtable env = tested.getAnonymousEnv();
        assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
        assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
        assertNull(env.get(Context.SECURITY_PRINCIPAL));
        assertNull(env.get(Context.SECURITY_CREDENTIALS));

        tested.setUrl("ldap://ldap2.example.com:389");
        env = tested.getAnonymousEnv();
        assertEquals("ldap://ldap2.example.com:389/dc=example,dc=se", env
                .get(Context.PROVIDER_URL));
    }

    public void testsetResponseControlFactory_Null() throws Exception {
        tested.setResponseControlFactory(null);
        assertNotNull(tested.getResponseControlFactory());
        assertEquals(ResponseControlFactory.class, tested
                .getResponseControlFactory());
    }

    public void testsetResponseControlFactory_Valid() throws Exception {
        tested.setResponseControlFactory(ControlFactory.class);
        assertEquals(ControlFactory.class, tested.getResponseControlFactory());
    }

    public void testsetResponseControlFactory_Invalid() throws Exception {
        try {
            tested.setResponseControlFactory(Control.class);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    private class DummyAuthenticationProvider implements AuthenticationSource {
        private String principal;

        private String credentials;

        public void setCredentials(String credentials) {
            this.credentials = credentials;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public String getPrincipal() {
            return principal;
        }

        public String getCredentials() {
            return credentials;
        }
    }
}
