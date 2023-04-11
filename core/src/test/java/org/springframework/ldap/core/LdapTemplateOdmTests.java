/*
 * Copyright 2005-2023 the original author or authors.
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

package org.springframework.ldap.core;

import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.odm.core.ObjectDirectoryMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapTemplateOdmTests {

	private LdapTemplate tested;

	private ObjectDirectoryMapper odmMock;

	@Before
	public void prepareTestedClass() {
		this.tested = mock(LdapTemplate.class);

		doCallRealMethod().when(this.tested).setObjectDirectoryMapper(any(ObjectDirectoryMapper.class));
		this.odmMock = mock(ObjectDirectoryMapper.class);

		this.tested.setObjectDirectoryMapper(this.odmMock);
	}

	@Test
	public void testFindByDn() {

	}

}
