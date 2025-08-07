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

package org.springframework.ldap.odm.test;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

// Simple LDAP entry for testing
@Entry(objectClasses = { "person", "top" })
public final class PlainPerson {

	public PlainPerson() {
	}

	public PlainPerson(Name dn, String commonName, String surname) {
		this.dn = dn;
		this.surname = surname;
		this.objectClasses = new ArrayList<String>();
		this.objectClasses.add("top");
		this.objectClasses.add("person");
		this.cn = commonName;
	}

	@Attribute(name = "objectClass")
	private List<String> objectClasses;

	@Id
	private Name dn;

	@Attribute(name = "cn")
	private String cn;

	@Attribute(name = "sn")
	private String surname;

	public Name getDn() {
		return this.dn;
	}

	public void setDn(Name dn) {
		this.dn = dn;
	}

	public String getCn() {
		return this.cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getSurname() {
		return this.surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PlainPerson that = (PlainPerson) o;

		if ((this.cn != null) ? !this.cn.equals(that.cn) : that.cn != null) {
			return false;
		}
		if ((this.dn != null) ? !this.dn.equals(that.dn) : that.dn != null) {
			return false;
		}
		if ((this.objectClasses != null) ? !this.objectClasses.equals(that.objectClasses)
				: that.objectClasses != null) {
			return false;
		}
		if ((this.surname != null) ? !this.surname.equals(that.surname) : that.surname != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = (this.objectClasses != null) ? this.objectClasses.hashCode() : 0;
		result = 31 * result + ((this.dn != null) ? this.dn.hashCode() : 0);
		result = 31 * result + ((this.cn != null) ? this.cn.hashCode() : 0);
		result = 31 * result + ((this.surname != null) ? this.surname.hashCode() : 0);
		return result;
	}

}
