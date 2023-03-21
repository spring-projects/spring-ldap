package org.springframework.ldap.support;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapNameBuilderTest {

	@Test
	public void testAddComponentToEmpty() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance().add("dc", "com").add("dc", "261consulting");
		assertThat(tested.build().toString()).isEqualTo("dc=261consulting,dc=com");
	}

	@Test
	public void testAddComponentToBaseString() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance("dc=com").add("dc", "261consulting");
		assertThat(tested.build().toString()).isEqualTo("dc=261consulting,dc=com");
	}

	@Test
	public void testAddComponentToBaseName() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance(LdapUtils.newLdapName("dc=com")).add("dc",
				"261consulting");
		assertThat(tested.build().toString()).isEqualTo("dc=261consulting,dc=com");
	}

	@Test
	public void testAddStringNameToBaseString() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance("dc=261consulting,dc=com").add("ou=people");
		assertThat(tested.build().toString()).isEqualTo("ou=people,dc=261consulting,dc=com");
	}

	@Test
	public void testAddNameToBaseString() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance("dc=261consulting,dc=com")
				.add(LdapUtils.newLdapName("ou=people"));
		assertThat(tested.build().toString()).isEqualTo("ou=people,dc=261consulting,dc=com");
	}

	@Test
	public void testAddNameToEmpty() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance().add(LdapUtils.newLdapName("ou=people"));
		assertThat(tested.build().toString()).isEqualTo("ou=people");
	}

	@Test
	public void testAddEmptyToEmpty() {
		LdapNameBuilder tested = LdapNameBuilder.newInstance().add("");
		assertThat(tested.build().toString()).isEqualTo("");
	}

}
