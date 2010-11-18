/*
 * Copyright 2005-2010 the original author or authors.
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyDao;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Integration tests for {@link ContextSourceAndDataSourceTransactionManager}.
 * 
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTransactionTestContext.xml" })
public class ContextSourceTransactionManagerIntegrationTest extends AbstractLdapTemplateIntegrationTest {

	private static Log log = LogFactory.getLog(ContextSourceTransactionManagerIntegrationTest.class);

	@Autowired
	@Qualifier("dummyDao")
	private DummyDao dummyDao;

	@Autowired
	private LdapTemplate ldapTemplate;

	@Before
	public void prepareTestedInstance() throws Exception {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
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
	}

	@Test
	public void testCreate() {
		dummyDao.create("Sweden", "company1", "some testperson", "testperson", "some description");

		log.debug("Verifying result");
		String expectedDn = "cn=some testperson, ou=company1, c=Sweden";
		Object ldapResult = ldapTemplate.lookup(expectedDn);
		assertNotNull(ldapResult);

		ldapTemplate.unbind(expectedDn);
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

		assertNotNull(ldapResult);
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

		assertNotNull(ldapResult);

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

		assertNotNull(ldapResult);
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

	}
}
