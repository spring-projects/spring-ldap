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

import javax.naming.Context;
import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleDirContextAuthenticationStrategyTest {

	private SimpleDirContextAuthenticationStrategy tested;

	@Before
	public void setUp() throws Exception {
		tested = new SimpleDirContextAuthenticationStrategy();
	}

	@Test
	public void testSetupEnvironment() {
		Hashtable env = new Hashtable();
		tested.setupEnvironment(env, "cn=John Doe", "pw");

		assertThat(env.get(Context.SECURITY_AUTHENTICATION)).isEqualTo("simple");
		assertThat(env.get(Context.SECURITY_PRINCIPAL)).isEqualTo("cn=John Doe");
		assertThat(env.get(Context.SECURITY_CREDENTIALS)).isEqualTo("pw");
	}

	@Test
	public void testProcessContextAfterCreation() {
		Hashtable env = new Hashtable();
		tested.processContextAfterCreation(null, "cn=John Doe", "pw");

		assertThat(env.isEmpty()).isTrue();
	}

}
