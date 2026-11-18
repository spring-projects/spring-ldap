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

package org.springframework.ldap.transaction.compensating;

import javax.naming.Name;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.SchemaViolationException;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class RebindOperationExecutorTests {

	private static final LdapName ORIGINAL = LdapUtils.newLdapName("cn=john doe");

	private static final LdapName TEMP = LdapUtils.newLdapName("cn=john doe_temp");

	private LdapOperations ldapOperationsMock;

	@BeforeEach
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
		tested.performOperation();
		tested.rollback();

		verify(this.ldapOperationsMock).unbind(expectedOriginalDn);
		verify(this.ldapOperationsMock).rename(expectedTempDn, expectedOriginalDn);
	}

	@Test
	public void testRollbackWithoutPerformDoesNothing() {
		RebindOperationExecutor tested = executor();

		tested.rollback();

		verifyNoInteractions(this.ldapOperationsMock);
	}

	@Test
	public void testFailedRenameCompensatesNothing() {
		RebindOperationExecutor tested = executor();
		willThrow(new NameAlreadyBoundException(new javax.naming.NameAlreadyBoundException(("temp exists"))))
			.given(this.ldapOperationsMock)
			.rename(ORIGINAL, TEMP);

		assertThatExceptionOfType(NameAlreadyBoundException.class).isThrownBy(tested::performOperation);
		tested.rollback();

		verify(this.ldapOperationsMock, never()).bind(any(Name.class), any(), any());
		verify(this.ldapOperationsMock, never()).unbind(any(Name.class));
		verify(this.ldapOperationsMock, never()).rename(TEMP, ORIGINAL);
	}

	@Test
	public void testFailedBindRenamesTemporaryEntryBack() {
		RebindOperationExecutor tested = executor();
		RuntimeException failure = new SchemaViolationException(
				new javax.naming.directory.SchemaViolationException(("bad entry")));
		willThrow(failure).given(this.ldapOperationsMock).bind(eq(ORIGINAL), any(), any());

		assertThatExceptionOfType(SchemaViolationException.class).isThrownBy(tested::performOperation)
			.isSameAs(failure);

		verify(this.ldapOperationsMock).rename(TEMP, ORIGINAL);
		verify(this.ldapOperationsMock, never()).unbind(any(Name.class));

		// a later rollback must not compensate anything a second time
		tested.rollback();
		verify(this.ldapOperationsMock, times(1)).rename(TEMP, ORIGINAL);
		verify(this.ldapOperationsMock, never()).unbind(any(Name.class));
	}

	@Test
	public void testFailedBindWithFailedRestoreStillRethrowsOriginal() {
		RebindOperationExecutor tested = executor();
		RuntimeException failure = new SchemaViolationException(
				new javax.naming.directory.SchemaViolationException(("bad entry")));
		willThrow(failure).given(this.ldapOperationsMock).bind(eq(ORIGINAL), any(), any());
		willThrow(new NameAlreadyBoundException(new javax.naming.NameAlreadyBoundException(("occupied"))))
			.given(this.ldapOperationsMock)
			.rename(TEMP, ORIGINAL);

		assertThatExceptionOfType(SchemaViolationException.class).isThrownBy(tested::performOperation)
			.isSameAs(failure);

		// the restore is attempted exactly once, and only by performOperation: the
		// transaction manager never records an executor whose performOperation threw,
		// so rollback() is not invoked for it and the failed rename is not retried
		verify(this.ldapOperationsMock, times(1)).rename(TEMP, ORIGINAL);
		verify(this.ldapOperationsMock, never()).unbind(any(Name.class));
	}

	private RebindOperationExecutor executor() {
		return new RebindOperationExecutor(this.ldapOperationsMock, ORIGINAL, TEMP, new Object(),
				new BasicAttributes());
	}

}
