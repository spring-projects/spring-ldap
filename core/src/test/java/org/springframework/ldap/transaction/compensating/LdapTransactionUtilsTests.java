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

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.support.LdapUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LdapTransactionUtilsTests {

	private DirContext dirContextMock;

	@BeforeEach
	public void setUp() throws Exception {
		this.dirContextMock = mock(DirContext.class);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	public void testCloseContext() throws NamingException {
		LdapUtils.closeContext(this.dirContextMock);
		verify(this.dirContextMock).close();
	}

	@Test
	public void testCloseContext_NullContext() throws NamingException {
		LdapUtils.closeContext(null);
	}

	@Test
	public void testIsSupportedWriteTransactionOperation() {
		assertThat(LdapTransactionUtils.isSupportedWriteTransactionOperation("bind")).isTrue();
		assertThat(LdapTransactionUtils.isSupportedWriteTransactionOperation("rebind")).isTrue();
		assertThat(LdapTransactionUtils.isSupportedWriteTransactionOperation("unbind")).isTrue();
		assertThat(LdapTransactionUtils.isSupportedWriteTransactionOperation("modifyAttributes")).isTrue();
		assertThat(LdapTransactionUtils.isSupportedWriteTransactionOperation("rename")).isTrue();
		assertThat(LdapTransactionUtils.isSupportedWriteTransactionOperation("lookup")).isFalse();
		assertThat(LdapTransactionUtils.isSupportedWriteTransactionOperation("search")).isFalse();
	}

	public void dummyMethod() {

	}

}
