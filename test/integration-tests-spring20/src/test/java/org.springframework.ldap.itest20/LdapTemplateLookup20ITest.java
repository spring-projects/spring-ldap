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

package org.springframework.ldap.itest20;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the lookup methods of LdapTemplate together with Spring 2.0.
 * 
 * @author Mattias Hellborg Arthursson
 */
public class LdapTemplateLookup20ITest extends AbstractDependencyInjectionSpringContextTests {

	private LdapTemplate tested;

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"/conf/ldapTemplateTestContext.xml"};
    }

    /**
	 * This method depends on a DirObjectFactory (
	 * {@link org.springframework.ldap.core.support.DefaultDirObjectFactory})
	 * being set in the ContextSource.
	 */
	public void testThatPlainLookupWorksWithSpring20() {
		DirContextOperations result = tested.lookupContext("cn=Some Person2, ou=company1,c=Sweden");

		assertEquals("Some Person2", result.getStringAttribute("cn"));
		assertEquals("Person2", result.getStringAttribute("sn"));
		assertEquals("Sweden, Company1, Some Person2", result.getStringAttribute("description"));
	}
}
