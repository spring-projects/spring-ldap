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

import javax.naming.ldap.LdapName;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.LdapUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class RenameOperationExecutorTests {

	private LdapOperations ldapOperationsMock;

	@Before
	public void setUp() throws Exception {
		this.ldapOperationsMock = mock(LdapOperations.class);
		;
	}

	@Test
	public void testPerformOperation() {
		LdapName expectedNewName = LdapUtils.newLdapName("ou=newOu");
		LdapName expectedOldName = LdapUtils.newLdapName("ou=someou");
		RenameOperationExecutor tested = new RenameOperationExecutor(this.ldapOperationsMock, expectedOldName,
				expectedNewName);

		// Perform test.
		tested.performOperation();

		verify(this.ldapOperationsMock).rename(expectedOldName, expectedNewName);
	}

	@Test
	public void testCommit() {
		LdapName expectedNewName = LdapUtils.newLdapName("ou=newOu");
		LdapName expectedOldName = LdapUtils.newLdapName("ou=someou");
		RenameOperationExecutor tested = new RenameOperationExecutor(this.ldapOperationsMock, expectedOldName,
				expectedNewName);

		// Nothing to do for this operation.
		verifyNoMoreInteractions(this.ldapOperationsMock);

		// Perform test.
		tested.commit();
	}

	@Test
	public void testRollback() {
		LdapName expectedNewName = LdapUtils.newLdapName("ou=newOu");
		LdapName expectedOldName = LdapUtils.newLdapName("ou=someou");
		RenameOperationExecutor tested = new RenameOperationExecutor(this.ldapOperationsMock, expectedOldName,
				expectedNewName);

		// Perform test.
		tested.rollback();

		verify(this.ldapOperationsMock).rename(expectedNewName, expectedOldName);
	}

}
