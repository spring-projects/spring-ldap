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

package org.springframework.ldap.odm.core.impl;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

/**
 * @author Mattias Hellborg Arthursson
 */
@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" })
public class UnitTestPersonWithIndexedDnAttributes {

	@Id
	private Name dn;

	@DnAttribute(value = "cn", index = 2)
	private String fullName;

	@DnAttribute(value = "ou", index = 1)
	private String company;

	@DnAttribute(value = "c", index = 0)
	private String country;

	@Attribute(name = "age")
	private Long age;

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Long getAge() {
		return this.age;
	}

	public void setAge(Long age) {
		this.age = age;
	}

}
