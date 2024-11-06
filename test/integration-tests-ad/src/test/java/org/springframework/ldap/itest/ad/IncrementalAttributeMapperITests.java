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

package org.springframework.ldap.itest.ad;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultIncrementalAttributesMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration("classpath:/incrementalAttributeMapperTest.xml")
public class IncrementalAttributeMapperITests extends AbstractJUnit4SpringContextTests {

	private static final DistinguishedName BASE_DN = new DistinguishedName("ou=dummy,dc=261consulting,dc=local");

	private static final DistinguishedName OU_DN = new DistinguishedName("ou=dummy");

	private static final DistinguishedName GROUP_DN = new DistinguishedName(OU_DN).append("cn", "testgroup");

	private static final String DEFAULT_PASSWORD = "ahcoophah5Oi4oh";

	@Autowired
	private LdapTemplate ldapTemplate;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Before
	public void prepareTestData() throws UnsupportedEncodingException {
		cleanup();

		createBaseOu();
		for (int i = 0; i < 1502; i++) {
			createUser("test" + i);
		}

		createGroup();
	}

	private void createGroup() {
		DirContextAdapter ctx = new DirContextAdapter(GROUP_DN);

		ctx.addAttributeValue("objectclass", "top");
		ctx.addAttributeValue("objectclass", "group");
		ctx.addAttributeValue("cn", "testgroup");
		ctx.addAttributeValue("sAMAccountName", "TESTGROUP");

		for (int i = 0; i < 1501; i++) {
			ctx.addAttributeValue("member", buildUserRefDn("test" + i));
		}

		this.ldapTemplate.bind(ctx);
	}

	private String buildUserRefDn(String username) {
		return new DistinguishedName(BASE_DN).append("cn", username).toString();
	}

	private void createBaseOu() {
		createOu();
	}

	private void createUser(String username) throws UnsupportedEncodingException {
		DirContextAdapter ctx = new DirContextAdapter(new DistinguishedName(OU_DN).append("cn", username));

		ctx.addAttributeValue("objectclass", "top");
		ctx.addAttributeValue("objectclass", "person");
		ctx.addAttributeValue("objectclass", "organizationalPerson");
		ctx.addAttributeValue("objectclass", "user");

		ctx.setAttributeValue("givenName", username);
		ctx.setAttributeValue("userPrincipalName", username + "@example.com");
		ctx.setAttributeValue("cn", username);
		ctx.setAttributeValue("description", "Dummy user");
		ctx.setAttributeValue("sAMAccountName",
				username.toUpperCase(Locale.ENGLISH) + "." + username.toUpperCase(Locale.ENGLISH));
		ctx.setAttributeValue("userAccountControl", "512");

		String newQuotedPassword = "\"" + DEFAULT_PASSWORD + "\"";
		ctx.setAttributeValue("unicodePwd", newQuotedPassword.getBytes("UTF-16LE"));

		this.ldapTemplate.bind(ctx);
	}

	private void createOu() {
		DirContextAdapter ctx = new DirContextAdapter(OU_DN);

		ctx.addAttributeValue("objectClass", "top");
		ctx.addAttributeValue("objectClass", "organizationalUnit");

		ctx.setAttributeValue("ou", "dummy");
		ctx.setAttributeValue("description", "dummy description");

		this.ldapTemplate.bind(ctx);
	}

	@After
	public void cleanup() {
		try {
			this.ldapTemplate.lookup(OU_DN);
		}
		catch (NameNotFoundException ex) {
			// Nothing to cleanup
			return;
		}

		while (true) {
			try {
				this.ldapTemplate.unbind(OU_DN, true);
				// Everything is deleted
				return;
			}
			catch (SizeLimitExceededException ex) {
				// There's more to delete
			}
		}
	}

	@Test
	public void verifyRetrievalOfLotsOfAttributeValues() {
		DistinguishedName testgroupDn = new DistinguishedName(OU_DN).append("cn", "testgroup");

		// The 'member' attribute consists of > 1500 entries and will not be returned
		// without range specifier.
		DirContextOperations ctx = this.ldapTemplate.lookupContext(testgroupDn);
		assertThat(ctx.getStringAttribute("member")).isNull();

		DefaultIncrementalAttributesMapper attributeMapper = new DefaultIncrementalAttributesMapper(
				new String[] { "member", "cn" });
		assertThat(attributeMapper.hasMore()).as("There should be more results to get").isTrue();

		String[] attributesArray = attributeMapper.getAttributesForLookup();
		assertThat(attributesArray.length).isEqualTo(2);
		assertThat(attributesArray[0]).isEqualTo("member");
		assertThat(attributesArray[1]).isEqualTo("cn");

		// First iteration - there should now be more members left, but all cn values
		// should have been collected.
		this.ldapTemplate.lookup(testgroupDn, attributesArray, attributeMapper);

		assertThat(attributeMapper.hasMore()).as("There should be more results to get").isTrue();
		// Only member attribute should be requested in this query.
		attributesArray = attributeMapper.getAttributesForLookup();
		assertThat(attributesArray.length).isEqualTo(1);
		assertThat(attributesArray[0]).isEqualTo("member;Range=1500-*");

		// Second iteration - all data should now have been collected.
		this.ldapTemplate.lookup(testgroupDn, attributeMapper.getAttributesForLookup(), attributeMapper);
		assertThat(attributeMapper.hasMore()).as("There should be no more results to get").isFalse();

		List memberValues = attributeMapper.getValues("member");
		assertThat(memberValues).isNotNull();
		assertThat(memberValues).hasSize(1501);

		List cnValues = attributeMapper.getValues("cn");
		assertThat(cnValues).isNotNull();
		assertThat(cnValues).hasSize(1);
	}

	@Test
	public void jiraLdap234ITest() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);

		try {
			transactionTemplate.execute(new TransactionCallback<Object>() {
				@Override
				public Object doInTransaction(TransactionStatus status) {
					ModificationItem modificationItem = new ModificationItem(DirContext.ADD_ATTRIBUTE,
							new BasicAttribute("member", buildUserRefDn("test" + 1501)));
					IncrementalAttributeMapperITests.this.ldapTemplate.modifyAttributes(GROUP_DN,
							new ModificationItem[] { modificationItem });

					// The below should cause a rollback
					throw new RuntimeException("Simulate some failure");
				}
			});

			fail("RuntimeException expected");
		}
		catch (RuntimeException expected) {
			DefaultIncrementalAttributesMapper attributeMapper = new DefaultIncrementalAttributesMapper(
					new String[] { "member" });
			while (attributeMapper.hasMore()) {
				this.ldapTemplate.lookup(GROUP_DN, attributeMapper.getAttributesForLookup(), attributeMapper);
			}

			// LDAP-234: After rollback the attribute values were cleared after rollback
			assertThat(DefaultIncrementalAttributesMapper.lookupAttributeValues(this.ldapTemplate, GROUP_DN, "member")
				.size()).isEqualTo(1501);
		}
	}

}
