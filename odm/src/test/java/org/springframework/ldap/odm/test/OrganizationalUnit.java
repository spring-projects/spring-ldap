/*
 * Copyright 2005-2025 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

/**
 * Automatically generated to represent the LDAP object classes "organizationalunit",
 * "top".
 */
@Entry(objectClasses = { "organizationalUnit", "top" })
public final class OrganizationalUnit {

	@Id
	private Name dn;

	@Attribute(name = "objectClass", syntax = "1.3.6.1.4.1.1466.115.121.1.38")
	private List<String> objectClass = new ArrayList<>();

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

		this.objectClass.add("top");
		this.objectClass.add("organizationalUnit");

		int size = dn.size();
		if (size > 1) {
			this.ou = dn.get(size - 1).split("=")[1];
		}
		else {
			this.ou = "";
		}

	}

	public Name getDn() {
		return this.dn;
	}

	public void setDn(Name dn) {
		this.dn = dn;
	}

	public List<String> getObjectClasses() {
		return Collections.unmodifiableList(this.objectClass);
	}

	public String getOu() {
		return this.ou;
	}

	public String getStreet() {
		return this.street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OrganizationalUnit other = (OrganizationalUnit) obj;
		if (this.description == null) {
			if (other.description != null) {
				return false;
			}
		}
		else if (!this.description.equals(other.description)) {
			return false;
		}
		if (this.dn == null) {
			if (other.dn != null) {
				return false;
			}
		}
		else if (!this.dn.equals(other.dn)) {
			return false;
		}
		if (this.objectClass == null) {
			if (other.objectClass != null) {
				return false;
			}
		}
		else if (this.objectClass.size() != other.objectClass.size()
				|| !(new HashSet<>(this.objectClass)).equals(new HashSet<>(other.objectClass))) {
			return false;
		}
		if (this.ou == null) {
			if (other.ou != null) {
				return false;
			}
		}
		else if (!this.ou.equals(other.ou)) {
			return false;
		}
		if (this.street == null) {
			if (other.street != null) {
				return false;
			}
		}
		else if (!this.street.equals(other.street)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());
		result = prime * result + ((this.dn == null) ? 0 : this.dn.hashCode());
		result = prime * result + ((this.objectClass == null) ? 0 : new HashSet<>(this.objectClass).hashCode());
		result = prime * result + ((this.ou == null) ? 0 : this.ou.hashCode());
		result = prime * result + ((this.street == null) ? 0 : this.street.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return String.format("objectClasses=%1$s | dn=%2$s | ou=%3$s | street=%4$s | description=%5$s",
				this.objectClass, this.dn, this.ou, this.street, this.description);
	}

}
