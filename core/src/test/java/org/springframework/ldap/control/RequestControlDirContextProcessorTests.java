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

package org.springframework.ldap.control;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

public class RequestControlDirContextProcessorTests {

	private AbstractRequestControlDirContextProcessor tested;

	private Control requestControlMock;

	private Control requestControl2Mock;

	private LdapContext ldapContextMock;

	private DirContext dirContextMock;

	@Before
	public void setUp() throws Exception {
		// Create requestControl mock
		this.requestControlMock = mock(Control.class);

		// Create requestControl2 mock
		this.requestControl2Mock = mock(Control.class);

		// Create ldapContext mock
		this.ldapContextMock = mock(LdapContext.class);

		// Create dirContext mock
		this.dirContextMock = mock(DirContext.class);

		this.tested = new AbstractRequestControlDirContextProcessor() {

			public Control createRequestControl() {
				return RequestControlDirContextProcessorTests.this.requestControlMock;
			}

			public void postProcess(DirContext ctx) throws NamingException {
			}

		};
	}

	@After
	public void tearDown() throws Exception {
		this.requestControlMock = null;
		this.requestControl2Mock = null;
		this.ldapContextMock = null;
		this.dirContextMock = null;

	}

	@Test
	public void testPreProcessWithExistingControlOfDifferentClassShouldAdd() throws Exception {
		SortControl existingControl = new SortControl(new String[] { "cn" }, true);
		given(this.ldapContextMock.getRequestControls()).willReturn(new Control[] { existingControl });

		this.tested.preProcess(this.ldapContextMock);

		verify(this.ldapContextMock).setRequestControls(new Control[] { existingControl, this.requestControlMock });
	}

	@Test
	public void testPreProcessWithExistingControlOfSameClassShouldReplace() throws Exception {
		given(this.ldapContextMock.getRequestControls()).willReturn(new Control[] { this.requestControl2Mock });

		this.tested.preProcess(this.ldapContextMock);

		verify(this.ldapContextMock).setRequestControls(new Control[] { this.requestControlMock });
	}

	@Test
	public void testPreProcessWithExistingControlOfSameClassAndPropertyFalseShouldAdd() throws Exception {
		given(this.ldapContextMock.getRequestControls()).willReturn(new Control[] { this.requestControl2Mock });

		this.tested.setReplaceSameControlEnabled(false);
		this.tested.preProcess(this.ldapContextMock);

		verify(this.ldapContextMock)
			.setRequestControls(new Control[] { this.requestControl2Mock, this.requestControlMock });
	}

	@Test
	public void testPreProcessWithNoExistingControlsShouldAdd() throws NamingException {
		given(this.ldapContextMock.getRequestControls()).willReturn(new Control[0]);

		this.tested.preProcess(this.ldapContextMock);

		verify(this.ldapContextMock).setRequestControls(new Control[] { this.requestControlMock });
	}

	@Test
	public void testPreProcessWithNullControlsShouldAdd() throws NamingException {
		given(this.ldapContextMock.getRequestControls()).willReturn(null);

		this.tested.preProcess(this.ldapContextMock);

		verify(this.ldapContextMock).setRequestControls(new Control[] { this.requestControlMock });
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPreProcessWhenNotLdapContextShouldFail() throws Exception {
		this.tested.preProcess(this.dirContextMock);
	}

}
