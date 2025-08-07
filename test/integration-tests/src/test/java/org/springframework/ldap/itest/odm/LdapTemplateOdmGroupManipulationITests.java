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

package org.springframework.ldap.itest.odm;

import java.util.HashSet;
import java.util.Set;

import javax.naming.Name;

import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTests;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateOdmGroupManipulationITests extends AbstractLdapTemplateIntegrationTests {

	@Autowired
	private LdapTemplate tested;

	@Test
	public void testFindOne() {
		Group group = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		assertThat(group).isNotNull();
		assertThat(group.getName()).isEqualTo("ROLE_USER");
		assertThat(group.getMembers()).hasSize(4);

		Set<Name> members = group.getMembers();
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden," + base))).isTrue();
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person2,ou=company1,ou=Sweden," + base))).isTrue();
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company2,ou=Sweden," + base))).isTrue();
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person3,ou=company1,ou=Sweden," + base))).isTrue();
	}

	@Test
	public void testRemoveMember() {
		Group group = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		group.removeMember(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden," + base));
		this.tested.update(group);

		Group verification = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		Set<Name> members = verification.getMembers();
		assertThat(members).hasSize(3);
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden," + base))).isFalse();
	}

	@Test
	public void testRemoveMemberSyntacticallyEqual() {
		Group group = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		group.removeMember(LdapUtils.newLdapName("cn=Some Person,OU=company1, ou=Sweden," + base));
		this.tested.update(group);

		Group verification = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		Set<Name> members = verification.getMembers();
		assertThat(members).hasSize(3);
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden," + base))).isFalse();
	}

	@Test
	public void testAddMember() {
		Group group = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		group.addMember(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Norway," + base));
		this.tested.update(group);

		Group verification = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		Set<Name> members = verification.getMembers();
		assertThat(members).hasSize(5);
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Norway," + base))).isTrue();
	}

	@Test
	public void testAddMemberDuplicate() {
		Group group = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		group.addMember(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden," + base));
		this.tested.update(group);

		Group verification = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		Set<Name> members = verification.getMembers();
		assertThat(members).hasSize(4);
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden," + base))).isTrue();
	}

	@Test
	public void testAddMemberSyntacticallyEqualDuplicate() {
		Group group = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		group.addMember(LdapUtils.newLdapName("cn=Some Person,OU=company1 ,ou=Sweden," + base));
		this.tested.update(group);

		Group verification = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		Set<Name> members = verification.getMembers();
		assertThat(members).hasSize(4);
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden," + base))).isTrue();
	}

	@Test
	public void testSetMembersSyntacticallyEqual() {
		Group group = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		group.setMembers(new HashSet<Name>() {
			{
				add(LdapUtils.newLdapName("CN=Some Person,OU=company1, ou=Sweden, " + base));
				add(LdapUtils.newLdapName("CN=Some Person2, OU=company1,ou=Sweden," + base));
			}
		});
		this.tested.update(group);

		Group verification = this.tested.findOne(LdapQueryBuilder.query().where("cn").is("ROLE_USER"), Group.class);

		Set<Name> members = verification.getMembers();

		assertThat(members).hasSize(2);
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person,ou=company1,ou=Sweden," + base))).isTrue();
		assertThat(members.contains(LdapUtils.newLdapName("cn=Some Person2,ou=company1,ou=Sweden," + base))).isTrue();
	}

}
