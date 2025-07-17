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

package org.springframework.ldap.aot.hint;

import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortResponseControl;
import javax.net.ssl.SSLSocketFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.ldap.core.support.AbstractContextSource;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LdapCoreRuntimeHints}
 */
public class LdapCoreRuntimeHintsTests {

	private final RuntimeHints hints = new RuntimeHints();

	@BeforeEach
	public void setup() {
		SpringFactoriesLoader.forResourceLocation("META-INF/spring/aot.factories")
			.load(RuntimeHintsRegistrar.class)
			.forEach((registrar) -> registrar.registerHints(this.hints, ClassUtils.getDefaultClassLoader()));
	}

	@Test
	public void ldapCtxFactoryHasHints() {
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(TypeReference.of("com.sun.jndi.ldap.LdapCtxFactory"))
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)).accepts(this.hints);
	}

	@Test
	public void abstractContextSourceHasHints() {
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(AbstractContextSource.class)
			.withMemberCategories(MemberCategory.INTROSPECT_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS))
			.accepts(this.hints);
	}

	@Test
	public void defaultDirObjectFactoryHasHints() {
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(DefaultDirObjectFactory.class)
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)).accepts(this.hints);
	}

	@Test
	public void pagedResultsControlHasHints() {
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(PagedResultsControl.class)
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS))
			.accepts(this.hints);
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(com.sun.jndi.ldap.ctl.PagedResultsControl.class)
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS))
			.accepts(this.hints);
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(PagedResultsResponseControl.class)
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS))
			.accepts(this.hints);
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(com.sun.jndi.ldap.ctl.PagedResultsResponseControl.class)
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS))
			.accepts(this.hints);
	}

	@Test
	public void sortControlHasHints() {
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(SortControl.class)
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS))
			.accepts(this.hints);
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(com.sun.jndi.ldap.ctl.SortControl.class)
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS))
			.accepts(this.hints);
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(SortResponseControl.class)
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS))
			.accepts(this.hints);
		assertThat(RuntimeHintsPredicates.reflection()
			.onType(com.sun.jndi.ldap.ctl.SortResponseControl.class)
			.withMemberCategories(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS))
			.accepts(this.hints);
	}

	@Test
	public void sslSocketFactoryHasHints() throws Exception {
		assertThat(RuntimeHintsPredicates.reflection().onMethod(SSLSocketFactory.class.getDeclaredMethod("getDefault")))
			.accepts(this.hints);
	}

}
