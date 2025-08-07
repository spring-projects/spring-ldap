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

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RenameOperationRecorderTests {

	private LdapOperations ldapOperationsMock;

	@Before
	public void setUp() throws Exception {
		this.ldapOperationsMock = mock(LdapOperations.class);
	}

	@Test
	public void testRecordOperation() {
		RenameOperationRecorder tested = new RenameOperationRecorder(this.ldapOperationsMock);

		// Perform test
		CompensatingTransactionOperationExecutor operation = tested
			.recordOperation(new Object[] { "ou=someou", "ou=newou" });

		assertThat(operation instanceof RenameOperationExecutor).isTrue();
		RenameOperationExecutor rollbackOperation = (RenameOperationExecutor) operation;
		assertThat(rollbackOperation.getLdapOperations()).isSameAs(this.ldapOperationsMock);
		assertThat(rollbackOperation.getNewDn().toString()).isEqualTo("ou=newou");
		assertThat(rollbackOperation.getOriginalDn().toString()).isEqualTo("ou=someou");
	}

}
