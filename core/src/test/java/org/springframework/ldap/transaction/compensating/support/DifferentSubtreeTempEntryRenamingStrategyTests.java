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

package org.springframework.ldap.transaction.compensating.support;

import javax.naming.Name;
import javax.naming.ldap.LdapName;

import org.junit.Test;

import org.springframework.ldap.support.LdapUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class DifferentSubtreeTempEntryRenamingStrategyTests {

	@Test
	public void testGetTemporaryName() {
		LdapName originalName = LdapUtils.newLdapName("cn=john doe, ou=somecompany, c=SE");
		DifferentSubtreeTempEntryRenamingStrategy tested = new DifferentSubtreeTempEntryRenamingStrategy(
				LdapUtils.newLdapName("ou=tempEntries"));

		int nextSequenceNo = tested.getNextSequenceNo();

		// Perform test
		Name result = tested.getTemporaryName(originalName);

		// Verify result
		assertThat(result.toString()).isEqualTo("cn=john doe" + nextSequenceNo + ",ou=tempEntries");
	}

}
