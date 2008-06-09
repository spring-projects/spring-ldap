/*
 * Copyright 2005-2007 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

public abstract class AbstractLdapTemplateIntegrationTest extends AbstractJUnit4SpringContextTests {
	private static final Log log = LogFactory.getLog(AbstractLdapTemplateIntegrationTest.class);

	@AfterClass
	public static final void shutdownServer() {
		try {
			// This would be the ideal setup, to enable each test to run in
			// isolation
			// However, due to port timeout problems on linux it doesn't work -
			// the port will not be released before the next test class will
			// start.
			// LdapTestUtils.destroyApacheDirectoryServer("uid=admin,ou=system",
			// "credentials");
		}
		catch (Exception e) {
			log.error("Failed to shut down directory server");
		}
	}
}
