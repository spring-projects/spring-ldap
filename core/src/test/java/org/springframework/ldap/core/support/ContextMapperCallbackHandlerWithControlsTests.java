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

import javax.naming.Binding;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.HasControls;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.ObjectRetrievalException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

/**
 * @Ulrik Sandberg
 * @author Mattias Hellborg Arthursson
 */
public class ContextMapperCallbackHandlerWithControlsTests {

	private ContextMapperWithControls mapperMock;

	private ContextMapperCallbackHandlerWithControls tested;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		this.mapperMock = mock(ContextMapperWithControls.class);
		this.tested = new ContextMapperCallbackHandlerWithControls(this.mapperMock);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithEmptyArgument() {
		new ContextMapperCallbackHandlerWithControls<Object>(null);
	}

	@Test
	public void testGetObjectFromNameClassPair() throws NamingException {
		Object expectedObject = "object";
		Object expectedResult = "result";
		Binding expectedBinding = new Binding("some name", expectedObject);

		given(this.mapperMock.mapFromContext(expectedObject)).willReturn(expectedResult);

		Object actualResult = this.tested.getObjectFromNameClassPair(expectedBinding);

		assertThat(actualResult).isEqualTo(expectedResult);
	}

	@Test
	public void testGetObjectFromNameClassPairImplementingHasControls() throws NamingException {
		Object expectedObject = "object";
		Object expectedResult = "result";
		MyBindingThatHasControls expectedBinding = new MyBindingThatHasControls("some name", expectedObject);

		given(this.mapperMock.mapFromContextWithControls(expectedObject, expectedBinding)).willReturn(expectedResult);

		Object actualResult = this.tested.getObjectFromNameClassPair(expectedBinding);

		assertThat(actualResult).isEqualTo(expectedResult);
	}

	@Test(expected = ObjectRetrievalException.class)
	public void testGetObjectFromNameClassPairObjectRetrievalException() throws NamingException {
		Binding expectedBinding = new Binding("some name", null);

		this.tested.getObjectFromNameClassPair(expectedBinding);
	}

	private static class MyBindingThatHasControls extends Binding implements HasControls {

		private static final long serialVersionUID = 1L;

		MyBindingThatHasControls(String name, Object obj) {
			super(name, obj);
		}

		@Override
		public Control[] getControls() throws NamingException {
			return null;
		}

	}

}
