/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.itest.transaction.compensating.manager;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class LdapDummyDaoImpl implements DummyDao {

	private static final boolean RECURSIVE = true;

	private LdapTemplate ldapTemplate;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.ldap.transaction.support.DummyDao#createWithException(java.lang
	 * .String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void createWithException(String country, String company, String fullname, String lastname,
			String description) {
		create(country, company, fullname, lastname, description);
		throw new DummyException("This method failed");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.transaction.support.DummyDao#create(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void create(String country, String company, String fullname, String lastname, String description) {
		DistinguishedName dn = new DistinguishedName();
		dn.add("ou", country);
		dn.add("ou", company);
		dn.add("cn", fullname);

		DirContextAdapter ctx = new DirContextAdapter();
		ctx.setAttributeValues("objectclass", new String[] { "top", "person" });
		ctx.setAttributeValue("cn", fullname);
		ctx.setAttributeValue("sn", lastname);
		ctx.setAttributeValue("description", description);
		ldapTemplate.bind(dn, ctx, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.transaction.support.DummyDao#update(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public void update(String dn, String fullname, String lastname, String description) {
		DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
		ctx.setAttributeValue("sn", lastname);
		ctx.setAttributeValue("description", description);

		ldapTemplate.modifyAttributes(ctx);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.ldap.transaction.support.DummyDao#updateWithException(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	public void updateWithException(String dn, String fullname, String lastname, String description) {
		update(dn, fullname, lastname, description);
		throw new DummyException("This method failed.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.ldap.transaction.support.DummyDao#updateAndRename(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	public void updateAndRename(String dn, String newDn, String description) {
		DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
		ctx.setAttributeValue("description", description);

		ldapTemplate.modifyAttributes(ctx);
		ldapTemplate.rename(dn, newDn);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.ldap.transaction.support.DummyDao#updateAndRenameWithException(
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	public void updateAndRenameWithException(String dn, String newDn, String description) {
		updateAndRename(dn, newDn, description);
		throw new DummyException("This method failed.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.ldap.transaction.support.DummyDao#modifyAttributes(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	public void modifyAttributes(String dn, String lastName, String description) {
		DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
		ctx.setAttributeValue("sn", lastName);
		ctx.setAttributeValue("description", description);

		ldapTemplate.modifyAttributes(dn, ctx.getModificationItems());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.ldap.transaction.support.DummyDao#modifyAttributesWithException
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	public void modifyAttributesWithException(String dn, String lastName, String description) {
		modifyAttributes(dn, lastName, description);
		throw new DummyException("This method failed.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ldap.transaction.support.DummyDao#unbind(java.lang.String)
	 */
	public void unbind(String dn, String fullname) {
		ldapTemplate.unbind(dn);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.ldap.transaction.support.DummyDao#unbindWithException(java.lang
	 * .String)
	 */
	public void unbindWithException(String dn, String fullname) {
		unbind(dn, fullname);
		throw new DummyException("This operation failed.");
	}

	@Override
	public void deleteRecursively(String dn) {
		ldapTemplate.unbind(dn, RECURSIVE);
	}

	@Override
	public void deleteRecursivelyWithException(String dn) {
		deleteRecursively(dn);
		throw new DummyException("This method failed");
	}

	@Override
	public void createRecursivelyAndUnbindSubnode() {
		DirContextAdapter ctx = new DirContextAdapter();
		ctx.setAttributeValues("objectclass", new String[] { "top", "organizationalUnit" });
		ctx.setAttributeValue("ou", "dummy");
		ctx.setAttributeValue("description", "dummy description");

		ldapTemplate.bind("ou=dummy", ctx, null);
		ldapTemplate.bind("ou=dummy,ou=dummy", ctx, null);
		ldapTemplate.unbind("ou=dummy,ou=dummy");
		ldapTemplate.unbind("ou=dummy");
	}

	@Override
	public void createRecursivelyAndUnbindSubnodeWithException() {
		createRecursivelyAndUnbindSubnode();
		throw new DummyException("This method failed");
	}

}
