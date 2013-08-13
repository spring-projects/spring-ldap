/*
 * Copyright 2005-2010 the original author or authors.
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
package org.springframework.ldap;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.naming.NamingException;
import java.io.IOException;

@DirtiesContext
public abstract class AbstractLdapTemplateIntegrationTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	@Qualifier("contextSource")
	protected ContextSource contextSource;

	@Before
	public void cleanAndSetup() throws NamingException, IOException {
        LdapTestUtils.cleanAndSetup(contextSource, getRoot(), getLdifFileResource());
    }

    protected Resource getLdifFileResource() {
        return new ClassPathResource("/setup_data.ldif");
    }

    protected DistinguishedName getRoot() {
		return DistinguishedName.EMPTY_PATH;
	}
}
