/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.transaction.compensating;

import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.LdapUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RebindOperationExecutorTests {

	private LdapOperations ldapOperationsMock;

	@Before
	public void setUp() throws Exception {
		this.ldapOperationsMock = mock(LdapOperations.class);
	}

	@Test
	public void testPerformOperation() {
		LdapName expectedOriginalDn = LdapUtils.newLdapName("cn=john doe");
		LdapName expectedTempDn = LdapUtils.newLdapName("cn=john doe_temp");
		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();
		RebindOperationExecutor tested = new RebindOperationExecutor(this.ldapOperationsMock, expectedOriginalDn,
				expectedTempDn, expectedObject, expectedAttributes);

		// perform test
		tested.performOperation();
		verify(this.ldapOperationsMock).rename(expectedOriginalDn, expectedTempDn);
		verify(this.ldapOperationsMock).bind(expectedOriginalDn, expectedObject, expectedAttributes);
	}

	@Test
	public void testCommit() {
		LdapName expectedOriginalDn = LdapUtils.newLdapName("cn=john doe");
		LdapName expectedTempDn = LdapUtils.newLdapName("cn=john doe_temp");
		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();
		RebindOperationExecutor tested = new RebindOperationExecutor(this.ldapOperationsMock, expectedOriginalDn,
				expectedTempDn, expectedObject, expectedAttributes);

		// perform test
		tested.commit();
		verify(this.ldapOperationsMock).unbind(expectedTempDn);
	}

	@Test
	public void testRollback() {
		LdapName expectedOriginalDn = LdapUtils.newLdapName("cn=john doe");
		LdapName expectedTempDn = LdapUtils.newLdapName("cn=john doe_temp");
		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();
		RebindOperationExecutor tested = new RebindOperationExecutor(this.ldapOperationsMock, expectedOriginalDn,
				expectedTempDn, expectedObject, expectedAttributes);

		// perform test
		tested.rollback();

		verify(this.ldapOperationsMock).unbind(expectedOriginalDn);
		verify(this.ldapOperationsMock).rename(expectedTempDn, expectedOriginalDn);
	}

}
