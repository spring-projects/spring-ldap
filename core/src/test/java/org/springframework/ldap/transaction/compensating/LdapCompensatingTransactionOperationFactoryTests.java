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

import javax.naming.directory.DirContext;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.LdapOperations;
import org.springframework.transaction.compensating.CompensatingTransactionOperationRecorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LdapCompensatingTransactionOperationFactoryTests {

	private LdapOperations ldapOperationsMock;

	private TempEntryRenamingStrategy renamingStrategyMock;

	private DirContext dirContextMock;

	private LdapCompensatingTransactionOperationFactory tested;

	@Before
	public void setUp() throws Exception {
		this.ldapOperationsMock = mock(LdapOperations.class);
		this.renamingStrategyMock = mock(TempEntryRenamingStrategy.class);
		this.dirContextMock = mock(DirContext.class);

		this.tested = new LdapCompensatingTransactionOperationFactory(this.renamingStrategyMock) {

			LdapOperations createLdapOperationsInstance(DirContext ctx) {
				assertThat(ctx).isEqualTo(LdapCompensatingTransactionOperationFactoryTests.this.dirContextMock);
				return LdapCompensatingTransactionOperationFactoryTests.this.ldapOperationsMock;
			}
		};
	}

	@Test
	public void testGetRecordingOperation_Bind() throws Exception {

		CompensatingTransactionOperationRecorder result = this.tested.createRecordingOperation(this.dirContextMock,
				"bind");
		assertThat(result instanceof BindOperationRecorder).isTrue();
		BindOperationRecorder bindOperationRecorder = (BindOperationRecorder) result;
		assertThat(bindOperationRecorder.getLdapOperations()).isSameAs(this.ldapOperationsMock);
	}

	@Test
	public void testGetRecordingOperation_Rebind() throws Exception {
		CompensatingTransactionOperationRecorder result = this.tested.createRecordingOperation(this.dirContextMock,
				"rebind");
		assertThat(result instanceof RebindOperationRecorder).isTrue();
		RebindOperationRecorder rebindOperationRecorder = (RebindOperationRecorder) result;
		assertThat(rebindOperationRecorder.getLdapOperations()).isSameAs(this.ldapOperationsMock);
		assertThat(rebindOperationRecorder.getRenamingStrategy()).isSameAs(this.renamingStrategyMock);
	}

	@Test
	public void testGetRecordingOperation_Rename() throws Exception {
		CompensatingTransactionOperationRecorder result = this.tested.createRecordingOperation(this.dirContextMock,
				"rename");
		assertThat(result instanceof RenameOperationRecorder).isTrue();
		RenameOperationRecorder recordingOperation = (RenameOperationRecorder) result;
		assertThat(recordingOperation.getLdapOperations()).isSameAs(this.ldapOperationsMock);
	}

	@Test
	public void testGetRecordingOperation_ModifyAttributes() throws Exception {
		CompensatingTransactionOperationRecorder result = this.tested.createRecordingOperation(this.dirContextMock,
				"modifyAttributes");
		assertThat(result instanceof ModifyAttributesOperationRecorder).isTrue();
		ModifyAttributesOperationRecorder recordingOperation = (ModifyAttributesOperationRecorder) result;
		assertThat(recordingOperation.getLdapOperations()).isSameAs(this.ldapOperationsMock);
	}

	@Test
	public void testGetRecordingOperation_Unbind() throws Exception {
		CompensatingTransactionOperationRecorder result = this.tested.createRecordingOperation(this.dirContextMock,
				"unbind");
		assertThat(result instanceof UnbindOperationRecorder).isTrue();
		UnbindOperationRecorder recordingOperation = (UnbindOperationRecorder) result;
		assertThat(recordingOperation.getLdapOperations()).isSameAs(this.ldapOperationsMock);
		assertThat(recordingOperation.getRenamingStrategy()).isSameAs(this.renamingStrategyMock);
	}

}
