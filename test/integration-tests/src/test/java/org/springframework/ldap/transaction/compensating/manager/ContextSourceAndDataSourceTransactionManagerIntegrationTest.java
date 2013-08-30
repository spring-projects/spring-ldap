/*
 * Copyright 2005-2013 the original author or authors.
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
package org.springframework.ldap.transaction.compensating.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.ldap.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyDao;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.sql.ResultSet;
import java.sql.SQLException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Integration tests for {@link ContextSourceAndDataSourceTransactionManager}.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapAndJdbcTransactionTestContext.xml" })
public class ContextSourceAndDataSourceTransactionManagerIntegrationTest extends AbstractLdapTemplateIntegrationTest {

	private static Log log = LogFactory.getLog(ContextSourceAndDataSourceTransactionManagerIntegrationTest.class);

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
		jdbcTemplate.execute("create table PERSON(fullname VARCHAR, lastname VARCHAR, description VARCHAR)");
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
			assertTrue(true);
		}

		log.debug("Verifying result");

		// Verify that no entry was created
		try {
			ldapTemplate.lookup("cn=some testperson, ou=company1, c=Sweden");
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
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
			assertTrue(true);
		}
	}

	@Test
	public void testCreate() {
		dummyDao.create("Sweden", "company1", "some testperson", "testperson", "some description");

		log.debug("Verifying result");
		Object ldapResult = ldapTemplate.lookup("cn=some testperson, ou=company1, c=Sweden");
		Object dbResult = jdbcTemplate.queryForObject("select * from PERSON where fullname='some testperson'",
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new Object();
					}
				});
		assertNotNull(ldapResult);
		assertNotNull(dbResult);

		ldapTemplate.unbind("cn=some testperson, ou=company1, c=Sweden");
	}

	@Test
	public void testUpdateWithException() {
		String dn = "cn=Some Person,ou=company1,c=Sweden";
		try {
			dummyDao.updateWithException(dn, "Some Person", "Updated Person", "Updated description");
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertTrue(true);
		}

		log.debug("Verifying result");

		Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertEquals("Person", attributes.get("sn").get());
				assertEquals("Sweden, Company1, Some Person", attributes.get("description").get());
				return new Object();
			}
		});

		Object jdbcResult = jdbcTemplate.queryForObject("select * from PERSON where fullname=?",
				new Object[] { "Some Person" }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						assertEquals("Person", rs.getString("lastname"));
						assertEquals("Sweden, Company1, Some Person", rs.getString("description"));
						return new Object();
					}
				});

		assertNotNull(ldapResult);
		assertNotNull(jdbcResult);
	}

	@Test
	public void testUpdate() {
		String dn = "cn=Some Person,ou=company1,c=Sweden";
		dummyDao.update(dn, "Some Person", "Updated Person", "Updated description");

		log.debug("Verifying result");
		Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertEquals("Updated Person", attributes.get("sn").get());
				assertEquals("Updated description", attributes.get("description").get());
				return new Object();
			}
		});

		Object jdbcResult = jdbcTemplate.queryForObject("select * from PERSON where fullname=?",
				new Object[] { "Some Person" }, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						assertEquals("Updated Person", rs.getString("lastname"));
						assertEquals("Updated description", rs.getString("description"));
						return new Object();
					}
				});

		assertNotNull(ldapResult);
		assertNotNull(jdbcResult);
		dummyDao.update(dn, "Some Person", "Person", "Sweden, Company1, Some Person");
	}

	@Test
	public void testUpdateAndRenameWithException() {
		String dn = "cn=Some Person2,ou=company1,c=Sweden";
		String newDn = "cn=Some Person2,ou=company2,c=Sweden";
		try {
			// Perform test
			dummyDao.updateAndRenameWithException(dn, newDn, "Updated description");
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertTrue(true);
		}

		// Verify that entry was not moved.
		try {
			ldapTemplate.lookup(newDn);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
		}

		// Verify that original entry was not updated.
		Object object = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertEquals("Sweden, Company1, Some Person2", attributes.get("description").get());
				return new Object();
			}
		});
		assertNotNull(object);
	}

	@Test
	public void testUpdateAndRename() {
		String dn = "cn=Some Person2,ou=company1,c=Sweden";
		String newDn = "cn=Some Person2,ou=company2,c=Sweden";
		// Perform test
		dummyDao.updateAndRename(dn, newDn, "Updated description");

		// Verify that entry was moved and updated.
		Object object = ldapTemplate.lookup(newDn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertEquals("Updated description", attributes.get("description").get());
				return new Object();
			}
		});

		assertNotNull(object);
		dummyDao.updateAndRename(newDn, dn, "Sweden, Company1, Some Person2");
	}

	@Test
	public void testModifyAttributesWithException() {
		String dn = "cn=Some Person,ou=company1,c=Sweden";
		try {
			// Perform test
			dummyDao.modifyAttributesWithException(dn, "Updated lastname", "Updated description");
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertTrue(true);
		}

		// Verify result - check that the operation was properly rolled back
		Object result = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertEquals("Person", attributes.get("sn").get());
				assertEquals("Sweden, Company1, Some Person", attributes.get("description").get());
				return new Object();
			}
		});

		assertNotNull(result);
	}

	@Test
	public void testModifyAttributes() {
		String dn = "cn=Some Person,ou=company1,c=Sweden";
		// Perform test
		dummyDao.modifyAttributes(dn, "Updated lastname", "Updated description");

		// Verify result - check that the operation was not rolled back
		Object result = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertEquals("Updated lastname", attributes.get("sn").get());
				assertEquals("Updated description", attributes.get("description").get());
				return new Object();
			}
		});

		assertNotNull(result);
		dummyDao.update(dn, "Some Person", "Person", "Sweden, Company1, Some Person");
	}

	@Test
	public void testUnbindWithException() {
		String dn = "cn=Some Person,ou=company1,c=Sweden";
		try {
			// Perform test
			dummyDao.unbindWithException(dn, "Some Person");
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertTrue(true);
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

		assertNotNull(ldapResult);
		assertNotNull(jdbcResult);
	}

	@Test
	public void testUnbind() {
		String dn = "cn=Some Person,ou=company1,c=Sweden";
		// Perform test
		dummyDao.unbind(dn, "Some Person");

		try {
			// Verify result - check that the operation was not rolled back
			ldapTemplate.lookup(dn);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertTrue(true);
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
			assertTrue(true);
		}
	}
}
