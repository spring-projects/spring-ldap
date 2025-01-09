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
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;

import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import org.junit.jupiter.api.Test;

import org.springframework.ldap.core.DirContextOperations;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ObservationContextSource}
 */
public class ObservationContextSourceTests {

	private final TestObservationRegistry registry = TestObservationRegistry.create();

	private final ObservationContextSource contextSource = new ObservationContextSource(new TestContextSource(),
			this.registry);

	@Test
	void dirContextGetAttributesWhenObservingThenObserves() throws Exception {
		this.contextSource.getReadOnlyContext().getAttributes("ou=user,ou=people");
		// @formatter:off
		TestObservationRegistryAssert.assertThat(this.registry)
			.hasObservationWithNameEqualTo("spring.ldap.dir.context.operations").that()
			.hasContextualNameEqualTo("perform get.attributes");
		// @formatter:on
	}

	@Test
	void dirContextGetAttributesByNameWhenObservingThenObserves() throws Exception {
		this.contextSource.getReadOnlyContext().getAttributes("ou=user,ou=people", new String[] { "id" });
		// @formatter:off
		TestObservationRegistryAssert.assertThat(this.registry)
			.hasObservationWithNameEqualTo("spring.ldap.dir.context.operations").that()
			.hasContextualNameEqualTo("perform get.attributes")
			.hasHighCardinalityKeyValue("attribute.ids", "[id]");
		// @formatter:on
	}

	@Test
	void dirContextRenameWhenObservingThenObserves() throws Exception {
		this.contextSource.getReadOnlyContext().rename("ou=user,ou=people", "ou=carrot,ou=people");
		// @formatter:off
		TestObservationRegistryAssert.assertThat(this.registry)
			.hasObservationWithNameEqualTo("spring.ldap.dir.context.operations").that()
			.hasContextualNameEqualTo("perform rename");
		// @formatter:on
	}

	@Test
	void dirContextWhenDirContextOperationsThenDoesNotObserve() throws Exception {
		BaseLdapPathContextSource contextSource = mock(BaseLdapPathContextSource.class);
		ObservationContextSource observing = new ObservationContextSource(contextSource, this.registry);
		DirContextOperations operations = mock(DirContextOperations.class);
		given(contextSource.getReadOnlyContext()).willReturn(operations);
		observing.getReadOnlyContext().getAttributes("ou=user,ou=people");
		// @formatter:off
		TestObservationRegistryAssert.assertThat(this.registry)
			.hasNumberOfObservationsEqualTo(0);
		// @formatter:on
	}

	@Test
	void ldapContextWhenExtendedOperationThenObserves() throws Exception {
		ObservationContextSource observing = new ObservationContextSource(new TestContextSource(true), this.registry);
		((LdapContext) observing.getReadOnlyContext()).extendedOperation(new StartTlsRequest());
		// @formatter:off
		TestObservationRegistryAssert.assertThat(this.registry)
			.hasObservationWithNameEqualTo("spring.ldap.dir.context.operations").that()
			.hasContextualNameEqualTo("perform extended.operation");
		// @formatter:on
	}

	@Test
	void constructorWhenObservationContextSourceThenIllegalArgument() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new ObservationContextSource(this.contextSource, this.registry));
	}

	@Test
	void constructorWhenNullParametersThenIllegalArgumment() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new ObservationContextSource(null, this.registry));
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new ObservationContextSource(new TestContextSource(), null));
	}

	private static final class TestContextSource extends AbstractContextSource {

		private final boolean ldapContext;

		TestContextSource() {
			this(false);
		}

		TestContextSource(boolean ldapContext) {
			this.ldapContext = ldapContext;
			setUrls(new String[] { "ldap://localhost:1234" });
			setBase("dc=example,dc=org");
			afterPropertiesSet();
		}

		@Override
		protected DirContext getDirContextInstance(Hashtable<String, Object> environment) throws NamingException {
			return this.ldapContext ? mock(LdapContext.class) : mock(DirContext.class);
		}

	}

}
