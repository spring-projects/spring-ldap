package org.springframework.ldap.aot.hint;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
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
	}

}
