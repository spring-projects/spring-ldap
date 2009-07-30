/*
 * Copyright 2005-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.core.simple;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.naming.Binding;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.HasControls;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.springframework.ldap.core.ObjectRetrievalException;

/**
 * Unit tests for the {@link ContextMapperCallbackHandlerWithControlsTest}
 * class.
 * 
 * @author Ulrik Sandberg
 */
@RunWith(JUnit4ClassRunner.class)
public class ContextMapperCallbackHandlerWithControlsTest extends TestCase {

	private ParameterizedContextMapperWithControls<Object> mapperMock;

	private ContextMapperCallbackHandlerWithControls tested;

	private static class MyBindingThatHasControls extends Binding implements HasControls {
		private static final long serialVersionUID = 1L;

		public MyBindingThatHasControls(String name, Object obj) {
			super(name, obj);
		}

		public Control[] getControls() throws NamingException {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		super.setUp();

		mapperMock = createMock(ParameterizedContextMapperWithControls.class);
		tested = new ContextMapperCallbackHandlerWithControls(mapperMock);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();

		mapperMock = null;
		tested = null;
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithEmptyArgument() {
		new ContextMapperCallbackHandlerWithControls(null);
	}

	@Test
	public void testGetObjectFromNameClassPair() {
		Object expectedObject = "object";
		Object expectedResult = "result";
		Binding expectedBinding = new Binding("some name", expectedObject);

		expect(mapperMock.mapFromContext(expectedObject)).andReturn(expectedResult);

		replay(mapperMock);
		Object actualResult = tested.getObjectFromNameClassPair(expectedBinding);
		verify(mapperMock);

		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void testGetObjectFromNameClassPairImplementingHasControls() {
		Object expectedObject = "object";
		Object expectedResult = "result";
		MyBindingThatHasControls expectedBinding = new MyBindingThatHasControls("some name", expectedObject);

		expect(mapperMock.mapFromContextWithControls(expectedObject, expectedBinding)).andReturn(expectedResult);

		replay(mapperMock);
		Object actualResult = tested.getObjectFromNameClassPair(expectedBinding);
		verify(mapperMock);

		assertEquals(expectedResult, actualResult);
	}

	@Test(expected = ObjectRetrievalException.class)
	public void testGetObjectFromNameClassPairObjectRetrievalException() {
		Binding expectedBinding = new Binding("some name", null);

		replay(mapperMock);
		tested.getObjectFromNameClassPair(expectedBinding);
		verify(mapperMock);
	}
}
