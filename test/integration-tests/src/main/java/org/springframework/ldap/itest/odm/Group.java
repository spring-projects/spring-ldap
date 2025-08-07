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

package org.springframework.ldap.itest.odm;

import java.util.Set;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

/**
 * @author Mattias Hellborg Arthursson
 */
@Entry(objectClasses = { "top", "groupOfUniqueNames" }, base = "cn=groups")
public class Group {

	@Id
	private Name dn;

	@Attribute(name = "cn")
	@DnAttribute("cn")
	private String name;

	@Attribute(name = "uniqueMember")
	private Set<Name> members;

	public Name getDn() {
		return this.dn;
	}

	public void setDn(Name dn) {
		this.dn = dn;
	}

	public Set<Name> getMembers() {
		return this.members;
	}

	public void setMembers(Set<Name> members) {
		this.members = members;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addMember(Name member) {
		this.members.add(member);
	}

	public void removeMember(Name member) {
		this.members.remove(member);
	}

}
