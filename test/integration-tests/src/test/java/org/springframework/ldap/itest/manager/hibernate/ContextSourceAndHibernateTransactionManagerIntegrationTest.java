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
package org.springframework.ldap.itest.manager.hibernate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.AbstractLdapTemplateIntegrationTest;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyException;
import org.springframework.ldap.itest.transaction.compensating.manager.hibernate.OrgPerson;
import org.springframework.ldap.itest.transaction.compensating.manager.hibernate.OrgPersonDao;
import org.springframework.ldap.transaction.compensating.manager.ContextSourceAndHibernateTransactionManager;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Integration tests for {@link ContextSourceAndHibernateTransactionManager}.
 *
 * @author Hans Westerbeek
 */
@ContextConfiguration(locations = {"/conf/ldapAndHibernateTransactionTestContext.xml"})
public class ContextSourceAndHibernateTransactionManagerIntegrationTest extends AbstractLdapTemplateIntegrationTest {

	private static Logger log = LoggerFactory.getLogger(ContextSourceAndHibernateTransactionManagerIntegrationTest.class);

	@Autowired
	@Qualifier("dummyDao")
	private OrgPersonDao dummyDao;

	@Autowired
	private LdapTemplate ldapTemplate;

	@Autowired
	private HibernateTemplate hibernateTemplate;

	@Autowired
	private SessionFactory sessionFactory;

	@Before
	public void prepareTest() throws Exception {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}

		OrgPerson person = new OrgPerson();
		person.setId(new Integer(1));
		person.setLastname("Person");
		person.setFullname("Some Person");
		person.setDescription("Sweden, Company1, Some Person");
		person.setCountry("Sweden");
		person.setCompany("Company1");
		// "Some Person", "Person", "Sweden, Company1, Some Person"
		// avoid the transaction manager we have configured, do it manually
		Session session = this.sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.saveOrUpdate(person);
		tx.commit();
		session.close();

	}

	@After
	public void cleanup() throws Exception {
		// probably the wrong idea, this will use the thing i am trying to
		// test..

		Session session = this.sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Query query = session.createQuery("delete from OrgPerson");
		query.executeUpdate();
		tx.commit();
		session.close();
	}

	@Test
	public void testCreateWithException() {
		OrgPerson person = new OrgPerson();

		person.setId(new Integer(2));
		person.setDescription("some description");
		person.setFullname("Some testperson");
		person.setLastname("testperson");
		person.setCountry("Sweden");
		person.setCompany("company1");

		try {
			dummyDao.createWithException(person);
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertThat(true).isTrue();
		}

		log.debug("Verifying result");

		// Verify that no entry was created in ldap or hibernate db
		try {
			ldapTemplate.lookup("cn=some testperson, ou=company1, ou=Sweden");
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		List result = hibernateTemplate.findByNamedParam("from OrgPerson person where person.lastname = :lastname",
				"lastname", person.getLastname());
		assertThat(result.size() == 0).isTrue();

	}

	@Test
	public void testCreate() {
		OrgPerson person = new OrgPerson();

		person.setId(new Integer(2));
		person.setDescription("some description");
		person.setFullname("Some testperson");
		person.setLastname("testperson");
		person.setCountry("Sweden");
		person.setCompany("company1");
		// dummyDao.create("Sweden", "company1", "some testperson",
		// "testperson", "some description");

		this.dummyDao.create(person);
		person = null;
		log.debug("Verifying result");
		Object ldapResult = ldapTemplate.lookup("cn=some testperson, ou=company1, ou=Sweden");
		OrgPerson fromDb = (OrgPerson) this.hibernateTemplate.get(OrgPerson.class, new Integer(2));
		assertThat(ldapResult).isNotNull();
		assertThat(fromDb).isNotNull();
	}

	@Test
	public void testUpdateWithException() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		OrgPerson originalPerson = (OrgPerson) this.hibernateTemplate.load(OrgPerson.class, new Integer(1));
		originalPerson.setLastname("fooo");
		try {
			dummyDao.updateWithException(originalPerson);
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertThat(true).isTrue();
		}

		log.debug("Verifying result");

		Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertThat(attributes.get("sn").get()).as("Person").isNotNull();
				assertThat(attributes.get("description").get()).isEqualTo("Sweden, Company1, Some Person");
				return new Object();
			}
		});

		OrgPerson notUpdatedPerson = (OrgPerson) this.hibernateTemplate.load(OrgPerson.class, new Integer(1));
		assertThat(notUpdatedPerson.getLastname()).isEqualTo("Person");
		assertThat(notUpdatedPerson.getDescription()).isEqualTo("Sweden, Company1, Some Person");

		assertThat(ldapResult).isNotNull();
		// no need to assert if notUpdatedPerson exists
	}

	@Test
	public void testUpdate() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		OrgPerson person = (OrgPerson) this.hibernateTemplate.load(OrgPerson.class, new Integer(1));
		person.setLastname("Updated Person");
		person.setDescription("Updated description");

		dummyDao.update(person);

		log.debug("Verifying result");
		Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				assertThat(attributes.get("sn").get()).isEqualTo("Updated Person");
				assertThat(attributes.get("description").get()).isEqualTo("Updated description");
				return new Object();
			}
		});

		OrgPerson updatedPerson = (OrgPerson) this.hibernateTemplate.load(OrgPerson.class, new Integer(1));
		assertThat(updatedPerson.getLastname()).isEqualTo("Updated Person");
		assertThat(updatedPerson.getDescription()).isEqualTo("Updated description");
		assertThat(ldapResult).isNotNull();
	}

	@Test
	public void testUpdateAndRenameWithException() {
		String dn = "cn=Some Person2,ou=company1,ou=Sweden";
		String newDn = "cn=Some Person2,ou=company2,ou=Sweden";
		OrgPerson person = (OrgPerson) this.hibernateTemplate.load(OrgPerson.class, new Integer(1));
		person.setLastname("Updated Person");
		person.setDescription("Updated description");

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
	}

	@Test
	public void testUnbindWithException() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		OrgPerson person = (OrgPerson) this.hibernateTemplate.load(OrgPerson.class, new Integer(1));

		try {
			// Perform test
			dummyDao.unbindWithException(person);
			fail("DummyException expected");
		}
		catch (DummyException expected) {
			assertThat(true).isTrue();
		}

		person = null;
		// Verify result - check that the operation was properly rolled back
		Object ldapResult = ldapTemplate.lookup(dn, new AttributesMapper() {
			public Object mapFromAttributes(Attributes attributes) throws NamingException {
				// Just verify that the entry still exists.
				return new Object();
			}
		});

		person = (OrgPerson) this.hibernateTemplate.load(OrgPerson.class, new Integer(1)); // will
		// throw
		// exception
		// of
		// person
		// does
		// not
		// exist

		assertThat(ldapResult).isNotNull();
	}

	@Test
	public void testUnbind() {
		String dn = "cn=Some Person,ou=company1,ou=Sweden";
		// Perform test
		OrgPerson person = (OrgPerson) this.hibernateTemplate.load(OrgPerson.class, new Integer(1));
		dummyDao.unbind(person);

		try {
			// Verify result - check that the operation was not rolled back
			ldapTemplate.lookup(dn);
			fail("NameNotFoundException expected");
		}
		catch (NameNotFoundException expected) {
			assertThat(true).isTrue();
		}

		person = (OrgPerson) this.hibernateTemplate.get(OrgPerson.class, new Integer(1));
		assertThat(person).isNull();
	}
}
