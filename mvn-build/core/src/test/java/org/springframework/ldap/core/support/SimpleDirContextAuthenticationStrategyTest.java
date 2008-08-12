/*
 * Copyright 2005-2008 the original author or authors.
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

import java.util.Hashtable;

import javax.naming.Context;

import junit.framework.TestCase;

public class SimpleDirContextAuthenticationStrategyTest extends TestCase {
	private SimpleDirContextAuthenticationStrategy tested;

	protected void setUp() throws Exception {
		super.setUp();

		tested = new SimpleDirContextAuthenticationStrategy();
	}

	public void testSetupEnvironment() {
		Hashtable env = new Hashtable();
		tested.setupEnvironment(env, "cn=John Doe", "pw");

		assertEquals("simple", env.get(Context.SECURITY_AUTHENTICATION));
		assertEquals("cn=John Doe", env.get(Context.SECURITY_PRINCIPAL));
		assertEquals("pw", env.get(Context.SECURITY_CREDENTIALS));
	}

	public void testProcessContextAfterCreation() {
		Hashtable env = new Hashtable();
		tested.processContextAfterCreation(null, "cn=John Doe", "pw");

		assertTrue(env.isEmpty());
	}
}
