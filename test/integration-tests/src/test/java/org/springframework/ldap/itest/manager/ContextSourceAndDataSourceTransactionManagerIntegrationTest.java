/*
 * Copyright 2005-2016 the original author or authors.
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
package org.springframework.ldap.itest.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyDao;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Integration tests for {@link org.springframework.ldap.transaction.compensating.manager.ContextSourceAndDataSourceTransactionManager}.
 *
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = {"/conf/ldapAndJdbcTransactionTestContext.xml"})
public class ContextSourceAndDataSourceTransactionManagerIntegrationTest extends AbstractLdapTemplateIntegrationTest {

	private static Logger log = LoggerFactory.getLogger(ContextSourceAndDataSourceTransactionManagerIntegrationTest.class);

	@Autowired
	@Qualifier("dummyDao")
	private DummyDao dummyDao;

	@Autowired
	private LdapTemplate ldapTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Before
	public void prepareTestedInstance() throws Exception {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}

		jdbcTemplate.execute("drop table PERSON if exists");
		jdbcTemplate.execute("create table PERSON(fullname VARCHAR(256), lastname VARCHAR(256), description VARCHAR(256))");
		jdbcTemplate.update("insert into PERSON values(?, ?, ?)", new Object[] { "Some Person", "Person",
				"Sweden, Company1, Some Person" });
	}

	@After
	public void cleanup() throws Exception {
		jdbcTemplate.execute("drop table PERSON if exists");
	}

	@Test
	public void testCreateWithException() {
		try {
			dummyDao.createWithException("Sweden", "company1", "some testperson", "testperson", "some description");
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertThat(true).isTrue();
		}

		log.debug("Verifying result");

		// Verify that no entry was created
		try {
			ldapTemplate.lookup("cn=some testperson, ou=company1, ou=Sweden");
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		try {
			jdbcTemplate.queryForObject("select * from PERSON where fullname='some testperson'", new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return null;
				}
			});
			fail("EmptyResultDataAccessException expected");
		}
		catch (EmptyResultDataAccessException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testCreate() {
		dummyDao.create("Sweden", "company1", "some testperson", "testperson", "some description");

		log.debug("Verifying result");
		Object ldapResult = ldapTemplate.lookup("cn=some testperson, ou=company1, ou=Sweden");
		Object dbResult = jdbcTemplate.queryForObject("select * from PERSON where fullname='some testperson'",
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new Object();
					}
				});
		assertThat(ldapResult).isNotNull();
		assertThat(dbResult).isNotNull();

		ldapTemplate.unbind("cn=some testperson, ou=company1, ou=Sweden");
	}

	@Test
	public void testUpdateWithException() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		try {
			dummyDao.updateWithException(dn, "Some Person", "Updated Person", "Updated description");
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertThat(true).isTrue();
		}

		log.debug("Verifying result");

		Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertThat(attributes.get("sn").get()).isEqualTo("Person");
				assertThat(attributes.get("description").get()).isEqualTo("Sweden, Company1, Some Person");
				return new Object();
			}
		});

		Object jdbcResult = jdbcTemplate.queryForObject("select * from PERSON where fullname=?",
				new Object[] { "Some Person" }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						assertThat(rs.getString("lastname")).isEqualTo("Person");
						assertThat(rs.getString("description")).isEqualTo("Sweden, Company1, Some Person");
						return new Object();
					}
				});

		assertThat(ldapResult).isNotNull();
		assertThat(jdbcResult).isNotNull();
	}

	@Test
	public void testUpdate() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		dummyDao.update(dn, "Some Person", "Updated Person", "Updated description");

		log.debug("Verifying result");
		Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertThat(attributes.get("sn").get()).isEqualTo("Updated Person");
				assertThat(attributes.get("description").get()).isEqualTo("Updated description");
				return new Object();
			}
		});

		Object jdbcResult = jdbcTemplate.queryForObject("select * from PERSON where fullname=?",
				new Object[] { "Some Person" }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						assertThat(rs.getString("lastname")).isEqualTo("Updated Person");
						assertThat(rs.getString("description")).isEqualTo("Updated description");
						return new Object();
					}
				});

		assertThat(ldapResult).isNotNull();
		assertThat(jdbcResult).isNotNull();
		dummyDao.update(dn, "Some Person", "Person", "Sweden, Company1, Some Person");
	}

	@Test
	public void testUpdateAndRenameWithException() {
		String dn = "cn=Some Person2,ou=company1,ou=Sweden";
		String newDn = "cn=Some Person2,ou=company2,ou=Sweden";
		try {
			// Perform test
			dummyDao.updateAndRenameWithException(dn, newDn, "Updated description");
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertThat(true).isTrue();
		}

		// Verify that entry was not moved.
		try {
			ldapTemplate.lookup(newDn);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		// Verify that original entry was not updated.
		Object object = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertThat(attributes.get("description").get()).isEqualTo("Sweden, Company1, Some Person2");
				return new Object();
			}
		});
		assertThat(object).isNotNull();
	}

	@Test
	public void testUpdateAndRename() {
		String dn = "cn=Some Person2,ou=company1,ou=Sweden";
		String newDn = "cn=Some Person2,ou=company2,ou=Sweden";
		// Perform test
		dummyDao.updateAndRename(dn, newDn, "Updated description");

		// Verify that entry was moved and updated.
		Object object = ldapTemplate.lookup(newDn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertThat(attributes.get("description").get()).isEqualTo("Updated description");
				return new Object();
			}
		});

		assertThat(object).isNotNull();
		dummyDao.updateAndRename(newDn, dn, "Sweden, Company1, Some Person2");
	}

	@Test
	public void testModifyAttributesWithException() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		try {
			// Perform test
			dummyDao.modifyAttributesWithException(dn, "Updated lastname", "Updated description");
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertThat(true).isTrue();
		}

		// Verify result - check that the operation was properly rolled back
		Object result = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertThat(attributes.get("sn").get()).isEqualTo("Person");
				assertThat(attributes.get("description").get()).isEqualTo("Sweden, Company1, Some Person");
				return new Object();
			}
		});

		assertThat(result).isNotNull();
	}

	@Test
	public void testModifyAttributes() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		// Perform test
		dummyDao.modifyAttributes(dn, "Updated lastname", "Updated description");

		// Verify result - check that the operation was not rolled back
		Object result = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertThat(attributes.get("sn").get()).isEqualTo("Updated lastname");
				assertThat(attributes.get("description").get()).isEqualTo("Updated description");
				return new Object();
			}
		});

		assertThat(result).isNotNull();
		dummyDao.update(dn, "Some Person", "Person", "Sweden, Company1, Some Person");
	}

	@Test
	public void testUnbindWithException() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		try {
			// Perform test
			dummyDao.unbindWithException(dn, "Some Person");
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertThat(true).isTrue();
		}

		// Verify result - check that the operation was properly rolled back
		Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				// Just verify that the entry still exists.
				return new Object();
			}
		});

		Object jdbcResult = jdbcTemplate.queryForObject("select * from PERSON where fullname=?",
				new Object[] { "Some Person" }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						// Just verify that the entry still exists.
						return new Object();
					}
				});

		assertThat(ldapResult).isNotNull();
		assertThat(jdbcResult).isNotNull();
	}

	@Test
	public void testUnbind() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		// Perform test
		dummyDao.unbind(dn, "Some Person");

		try {
			// Verify result - check that the operation was not rolled back
			ldapTemplate.lookup(dn);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		try {
			jdbcTemplate.queryForObject("select * from PERSON where fullname=?", new Object[] { "Some Person" },
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							return null;
						}
					});
			fail("EmptyResultDataAccessException expected");
		}
		catch (EmptyResultDataAccessException expected) {
			assertThat(true).isTrue();
		}
	}
}
