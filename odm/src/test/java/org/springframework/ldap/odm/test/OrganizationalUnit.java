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

package org.springframework.ldap.odm.test;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Automatically generated to represent the LDAP object classes
 * "organizationalunit", "top".
 */
@Entry(objectClasses = { "organizationalUnit", "top" })
public final class OrganizationalUnit {

	@Id
	private Name dn;

	@Attribute(name = "objectClass", syntax = "1.3.6.1.4.1.1466.115.121.1.38")
	private List<String> objectClass = new ArrayList<String>();

	@Attribute(name = "ou", syntax = "1.3.6.1.4.1.1466.115.121.1.15")
	private String ou;

	@Attribute(name = "street", syntax = "1.3.6.1.4.1.1466.115.121.1.15")
	private String street;

	@Attribute(name = "description", syntax = "1.3.6.1.4.1.1466.115.121.1.15")
	private String description;

	public OrganizationalUnit() {
	}

	public OrganizationalUnit(Name dn, String street, String description) {
		this.dn = dn;
		this.street = street;
		this.description = description;

		objectClass.add("top");
		objectClass.add("organizationalUnit");
	   

		int size = dn.size();
		if (size > 1) {
			ou = dn.get(size - 1).split("=")[1];
		} else {
			ou = "";
		}

	}

	public Name getDn() {
		return dn;
	}

	public void setDn(Name dn) {
		this.dn = dn;
	}

	public List<String> getObjectClasses() {
		return Collections.unmodifiableList(objectClass);
	}

	public String getOu() {
		return ou;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return String.format("objectClasses=%1$s | dn=%2$s | ou=%3$s | street=%4$s | description=%5$s", objectClass,
				dn, ou, street, description);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((dn == null) ? 0 : dn.hashCode());
		result = prime * result + ((objectClass == null) ? 0 : new HashSet<String>(objectClass).hashCode());
		result = prime * result + ((ou == null) ? 0 : ou.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrganizationalUnit other = (OrganizationalUnit) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (dn == null) {
			if (other.dn != null)
				return false;
		} else if (!dn.equals(other.dn))
			return false;
		if (objectClass == null) {
			if (other.objectClass != null)
				return false;
		} else 
			if (objectClass.size()!=other.objectClass.size() || 
					!(new HashSet<String>(objectClass)).equals(new HashSet<String>(other.objectClass)))
				return false;
		if (ou == null) {
			if (other.ou != null)
				return false;
		} else if (!ou.equals(other.ou))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		return true;
	}
}
