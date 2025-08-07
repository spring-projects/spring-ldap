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

package org.springframework.ldap.core.support;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verifyNoMoreInteractions;

/**
 * @author Mattias Hellborg Arthursson
 */
public class SingleContextSourceTests {

	private ContextSource contextSourceMock;

	private DirContext dirContextMock;

	@BeforeEach
	public void prepareMocks() {
		this.contextSourceMock = mock(ContextSource.class);
		this.dirContextMock = mock(DirContext.class);
	}

	@Test
	public void testDoWithSingleContext() {
		given(this.contextSourceMock.getReadWriteContext()).willReturn(this.dirContextMock);
		verifyNoMoreInteractions(this.contextSourceMock);

		SingleContextSource.doWithSingleContext(this.contextSourceMock, new LdapOperationsCallback<>() {
			@Override
			public Object doWithLdapOperations(LdapOperations operations) {
				operations.executeReadOnly(new ContextExecutor<>() {
					@Override
					public Object executeWithContext(DirContext ctx) throws NamingException {
						Object targetContex = getInternalState(Proxy.getInvocationHandler(ctx), "target");
						assertThat(targetContex).isSameAs(SingleContextSourceTests.this.dirContextMock);
						return false;
					}
				});

				// Second operation will have retrieved new DirContext from the
				// SingleContextSource.
				// It should be the same instance.
				operations.executeReadOnly(new ContextExecutor<>() {
					@Override
					public Object executeWithContext(DirContext ctx) throws NamingException {
						Object targetContex = getInternalState(Proxy.getInvocationHandler(ctx), "target");
						assertThat(targetContex).isSameAs(SingleContextSourceTests.this.dirContextMock);
						return false;
					}
				});

				return null;
			}
		});
	}

	private <T> T getInternalState(Object target, String fieldName) {
		Field field = ReflectionUtils.findField(target.getClass(), fieldName);
		field.setAccessible(true);
		return (T) ReflectionUtils.getField(field, target);
	}

}
