/*
 * Copyright 2005-2013 the original author or authors.
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

import static junit.framework.Assert.assertEquals;

/**
 * Tests the lookup methods of LdapTemplate together with Spring 3.0.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapTemplateTestContext.xml"})
@DirtiesContext
public class LdapTemplateLookup30ITest extends AbstractJUnit4SpringContextTests {

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

		assertEquals("Some Person2", result.getStringAttribute("cn"));
		assertEquals("Person2", result.getStringAttribute("sn"));
		assertEquals("Sweden, Company1, Some Person2", result.getStringAttribute("description"));
	}
}
