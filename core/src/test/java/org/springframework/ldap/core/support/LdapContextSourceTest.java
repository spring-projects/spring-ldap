/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.core.support;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Context;
import java.util.HashMap;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the LdapContextSource class.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class LdapContextSourceTest {

	private LdapContextSource tested;

    @Before
	public void setUp() throws Exception {
		tested = new LdapContextSource();
	}

    @Test(expected = IllegalArgumentException.class)
	public void testAfterPropertiesSet_NoUrl() throws Exception {
        tested.afterPropertiesSet();
    }

    @Test(expected = IllegalArgumentException.class)
	public void testAfterPropertiesSet_BaseAndTooEarlyJdk() throws Exception {
		tested = new LdapContextSource() {
			String getJdkVersion() {
				return "1.4.1_03";
			}
		};

		tested.setUrl("http://ldap.example.com:389");
		tested.setBase("dc=jayway,dc=se");
        tested.afterPropertiesSet();
    }

    @Test
	public void testGetAnonymousEnv() throws Exception {
		tested.setBase("dc=some example,dc=se");
		tested.setUrl("ldap://ldap.example.com:389");
		tested.setPooled(true);
		tested.setUserDn("cn=Some User");
		tested.setPassword("secret");
		tested.afterPropertiesSet();
		Hashtable env = tested.getAnonymousEnv();
		assertEquals("ldap://ldap.example.com:389/dc=some%20example,dc=se", env.get(Context.PROVIDER_URL));
		assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
		assertNull(env.get(Context.SECURITY_PRINCIPAL));
		assertNull(env.get(Context.SECURITY_CREDENTIALS));

		// check that base was added to environment
		assertEquals(LdapUtils.newLdapName("dc=some example,dc=se"), env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY));

		// Verify that changing values does not change the environment values.
		tested.setBase("dc=other,dc=se");
		tested.setUrl("ldap://ldap2.example.com:389");
		tested.setPooled(false);

		env = tested.getAnonymousEnv();
		assertEquals("ldap://ldap.example.com:389/dc=some%20example,dc=se", env.get(Context.PROVIDER_URL));
		assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
		assertNull(env.get(Context.SECURITY_PRINCIPAL));
		assertNull(env.get(Context.SECURITY_CREDENTIALS));

		assertEquals(LdapUtils.newLdapName("dc=some example,dc=se"), env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY));
	}

    @Test
	public void testGetAnonymousEnvWithNoBaseSet() throws Exception {
		tested.setUrl("ldap://ldap.example.com:389");
		tested.afterPropertiesSet();
		Hashtable env = tested.getAnonymousEnv();
		assertEquals("ldap://ldap.example.com:389", env.get(Context.PROVIDER_URL));

		// check that base was not added to environment
		assertNull(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY));
	}

    @Test
	public void testGetAnonymousEnvWithBaseEnvironment() throws Exception {
		tested.setUrl("ldap://ldap.example.com:389");
		HashMap map = new HashMap();
		map.put(LdapContextSource.SUN_LDAP_POOLING_FLAG, "true");
		tested.setBaseEnvironmentProperties(map);
		tested.afterPropertiesSet();
		Hashtable env = tested.getAnonymousEnv();
		assertEquals("ldap://ldap.example.com:389", env.get(Context.PROVIDER_URL));
		assertNull(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
	}

    @Test
	public void testGetAnonymousEnvWithPoolingInBaseEnvironmentAndPoolingOff() throws Exception {
		tested.setUrl("ldap://ldap.example.com:389");
		HashMap map = new HashMap();
		map.put(LdapContextSource.SUN_LDAP_POOLING_FLAG, "true");
		tested.setBaseEnvironmentProperties(map);
		tested.setPooled(false);
		tested.afterPropertiesSet();
		Hashtable env = tested.getAnonymousEnv();
		assertEquals("ldap://ldap.example.com:389", env.get(Context.PROVIDER_URL));
		assertNull(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
	}

    @Test
	public void testGetAnonymousEnvWithEmptyBaseSet() throws Exception {
		tested.setUrl("ldap://ldap.example.com:389");
		tested.setBase(null);
		tested.afterPropertiesSet();
		Hashtable env = tested.getAnonymousEnv();
		assertEquals("ldap://ldap.example.com:389", env.get(Context.PROVIDER_URL));

		// check that base was not added to environment
		assertNull(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY));
	}

    @Test
	public void testOldJdkWithNoBaseSetShouldWork() throws Exception {
		tested = new LdapContextSource() {
			String getJdkVersion() {
				return "1.3";
			}
		};
		tested.setUrl("ldap://ldap.example.com:389");
		tested.afterPropertiesSet();

		// check that base was not added to environment
		Hashtable env = tested.getAnonymousEnv();
		assertNull(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY));
	}

    @Test(expected = IllegalArgumentException.class)
	public void testOldJdkWithBaseSetShouldNotWork() throws Exception {
		tested = new LdapContextSource() {
			String getJdkVersion() {
				return "1.3";
			}
		};
		tested.setUrl("ldap://ldap.example.com:389");
		tested.setBase("dc=example,dc=com");
        tested.afterPropertiesSet();
    }

    @Test
	public void testOldJdkWithBaseSetToEmptyPathShouldWork() throws Exception {
		tested = new LdapContextSource() {
			String getJdkVersion() {
				return "1.3";
			}
		};
		tested.setUrl("ldap://ldap.example.com:389");
		tested.setBase(null);
		tested.afterPropertiesSet();

		// check that base was not added to environment
		Hashtable env = tested.getAnonymousEnv();
		assertNull(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY));
	}

    @Test
	public void testGetAuthenticatedEnv() throws Exception {
		tested.setBase("dc=example,dc=se");
		tested.setUrl("ldap://ldap.example.com:389");
		tested.setPooled(true);
		tested.setUserDn("cn=Some User");
		tested.setPassword("secret");
		tested.afterPropertiesSet();

		Hashtable env = tested.getAuthenticatedEnv("cn=Some User", "secret");
		assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env.get(Context.PROVIDER_URL));
		assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
		assertEquals("cn=Some User", env.get(Context.SECURITY_PRINCIPAL));
		assertEquals("secret", env.get(Context.SECURITY_CREDENTIALS));

		// check that base was added to environment
		assertEquals(LdapUtils.newLdapName("dc=example,dc=se"), env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY));
	}

    @Test
	public void testGetAnonymousEnvWhenCacheIsOff() throws Exception {
		tested.setBase("dc=example,dc=se");
		tested.setUrl("ldap://ldap.example.com:389");
		tested.setPooled(true);
		tested.setUserDn("cn=Some User");
		tested.setPassword("secret");
		tested.setCacheEnvironmentProperties(false);
		tested.afterPropertiesSet();
		Hashtable env = tested.getAnonymousEnv();
		assertEquals("ldap://ldap.example.com:389/dc=example,dc=se", env.get(Context.PROVIDER_URL));
		assertEquals("true", env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG));
		assertNull(env.get(Context.SECURITY_PRINCIPAL));
		assertNull(env.get(Context.SECURITY_CREDENTIALS));

		tested.setUrl("ldap://ldap2.example.com:389");
		env = tested.getAnonymousEnv();
		assertEquals("ldap://ldap2.example.com:389/dc=example,dc=se", env.get(Context.PROVIDER_URL));
	}
}
