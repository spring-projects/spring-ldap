/*
 * Copyright 2005-2013 the original author or authors.
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.LdapConditionallyFilteredTestRunner;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.naming.Name;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@DirtiesContext
@RunWith(LdapConditionallyFilteredTestRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
public abstract class AbstractLdapTemplateIntegrationTest {

	private final static String DEFAULT_BASE = "dc=261consulting,dc=com";

	@Autowired
	@Qualifier("contextSource")
	protected ContextSource contextSource;

	@Value("${base}")
	protected String base;

	@Before
	public void cleanAndSetup() throws NamingException, IOException {
		Resource ldifResource = getLdifFileResource();
		if (!LdapUtils.newLdapName(base).equals(LdapUtils.newLdapName(DEFAULT_BASE))) {
			List<String> lines = IOUtils.readLines(ldifResource.getInputStream());

			StringWriter sw = new StringWriter();
			PrintWriter writer = new PrintWriter(sw);
			for (String line : lines) {
				writer.println(StringUtils.replace(line, DEFAULT_BASE, base));
			}

			writer.flush();
			ldifResource = new ByteArrayResource(sw.toString().getBytes("UTF8"));
		}

		LdapTestUtils.cleanAndSetup(contextSource, getRoot(), ldifResource);
	}

	protected Resource getLdifFileResource() {
		return new ClassPathResource("/setup_data.ldif");
	}

	protected Name getRoot() {
		return LdapUtils.emptyLdapName();
	}

}
