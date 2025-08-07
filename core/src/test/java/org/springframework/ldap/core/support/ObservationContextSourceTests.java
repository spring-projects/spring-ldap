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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.stream.Stream;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;

import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.support.ModifierSupport;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ObservationContextSource}
 */
public class ObservationContextSourceTests {

	private final TestObservationRegistry registry = TestObservationRegistry.create();

	private final ObservationContextSource dir = new ObservationContextSource(new TestContextSource(), this.registry);

	private final ObservationContextSource ldap = new ObservationContextSource(new TestContextSource(true),
			this.registry);

	private final DirContext dirCtx = this.dir.getReadWriteContext();

	private final LdapContext ldapCtx = (LdapContext) this.ldap.getReadWriteContext();

	@TestFactory
	Stream<DynamicTest> confirmObservabilityOfEachLdapContextMethod() {
		return Arrays.stream(this.ldapCtx.getClass().getDeclaredMethods())
			.filter(ModifierSupport::isPublic)
			.map((method) -> DynamicTest.dynamicTest(methodSignature(method), () -> {
				this.registry.clear();
				Object[] args = Arrays.stream(method.getParameterTypes()).map(this::fuzzValue).toArray();
				method.invoke(this.ldapCtx, args);
			// @formatter:off
				TestObservationRegistryAssert.assertThat(this.registry)
					.hasObservationWithNameEqualTo("spring.ldap.dir.context.operations").that()
					.hasContextualNameEqualTo("perform " + method.getName())
					.hasBeenStarted().hasBeenStopped();
				// @formatter:on
			}));
	}

	@TestFactory
	Stream<DynamicTest> confirmObservabilityOfEachDirContextMethod() {
		return Arrays.stream(this.dirCtx.getClass().getDeclaredMethods())
			.filter(ModifierSupport::isPublic)
			.map((method) -> DynamicTest.dynamicTest(methodSignature(method), () -> {
				this.registry.clear();
				Object[] args = Arrays.stream(method.getParameterTypes()).map(this::fuzzValue).toArray();
				method.invoke(this.dirCtx, args);
			// @formatter:off
				TestObservationRegistryAssert.assertThat(this.registry)
					.hasObservationWithNameEqualTo("spring.ldap.dir.context.operations").that()
					.hasContextualNameEqualTo("perform " + method.getName())
					.hasBeenStarted().hasBeenStopped();
				// @formatter:on
			}));
	}

	String methodSignature(Method method) {
		return method.getName() + Arrays.toString(method.getParameterTypes());
	}

	Object fuzzValue(Class<?> type) {
		if (type == String.class) {
			return "";
		}
		if (type == Name.class) {
			return LdapUtils.emptyLdapName();
		}
		if (type == int.class) {
			return 1;
		}
		if (type == Attributes.class) {
			return new BasicAttributes();
		}
		if (type.isArray()) {
			return Array.newInstance(type.componentType(), 0);
		}
		if (type == ExtendedRequest.class) {
			return new StartTlsRequest();
		}
		return null;
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
	void constructorWhenObservationContextSourceThenIllegalArgument() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new ObservationContextSource(this.dir, this.registry));
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
