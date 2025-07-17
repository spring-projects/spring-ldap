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

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Unit tests for the LdapContextSource class.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class LdapContextSourceTests {

	private LdapContextSource tested;

	@BeforeEach
	public void setUp() throws Exception {
		this.tested = new LdapContextSource();
	}

	@Test
	public void testAfterPropertiesSet_NoUrl() {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> this.tested.afterPropertiesSet());
	}

	// gh-538
	@Test
	public void testAfterPropertiesSet_NullPassword() {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
			this.tested.setUrl("ldap://ldap.example.com:389");
			this.tested.setUserDn("value");
			this.tested.setPassword(null);
			this.tested.afterPropertiesSet();
		});
	}

	@Test
	public void testGetAnonymousEnv() throws Exception {
		this.tested.setBase("dc=some example,dc=se");
		this.tested.setUrl("ldap://ldap.example.com:389");
		this.tested.setPooled(true);
		this.tested.setUserDn("cn=Some User");
		this.tested.setPassword("secret");
		this.tested.afterPropertiesSet();
		Hashtable env = this.tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389/dc=some%20example,dc=se");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isEqualTo("true");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isNull();
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isNull();

		// check that base was added to environment
		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY))
			.isEqualTo(LdapUtils.newLdapName("dc=some example,dc=se"));

		// Verify that changing values does not change the environment values.
		this.tested.setBase("dc=other,dc=se");
		this.tested.setUrl("ldap://ldap2.example.com:389");
		this.tested.setPooled(false);

		env = this.tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389/dc=some%20example,dc=se");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isEqualTo("true");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isNull();
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isNull();

		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY))
			.isEqualTo(LdapUtils.newLdapName("dc=some example,dc=se"));
	}

	@Test
	public void testGetAnonymousEnvWithNoBaseSet() throws Exception {
		this.tested.setUrl("ldap://ldap.example.com:389");
		this.tested.afterPropertiesSet();
		Hashtable env = this.tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389");

		// check that base was not added to environment
		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY)).isNull();
	}

	@Test
	public void testGetAnonymousEnvWithBaseEnvironment() throws Exception {
		this.tested.setUrl("ldap://ldap.example.com:389");
		HashMap map = new HashMap();
		map.put(LdapContextSource.SUN_LDAP_POOLING_FLAG, "true");
		this.tested.setBaseEnvironmentProperties(map);
		this.tested.afterPropertiesSet();
		Hashtable env = this.tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isNull();
	}

	@Test
	public void testGetAnonymousEnvWithPoolingInBaseEnvironmentAndPoolingOff() throws Exception {
		this.tested.setUrl("ldap://ldap.example.com:389");
		HashMap map = new HashMap();
		map.put(LdapContextSource.SUN_LDAP_POOLING_FLAG, "true");
		this.tested.setBaseEnvironmentProperties(map);
		this.tested.setPooled(false);
		this.tested.afterPropertiesSet();
		Hashtable env = this.tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isNull();
	}

	@Test
	public void testGetAnonymousEnvWithEmptyBaseSet() throws Exception {
		this.tested.setUrl("ldap://ldap.example.com:389");
		this.tested.setBase(null);
		this.tested.afterPropertiesSet();
		Hashtable env = this.tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389");

		// check that base was not added to environment
		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY)).isNull();
	}

	@Test
	public void testGetAuthenticatedEnv() throws Exception {
		this.tested.setBase("dc=example,dc=se");
		this.tested.setUrl("ldap://ldap.example.com:389");
		this.tested.setPooled(true);
		this.tested.setUserDn("cn=Some User");
		this.tested.setPassword("secret");
		this.tested.afterPropertiesSet();

		Hashtable env = this.tested.getAuthenticatedEnv("cn=Some User", "secret");
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389/dc=example,dc=se");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isEqualTo("true");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isEqualTo("cn=Some User");
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isEqualTo("secret");

		// check that base was added to environment
		assertThat(env.get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY))
			.isEqualTo(LdapUtils.newLdapName("dc=example,dc=se"));
	}

	@Test
	public void testGetAnonymousEnvWhenCacheIsOff() throws Exception {
		this.tested.setBase("dc=example,dc=se");
		this.tested.setUrl("ldap://ldap.example.com:389");
		this.tested.setPooled(true);
		this.tested.setUserDn("cn=Some User");
		this.tested.setPassword("secret");
		this.tested.setCacheEnvironmentProperties(false);
		this.tested.afterPropertiesSet();
		Hashtable env = this.tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap.example.com:389/dc=example,dc=se");
		assertThat(env.get(LdapContextSource.SUN_LDAP_POOLING_FLAG)).isEqualTo("true");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isNull();
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isNull();

		this.tested.setUrl("ldap://ldap2.example.com:389");
		env = this.tested.getAnonymousEnv();
		assertThat(env.get(Context.PROVIDER_URL)).isEqualTo("ldap://ldap2.example.com:389/dc=example,dc=se");
	}

}
