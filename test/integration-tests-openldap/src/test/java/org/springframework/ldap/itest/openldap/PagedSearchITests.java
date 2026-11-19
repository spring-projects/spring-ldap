/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.itest.openldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.SearchControls;

import org.junit.jupiter.api.Test;

import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SingleContextSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the paged results control against OpenLDAP.
 *
 * @author Josh Cummings
 */
class PagedSearchITests {

	private final OpenLdapContainer openLdap = OpenLdapContainer.shared();

	@Test
	void pagedSearchReturnsAllPages() {
		LdapContextSource contextSource = this.openLdap.contextSource(null);
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		AttributesMapper<String> cn = (attributes) -> attributes.get("cn").get().toString();

		// There should be two pages of three entries, and one final page with one entry
		PagedResultsDirContextProcessor processor = new PagedResultsDirContextProcessor(3);

		List<String> all = new ArrayList<>();
		SingleContextSource.doWithSingleContext(contextSource, (operations) -> {
			for (int expected : new int[] { 3, 3, 1 }) {
				List<String> result = operations.search("ou=people", "(objectclass=person)", searchControls, cn,
						processor);
				assertThat(result).hasSize(expected);
				all.addAll(result);
			}
			assertThat(processor.hasMore()).isFalse();
			return null;
		});

		// the cookie must advance, not return the same page repeatedly
		assertThat(all).hasSize(OpenLdapContainer.PEOPLE).doesNotHaveDuplicates();
	}

}
