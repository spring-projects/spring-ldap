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

import org.junit.Before;
import org.junit.Test;

import javax.naming.NameClassPair;
import javax.naming.NamingException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;

public class CollectingNameClassPairCallbackHandlerTests {

	private CollectingNameClassPairCallbackHandler tested;

	private Object expectedResult;

	private NameClassPair expectedNameClassPair;

	@Before
	public void setUp() throws Exception {
		this.expectedResult = new Object();
		this.expectedNameClassPair = new NameClassPair(null, null);
		this.tested = new CollectingNameClassPairCallbackHandler() {
			public Object getObjectFromNameClassPair(NameClassPair nameClassPair) {
				assertThat(nameClassPair)
						.isSameAs(CollectingNameClassPairCallbackHandlerTests.this.expectedNameClassPair);
				return CollectingNameClassPairCallbackHandlerTests.this.expectedResult;
			}
		};
	}

	@Test
	public void testHandleNameClassPair() throws NamingException {
		this.tested.handleNameClassPair(this.expectedNameClassPair);
		List result = this.tested.getList();
		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isSameAs(this.expectedResult);
	}

}
