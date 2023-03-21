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

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class BindOperationExecutorTest {

	private LdapOperations ldapOperationsMock;

	@Before
	public void setUp() throws Exception {
		ldapOperationsMock = mock(LdapOperations.class);
	}

	@Test
	public void testPerformOperation() {
		LdapName expectedDn = LdapUtils.newLdapName("cn=john doe");
		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();
		BindOperationExecutor tested = new BindOperationExecutor(ldapOperationsMock, expectedDn, expectedObject,
				expectedAttributes);

		// perform teste
		tested.performOperation();

		verify(ldapOperationsMock).bind(expectedDn, expectedObject, expectedAttributes);
	}

	@Test
	public void testCommit() {
		LdapName expectedDn = LdapUtils.newLdapName("cn=john doe");
		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();
		BindOperationExecutor tested = new BindOperationExecutor(ldapOperationsMock, expectedDn, expectedObject,
				expectedAttributes);

		verifyNoMoreInteractions(ldapOperationsMock);

		// perform teste
		tested.commit();
	}

	@Test
	public void testRollback() {
		LdapName expectedDn = LdapUtils.newLdapName("cn=john doe");
		BindOperationExecutor tested = new BindOperationExecutor(ldapOperationsMock, expectedDn, null, null);

		// perform teste
		tested.rollback();

		verify(ldapOperationsMock).unbind(expectedDn);
	}

}
