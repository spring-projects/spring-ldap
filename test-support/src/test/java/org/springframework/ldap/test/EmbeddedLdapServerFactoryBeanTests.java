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

package org.springframework.ldap.test;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddedLdapServerFactoryBeanTests {

	@Test
	void testServerStartup() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/applicationContext.xml");
		LdapTemplate ldapTemplate = ctx.getBean(LdapTemplate.class);
		assertThat(ldapTemplate).isNotNull();

		List<String> list = ldapTemplate.search(LdapQueryBuilder.query().where("objectclass").is("person"),
				this::commonNameAttribute);
		assertThat(list).hasSize(5);
	}

	private String commonNameAttribute(Attributes attrs) throws NamingException {
		return (String) attrs.get("cn").get();
	}

}
