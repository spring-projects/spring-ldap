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
package org.springframework.ldap.transaction.compensating.support;

import org.junit.Test;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import javax.naming.ldap.LdapName;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTempEntryRenamingStrategyTest {

	@Test
	public void testGetTemporaryName() {
		LdapName expectedOriginalName = LdapUtils.newLdapName("cn=john doe, ou=somecompany, c=SE");
		DefaultTempEntryRenamingStrategy tested = new DefaultTempEntryRenamingStrategy();

		Name result = tested.getTemporaryName(expectedOriginalName);
		assertThat(result.toString()).isEqualTo("cn=john doe_temp,ou=somecompany,c=SE");
		assertThat(result).isNotSameAs(expectedOriginalName);
	}

	@Test
	public void testGetTemporaryDN_MultivalueDN() {
		LdapName expectedOriginalName = LdapUtils.newLdapName("cn=john doe+sn=doe, ou=somecompany, c=SE");
		DefaultTempEntryRenamingStrategy tested = new DefaultTempEntryRenamingStrategy();

		Name result = tested.getTemporaryName(expectedOriginalName);
		assertThat(result.toString()).isEqualTo("cn=john doe+sn=doe_temp,ou=somecompany,c=SE");
	}

}
