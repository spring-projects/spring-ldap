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

package org.springframework.ldap.core.support;

import javax.naming.directory.SearchResult;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CountNameClassPairResultCallbackHandlerTests {

	private CountNameClassPairCallbackHandler tested;

	@Before
	public void setUp() throws Exception {
		this.tested = new CountNameClassPairCallbackHandler();
	}

	@Test
	public void testHandleSearchResult() throws Exception {
		SearchResult dummy = new SearchResult(null, null, null);
		this.tested.handleNameClassPair(dummy);
		this.tested.handleNameClassPair(dummy);
		this.tested.handleNameClassPair(dummy);

		assertThat(this.tested.getNoOfRows()).isEqualTo(3);
	}

}
