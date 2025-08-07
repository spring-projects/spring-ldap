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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Attribute.Type;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

// Simple LDAP entry for testing
@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" })
public final class Person {

	public Person() {
	}

	public Person(Name dn, String surname, List<String> desc, int telephoneNumber, byte[] jpegPhoto) {
		this.dn = dn;
		this.surname = surname;
		this.desc = desc;
		this.telephoneNumber = telephoneNumber;
		this.jpegPhoto = jpegPhoto;
		this.objectClasses = new ArrayList<>();
		this.objectClasses.add("inetOrgPerson");
		this.objectClasses.add("organizationalPerson");
		this.objectClasses.add("person");
		this.objectClasses.add("top");
		int size = dn.size();
		if (size > 1) {
			this.cn = dn.get(size - 1).split("=")[1];
		}
		else {
			this.cn = "";
		}
	}

	@Transient
	private String someRandomField = null;

	@Transient
	private List<String> someRandomList = new ArrayList<>();

	@Attribute(name = "objectClass")
	private List<String> objectClasses;

	@Id
	private Name dn;

	// No annotation on purpose!
	private String cn;

	@Attribute(name = "sn")
	private String surname;

	// Everything should be sets and in search operations also as results can be in any
	// order
	@Attribute(name = "description")
	private List<String> desc;

	@Attribute
	private int telephoneNumber;

	@Attribute(type = Type.BINARY)
	byte[] jpegPhoto;

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

	public List<String> getDesc() {
		return this.desc;
	}

	public void setDesc(List<String> desc) {
		this.desc = desc;
	}

	public int getTelephoneNumber() {
		return this.telephoneNumber;
	}

	public void setTelephoneNumber(int telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public byte[] getJpegPhoto() {
		return this.jpegPhoto;
	}

	public void setJpegPhoto(byte[] jpegPhoto) {
		this.jpegPhoto = jpegPhoto;
	}

	public List<String> getObjectClasses() {
		return this.objectClasses;
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
		Person other = (Person) obj;
		if (this.cn == null) {
			if (other.cn != null) {
				return false;
			}
		}
		else if (!this.cn.equals(other.cn)) {
			return false;
		}
		if (this.desc == null) {
			if (other.desc != null) {
				return false;
			}
		}
		else if (this.desc.size() != other.desc.size()
				|| !(new HashSet<>(this.desc)).equals(new HashSet<>(other.desc))) {
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
		if (!Arrays.equals(this.jpegPhoto, other.jpegPhoto)) {
			return false;
		}
		if (this.objectClasses == null) {
			if (other.objectClasses != null) {
				return false;
			}
		}
		else if (this.objectClasses.size() != other.objectClasses.size()
				|| !(new HashSet<>(this.objectClasses)).equals(new HashSet<>(other.objectClasses))) {
			return false;
		}
		if (this.someRandomField == null) {
			if (other.someRandomField != null) {
				return false;
			}
		}
		else if (!this.someRandomField.equals(other.someRandomField)) {
			return false;
		}
		if (this.someRandomList == null) {
			if (other.someRandomList != null) {
				return false;
			}
		}
		else if (!this.someRandomList.equals(other.someRandomList)) {
			return false;
		}
		if (this.surname == null) {
			if (other.surname != null) {
				return false;
			}
		}
		else if (!this.surname.equals(other.surname)) {
			return false;
		}
		if (this.telephoneNumber != other.telephoneNumber) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.cn == null) ? 0 : this.cn.hashCode());
		result = prime * result + ((this.desc == null) ? 0 : new HashSet<>(this.desc).hashCode());
		result = prime * result + ((this.dn == null) ? 0 : this.dn.hashCode());
		result = prime * result + Arrays.hashCode(this.jpegPhoto);
		result = prime * result + ((this.objectClasses == null) ? 0 : new HashSet<>(this.objectClasses).hashCode());
		result = prime * result + ((this.someRandomField == null) ? 0 : this.someRandomField.hashCode());
		result = prime * result + ((this.someRandomList == null) ? 0 : this.someRandomList.hashCode());
		result = prime * result + ((this.surname == null) ? 0 : this.surname.hashCode());
		result = prime * result + this.telephoneNumber;
		return result;
	}

	@Override
	public String toString() {
		StringBuilder jpegString = new StringBuilder();
		if (this.jpegPhoto != null) {
			for (byte b : this.jpegPhoto) {
				jpegString.append(Byte.toString(b));
			}
		}

		return String.format(
				"objectClasses=%1$s | dn=%2$s | cn=%3$s | sn=%4$s | desc=%5$s | telephoneNumber=%6$s | jpegPhoto=%7$s",
				this.objectClasses, this.dn, this.cn, this.surname, this.desc, this.telephoneNumber, jpegString);
	}

}
