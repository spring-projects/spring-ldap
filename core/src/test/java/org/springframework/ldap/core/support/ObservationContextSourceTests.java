/*
 * Copyright 2005-2025 the original author or authors.
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

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ObservationContextSourceTests {

	private final TestObservationRegistry registry = TestObservationRegistry.create();

	@Test
	public void dirContextGetAttributesWhenObservingThenObserves() throws Exception {
		TestContextSource observed = new TestContextSource();
		ObservationContextSource observing = new ObservationContextSource(observed, this.registry);
		observing.getReadOnlyContext().getAttributes("ou=user,ou=people");
		// @formatter:off
		TestObservationRegistryAssert.assertThat(this.registry)
			.hasObservationWithNameEqualTo("spring.ldap.dir.context.operations").that()
			.hasContextualNameEqualTo("perform get.attributes");
		// @formatter:on
	}

	private final class TestContextSource extends AbstractContextSource {

		TestContextSource() {
			setUrls(new String[] { "ldap://localhost:1234" });
			setBase("dc=example,dc=org");
			afterPropertiesSet();
		}

		@Override
		protected DirContext getDirContextInstance(Hashtable<String, Object> environment) throws NamingException {
			return mock(DirContext.class);
		}

	}

}
