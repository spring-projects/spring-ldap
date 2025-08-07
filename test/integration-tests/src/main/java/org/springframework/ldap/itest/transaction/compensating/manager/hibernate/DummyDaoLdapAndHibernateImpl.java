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

package org.springframework.ldap.itest.transaction.compensating.manager.hibernate;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.itest.transaction.compensating.manager.DummyException;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Hans Westerbeek
 */
@Transactional
public class DummyDaoLdapAndHibernateImpl extends HibernateDaoSupport implements OrgPersonDao {

	private LdapTemplate ldapTemplate;

	public void create(OrgPerson person) {
		DistinguishedName dn = new DistinguishedName();
		dn.add("ou", person.getCountry());
		dn.add("ou", person.getCompany());
		dn.add("cn", person.getFullname());

		DirContextAdapter ctx = new DirContextAdapter();
		ctx.setAttributeValues("objectclass", new String[] { "top", "person" });
		ctx.setAttributeValue("cn", person.getFullname());
		ctx.setAttributeValue("sn", person.getLastname());
		ctx.setAttributeValue("description", person.getDescription());
		this.ldapTemplate.bind(dn, ctx, null);
		this.getHibernateTemplate().saveOrUpdate(person);

	}

	public void createWithException(OrgPerson person) {
		this.create(person);
		throw new DummyException("This method failed");

	}

	public void modifyAttributes(String dn, String lastName, String description) {
		DirContextAdapter ctx = (DirContextAdapter) this.ldapTemplate.lookup(dn);
		ctx.setAttributeValue("sn", lastName);
		ctx.setAttributeValue("description", description);

		this.ldapTemplate.modifyAttributes(dn, ctx.getModificationItems());
	}

	public void modifyAttributesWithException(String dn, String lastName, String description) {
		modifyAttributes(dn, lastName, description);
		throw new DummyException("This method failed.");
	}

	public void unbind(OrgPerson person) {
		String dn = prepareDn(person);
		this.ldapTemplate.unbind(dn);
		this.getHibernateTemplate().delete(person);

	}

	public void unbindWithException(OrgPerson person) {
		this.unbind(person);
		throw new DummyException("This method failed");
	}

	public void update(OrgPerson person) {
		String dn = prepareDn(person);
		DirContextAdapter ctx = (DirContextAdapter) this.ldapTemplate.lookup(dn);
		ctx.setAttributeValue("sn", person.getLastname());
		ctx.setAttributeValue("description", person.getDescription());

		this.ldapTemplate.modifyAttributes(ctx);
		this.getHibernateTemplate().saveOrUpdate(person);

	}

	public void updateWithException(OrgPerson person) {
		this.update(person);
		throw new DummyException("This method failed");
	}

	public void updateAndRename(String dn, String newDn, String updatedDescription) {

		DirContextAdapter ctx = (DirContextAdapter) this.ldapTemplate.lookup(dn);
		ctx.setAttributeValue("description", updatedDescription);

		this.ldapTemplate.modifyAttributes(ctx);
		this.ldapTemplate.rename(dn, newDn);

	}

	public void updateAndRenameWithException(String dn, String newDn, String updatedDescription) {
		this.updateAndRename(dn, newDn, updatedDescription);
		throw new DummyException("This method failed");
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	private String prepareDn(OrgPerson person) {
		return "cn=" + person.getFullname() + ",ou=" + person.getCompany() + ",ou=" + person.getCountry();
	}

}
