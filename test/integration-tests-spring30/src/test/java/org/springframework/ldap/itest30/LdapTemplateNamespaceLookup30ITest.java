/*
 * Copyright 2005-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.itest30;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the lookup methods of LdapTemplate together with Spring 3.0.
 *
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateNamespaceTestContext.xml"})
@DirtiesContext
public class LdapTemplateNamespaceLookup30ITest extends AbstractJUnit4SpringContextTests {

    @Autowired
	private LdapTemplate tested;

    /**
	 * This method depends on a DirObjectFactory (
	 * {@link org.springframework.ldap.core.support.DefaultDirObjectFactory})
	 * being set in the ContextSource.
	 */
    @Test
	public void testThatPlainLookupWorksWithSpring30() {
		DirContextOperations result = tested.lookupContext("cn=Some Person2, ou=company1,c=Sweden");

		assertThat(result.getStringAttribute("cn")).isEqualTo("Some Person2");
		assertThat(result.getStringAttribute("sn")).isEqualTo("Person2");
		assertThat(result.getStringAttribute("description")).isEqualTo("Sweden, Company1, Some Person2");
	}
}
