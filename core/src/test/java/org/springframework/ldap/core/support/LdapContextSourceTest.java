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

package org.springframework.ldap.core.support;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Context;
import java.util.HashMap;
import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;

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

	// gh-538
	@Test(expected = IllegalArgumentException.class)
	public void testAfterPropertiesSet_NullPassword() {
		tested.setUrl("ldap://ldap.example.com:389");
		tested.setUserDn("value");
		tested.setPassword(null);
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
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389/dc=some%20example,dc=se");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isEqualTo("true");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isNull();
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isNull();

		// check that base was added to environment
		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY)).isEqualTo(LdapUtils.newLdapName("dc=some example,dc=se"));

		// Verify that changing values does not change the environment values.
		tested.setBase("dc=other,dc=se");
		tested.setUrl("ldap://ldap2.example.com:389");
		tested.setPooled(false);

		env = tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389/dc=some%20example,dc=se");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isEqualTo("true");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isNull();
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isNull();

		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY)).isEqualTo(LdapUtils.newLdapName("dc=some example,dc=se"));
	}

    @Test
	public void testGetAnonymousEnvWithNoBaseSet() throws Exception {
		tested.setUrl("ldap://ldap.example.com:389");
		tested.afterPropertiesSet();
		Hashtable env = tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389");

		// check that base was not added to environment
		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY)).isNull();
	}

    @Test
	public void testGetAnonymousEnvWithBaseEnvironment() throws Exception {
		tested.setUrl("ldap://ldap.example.com:389");
		HashMap map = new HashMap();
		map.put(LdapContextSource.SUN_LDAP_POOLING_FLAG, "true");
		tested.setBaseEnvironmentProperties(map);
		tested.afterPropertiesSet();
		Hashtable env = tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isNull();
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
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isNull();
	}

    @Test
	public void testGetAnonymousEnvWithEmptyBaseSet() throws Exception {
		tested.setUrl("ldap://ldap.example.com:389");
		tested.setBase(null);
		tested.afterPropertiesSet();
		Hashtable env = tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389");

		// check that base was not added to environment
		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY)).isNull();
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
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389/dc=example,dc=se");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isEqualTo("true");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isEqualTo("cn=Some User");
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isEqualTo("secret");

		// check that base was added to environment
		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY)).isEqualTo(LdapUtils.newLdapName("dc=example,dc=se"));
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
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389/dc=example,dc=se");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isEqualTo("true");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isNull();
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isNull();

		tested.setUrl("ldap://ldap2.example.com:389");
		env = tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap2.example.com:389/dc=example,dc=se");
	}
}
