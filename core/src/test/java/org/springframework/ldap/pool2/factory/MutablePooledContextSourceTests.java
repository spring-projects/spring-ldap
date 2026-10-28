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

package org.springframework.ldap.pool2.factory;

import java.util.concurrent.atomic.AtomicReference;

import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;

import org.junit.jupiter.api.Test;

import org.springframework.ldap.pool2.AbstractPoolTestCase;
import org.springframework.ldap.pool2.MutableDelegatingLdapContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;

/**
 * Unit tests for the MutablePoolingContextSource class.
 *
 * @author Ulrik Sandberg
 * @author Anindya Chatterjee
 */
public class MutablePooledContextSourceTests extends AbstractPoolTestCase {

	@Test
	public void testGetReadOnlyLdapContext() throws Exception {

		given(contextSourceMock.getReadOnlyContext()).willReturn(ldapContextMock);

		final MutablePooledContextSource poolingContextSource = new MutablePooledContextSource(null);
		poolingContextSource.setContextSource(contextSourceMock);

		// Get a context
		final DirContext result = poolingContextSource.getReadOnlyContext();

		assertThat(result.getClass()).isEqualTo(MutableDelegatingLdapContext.class);
	}

	@Test
	public void testRequestControlsAreNotRetainedAfterContextIsReturnedToPool() throws Exception {
		AtomicReference<Control[]> connectionControls = new AtomicReference<>();
		willAnswer((invocation) -> {
			connectionControls.set(invocation.getArgument(0));
			return null;
		}).given(this.ldapContextMock).setRequestControls(any());
		given(this.ldapContextMock.getRequestControls()).willAnswer((invocation) -> connectionControls.get());
		given(this.contextSourceMock.getReadOnlyContext()).willReturn(this.ldapContextMock);

		MutablePooledContextSource poolingContextSource = new MutablePooledContextSource(new PoolConfig());
		poolingContextSource.setContextSource(this.contextSourceMock);

		LdapContext first = (LdapContext) poolingContextSource.getReadOnlyContext();
		first.setRequestControls(new Control[] { new PagedResultsControl(10, Control.CRITICAL) });
		assertThat(first.getRequestControls()).hasSize(1);
		first.close();

		LdapContext second = (LdapContext) poolingContextSource.getReadOnlyContext();
		assertThat(second.getRequestControls()).isNull();
	}

}
