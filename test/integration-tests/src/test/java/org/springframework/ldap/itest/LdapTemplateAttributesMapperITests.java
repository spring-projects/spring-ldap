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

package org.springframework.ldap.itest;

import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the attributes mapper search method.
 *
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateAttributesMapperITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapTemplate tested;

	@Test
	public void testSearch_AttributeMapper() throws Exception {
		AttributesMapper mapper = new PersonAttributesMapper();
		List result = this.tested.search("ou=company1,ou=Sweden", "(&(objectclass=person)(sn=Person2))", mapper);

		assertThat(result).hasSize(1);
		Person person = (Person) result.get(0);
		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).isEqualTo("Person2");
		assertThat(person.getDescription()).isEqualTo("Sweden, Company1, Some Person2");
	}

	/**
	 * Demonstrates how to retrieve all values of a multi-value attribute.
	 *
	 * @see LdapTemplateContextMapperITest#testSearch_ContextMapper_MultiValue()
	 */
	@Test
	public void testSearch_AttributesMapper_MultiValue() throws Exception {
		AttributesMapper mapper = new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				LinkedList list = new LinkedList();
				NamingEnumeration enumeration = attributes.get("uniqueMember").getAll();
				while (enumeration.hasMoreElements()) {
					String value = (String) enumeration.nextElement();
					list.add(value);
				}
				String[] members = (String[]) list.toArray(new String[0]);
				return members;
			}
		};
		List result = this.tested.search("ou=groups", "(&(objectclass=groupOfUniqueNames)(cn=ROLE_USER))", mapper);

		assertThat(result).hasSize(1);

		assertThat(((String[]) result.get(0)).length).isEqualTo(4);
	}

}
