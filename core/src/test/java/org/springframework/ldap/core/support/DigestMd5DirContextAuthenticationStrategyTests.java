/*
 * Copyright 2006-present the original author or authors.
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

import java.util.Hashtable;

import javax.naming.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DigestMd5DirContextAuthenticationStrategyTests {

	private DigestMd5DirContextAuthenticationStrategy tested;

	@BeforeEach
	public void setUp() {
		this.tested = new DigestMd5DirContextAuthenticationStrategy();
	}

	@Test
	public void setupEnvironmentSetsAuthenticationPrincipalAndCredentials() {
		Hashtable<String, Object> env = new Hashtable<>();
		this.tested.setupEnvironment(env, "username", "pw");

		assertThat(env.get(Context.SECURITY_AUTHENTICATION)).isEqualTo("DIGEST-MD5");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isEqualTo("username");
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isEqualTo("pw");
	}

	@Test
	public void setupEnvironmentWhenUserDnIsSetAndPasswordIsEmptyThenAuthenticationException() {
		Hashtable<String, Object> env = new Hashtable<>();
		assertThatExceptionOfType(AuthenticationException.class)
			.isThrownBy(() -> this.tested.setupEnvironment(env, "username", ""));
	}

	@Test
	public void setupEnvironmentWhenUserDnIsSetAndPasswordIsNullThenAuthenticationException() {
		Hashtable<String, Object> env = new Hashtable<>();
		assertThatExceptionOfType(AuthenticationException.class)
			.isThrownBy(() -> this.tested.setupEnvironment(env, "username", null));
	}

}
