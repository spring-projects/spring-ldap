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

package org.springframework.ldap.core;

import javax.naming.Binding;
import javax.naming.NamingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

public class ContextMapperCallbackHandlerTests {

	private ContextMapper mapperMock;

	private ContextMapperCallbackHandler tested;

	@BeforeEach
	public void setUp() throws Exception {
		this.mapperMock = mock(ContextMapper.class);
		this.tested = new ContextMapperCallbackHandler(this.mapperMock);
	}

	@Test
	public void testConstructorWithEmptyArgument() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new ContextMapperCallbackHandler(null));
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
	public void testGetObjectFromNameClassPairObjectRetrievalException() {
		assertThatExceptionOfType(ObjectRetrievalException.class).isThrownBy(() -> {
			Binding expectedBinding = new Binding("some name", null);
			this.tested.getObjectFromNameClassPair(expectedBinding);
		});
	}

}
