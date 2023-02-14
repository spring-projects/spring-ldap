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

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.control.SortControlDirContextProcessor;
import org.springframework.ldap.core.support.AbstractContextSource;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;

/**
 * A {@link RuntimeHintsRegistrar} for LDAP Core classes
 *
 * @author Marcus Da Coregio
 * @since 3.0
 */
class LdapCoreRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		hints.reflection().registerType(TypeReference.of("com.sun.jndi.ldap.LdapCtxFactory"),
				(builder) -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerType(AbstractContextSource.class, (builder) -> builder
				.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(DefaultDirObjectFactory.class,
				(builder) -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerTypeIfPresent(classLoader, "javax.naming.ldap.PagedResultsControl",
				(builder) -> builder.onReachableType(PagedResultsDirContextProcessor.class)
						.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerTypeIfPresent(classLoader, "com.sun.jndi.ldap.ctl.PagedResultsControl",
				(builder) -> builder.onReachableType(PagedResultsDirContextProcessor.class)
						.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerTypeIfPresent(classLoader, "javax.naming.ldap.PagedResultsResponseControl",
				(builder) -> builder.onReachableType(PagedResultsDirContextProcessor.class)
						.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerTypeIfPresent(classLoader, "com.sun.jndi.ldap.ctl.PagedResultsResponseControl",
				(builder) -> builder.onReachableType(PagedResultsDirContextProcessor.class)
						.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerTypeIfPresent(classLoader, "javax.naming.ldap.SortControl",
				(builder) -> builder.onReachableType(SortControlDirContextProcessor.class)
						.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerTypeIfPresent(classLoader, "com.sun.jndi.ldap.ctl.SortControl",
				(builder) -> builder.onReachableType(SortControlDirContextProcessor.class)
						.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerTypeIfPresent(classLoader, "javax.naming.ldap.SortResponseControl",
				(builder) -> builder.onReachableType(SortControlDirContextProcessor.class)
						.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
		hints.reflection().registerTypeIfPresent(classLoader, "com.sun.jndi.ldap.ctl.SortResponseControl",
				(builder) -> builder.onReachableType(SortControlDirContextProcessor.class)
						.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
	}

}
