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

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextExecutor;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for LdapTemplate's context executor methods.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
public class LdapTemplateContextExecutorTest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	@Test
	public void testLookupLink() {
		ContextExecutor executor = new ContextExecutor() {
			public Object executeWithContext(DirContext ctx) throws NamingException {
				return ctx.lookupLink("cn=Some Person,ou=company1,ou=Sweden");
			}
		};

		Object object = tested.executeReadOnly(executor);
		assertThat(object instanceof DirContextAdapter).as("Should be a DirContextAdapter").isTrue();
	}
}
