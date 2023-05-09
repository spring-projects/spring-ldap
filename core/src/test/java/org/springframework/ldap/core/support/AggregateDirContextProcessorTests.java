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

package org.springframework.ldap.core.support;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.core.DirContextProcessor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AggregateDirContextProcessorTests {

	private DirContextProcessor processor1Mock;

	private DirContextProcessor processor2Mock;

	private AggregateDirContextProcessor tested;

	@Before
	public void setUp() throws Exception {
		// Create processor1 mock
		this.processor1Mock = mock(DirContextProcessor.class);

		// Create processor2 mock
		this.processor2Mock = mock(DirContextProcessor.class);

		this.tested = new AggregateDirContextProcessor();
		this.tested.addDirContextProcessor(this.processor1Mock);
		this.tested.addDirContextProcessor(this.processor2Mock);

	}

	@Test
	public void testPreProcess() throws NamingException {
		this.tested.preProcess(null);

		verify(this.processor1Mock).preProcess(null);
		verify(this.processor2Mock).preProcess(null);
	}

	@Test
	public void testPostProcess() throws NamingException {
		this.tested.postProcess(null);

		verify(this.processor1Mock).postProcess(null);
		verify(this.processor2Mock).postProcess(null);
	}

}
