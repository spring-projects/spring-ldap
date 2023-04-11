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

package org.springframework.ldap.itest.control;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.support.LdapOperationsCallback;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration("classpath:/conf/pagedSearchTestContext.xml")
public class PagedSearchITests extends AbstractJUnit4SpringContextTests {

	@Autowired
	private ContextSource contextSource;

	private static final AttributesMapper<String> CN_ATTRIBUTES_MAPPER = new AttributesMapper<String>() {
		@Override
		public String mapFromAttributes(Attributes attributes) throws NamingException {
			return attributes.get("cn").get().toString();
		}
	};

	@Before
	public void prepareTestedData() throws IOException, NamingException {
		LdapTestUtils.cleanAndSetup(contextSource, LdapUtils.newLdapName("ou=People"),
				new ClassPathResource("/setup_data.ldif"));
	}

	@After
	public void cleanup() throws NamingException {
		LdapTestUtils.clearSubContexts(contextSource, LdapUtils.newLdapName("ou=People"));
	}

	@Test
	public void testPaged() {
		final SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		// There should be three pages of three entries, and one final page with one entry
		final PagedResultsDirContextProcessor processor = new PagedResultsDirContextProcessor(3);

		SingleContextSource.doWithSingleContext(contextSource, new LdapOperationsCallback<Object>() {
			@Override
			public Object doWithLdapOperations(LdapOperations operations) {
				List<String> result = operations.search("ou=People", "(&(objectclass=person))", searchControls,
						CN_ATTRIBUTES_MAPPER, processor);
				assertThat(result).hasSize(3);

				result = operations.search("ou=People", "(&(objectclass=person))", searchControls, CN_ATTRIBUTES_MAPPER,
						processor);
				assertThat(result).hasSize(3);

				result = operations.search("ou=People", "(&(objectclass=person))", searchControls, CN_ATTRIBUTES_MAPPER,
						processor);
				assertThat(result).hasSize(3);

				result = operations.search("ou=People", "(&(objectclass=person))", searchControls, CN_ATTRIBUTES_MAPPER,
						processor);
				assertThat(result).hasSize(1);

				assertThat(processor.hasMore()).isFalse();
				return null;
			}
		});

	}

}
