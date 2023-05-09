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

package org.springframework.ldap.transaction.compensating;

import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class BindOperationRecorderTests {

	private LdapOperations ldapOperationsMock;

	@Before
	public void setUp() throws Exception {
		this.ldapOperationsMock = mock(LdapOperations.class);

	}

	@Test
	public void testRecordOperation_Name() {
		BindOperationRecorder tested = new BindOperationRecorder(this.ldapOperationsMock);
		LdapName expectedDn = LdapUtils.newLdapName("cn=John Doe");

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();
		// Perform test.
		CompensatingTransactionOperationExecutor operation = tested
				.recordOperation(new Object[] { expectedDn, expectedObject, expectedAttributes });

		assertThat(operation instanceof BindOperationExecutor).isTrue();
		BindOperationExecutor rollbackOperation = (BindOperationExecutor) operation;
		assertThat(rollbackOperation.getDn()).isSameAs(expectedDn);
		assertThat(rollbackOperation.getLdapOperations()).isSameAs(this.ldapOperationsMock);
		assertThat(rollbackOperation.getOriginalObject()).isSameAs(expectedObject);
		assertThat(expectedAttributes).isSameAs(rollbackOperation.getOriginalAttributes());
	}

	@Test
	public void testPerformOperation_String() {
		BindOperationRecorder tested = new BindOperationRecorder(this.ldapOperationsMock);
		String expectedDn = "cn=John Doe";

		Object expectedObject = new Object();
		BasicAttributes expectedAttributes = new BasicAttributes();
		// Perform test.
		CompensatingTransactionOperationExecutor operation = tested
				.recordOperation(new Object[] { expectedDn, expectedObject, expectedAttributes });

		assertThat(operation instanceof BindOperationExecutor).isTrue();
		BindOperationExecutor rollbackOperation = (BindOperationExecutor) operation;
		assertThat(rollbackOperation.getDn().toString()).isEqualTo(expectedDn);
		assertThat(rollbackOperation.getLdapOperations()).isSameAs(this.ldapOperationsMock);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPerformOperation_Invalid() {
		BindOperationRecorder tested = new BindOperationRecorder(this.ldapOperationsMock);
		Object expectedDn = new Object();

		// Perform test.
		tested.recordOperation(new Object[] { expectedDn });
	}

}
