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

package org.springframework.ldap.itest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the ContextMapper search method. In its way this method also
 * demonstrates the use of DirContextAdapter and the DirObjectFactory.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateContextMapperITest extends AbstractLdapTemplateIntegrationTest {
	
	@Autowired
	private LdapTemplate tested;

	/**
	 * This method depends on a DirObjectFactory ({@link org.springframework.ldap.core.support.DefaultDirObjectFactory})
	 * being set in the ContextSource.
	 */
	@Test
	public void testSearch_ContextMapper() {
		ContextMapper mapper = new PersonContextMapper();
		List result = tested.search("ou=company1,ou=Sweden", "(&(objectclass=person)(sn=Person2))", mapper);

		assertThat(result).hasSize(1);
		Person person = (Person) result.get(0);
		assertThat(person.getFullname()).isEqualTo("Some Person2");
		assertThat(person.getLastname()).isEqualTo("Person2");
		assertThat(person.getDescription()).isEqualTo("Sweden, Company1, Some Person2");
	}

	/**
	 * Demonstrates how to retrieve all values of a multi-value attribute.
	 * 
	 * @see LdapTemplateAttributesMapperITest#testSearch_AttributesMapper_MultiValue()
	 */
	@Test
	public void testSearch_ContextMapper_MultiValue() throws Exception {
		ContextMapper mapper = new ContextMapper() {
			public Object mapFromContext(Object ctx) {
				DirContextAdapter adapter = (DirContextAdapter) ctx;
				String[] members = adapter.getStringAttributes("uniqueMember");
				return members;
			}
		};
		List result = tested.search("ou=groups", "(&(objectclass=groupOfUniqueNames)(cn=ROLE_USER))", mapper);

		assertThat(result).hasSize(1);
		assertThat(((String[]) result.get(0)).length).isEqualTo(4);
	}
}
