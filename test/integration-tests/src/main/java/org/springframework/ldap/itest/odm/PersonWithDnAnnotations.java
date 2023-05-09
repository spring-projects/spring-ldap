/*
 * Copyright 2005-2023 the original author or authors.
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

package org.springframework.ldap.itest.odm;

import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

/**
 * @author Mattias Hellborg Arthursson
 */
@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" })
public class PersonWithDnAnnotations {

	@Id
	private Name dn;

	@Attribute(name = "cn")
	@DnAttribute(value = "cn", index = 2)
	private String commonName;

	@Attribute(name = "sn")
	private String surname;

	@Attribute(name = "description")
	private List<String> desc;

	@Attribute(name = "uid")
	private List<String> userId;

	@Attribute(name = "telephoneNumber")
	private String telephoneNumber;

	@DnAttribute(value = "ou", index = 1)
	@Transient
	private String company;

	@DnAttribute(value = "ou", index = 0)
	@Transient
	private String country;

	// operational attribute according to https://tools.ietf.org/html/rfc4530
	@Attribute(name = "entryUUID", readonly = true)
	private String entryUuid;

	public Name getDn() {
		return this.dn;
	}

	public void setDn(Name dn) {
		this.dn = dn;
	}

	public String getCommonName() {
		return this.commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
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

	public List<String> getUserId() {
		return this.userId;
	}

	public void setUserId(List<String> userId) {
		this.userId = userId;
	}

	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getCompany() {
		return this.company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEntryUuid() {
		return this.entryUuid;
	}

}
