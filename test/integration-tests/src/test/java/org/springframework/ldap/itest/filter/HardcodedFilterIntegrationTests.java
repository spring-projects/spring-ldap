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
package org.springframework.ldap.itest.filter;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.CountNameClassPairCallbackHandler;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTests;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/hardcodedFilterTestContext.xml", "/conf/ldapTemplateTestContext.xml" })
public class HardcodedFilterIntegrationTests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private DummyFilterConsumer dummyFilterConsumer;

	@Autowired
	private LdapTemplate ldapTemplate;

	@Test
	public void verifyThatFilterEditorWorks() {
		Filter filter = dummyFilterConsumer.getFilter();
		assertThat(filter instanceof HardcodedFilter).isTrue();
		assertThat(filter.toString()).isEqualTo("(&(objectclass=person)(!(objectclass=computer))");
	}

	@Test
	public void verifyThatWildcardsAreUnescaped() {
		HardcodedFilter filter = new HardcodedFilter("cn=Some*");
		CountNameClassPairCallbackHandler handler = new CountNameClassPairCallbackHandler();
		ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), handler);
		int hits = handler.getNoOfRows();
		assertThat(hits > 1).isTrue();
	}

}
