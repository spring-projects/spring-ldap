/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.samples.useradmin.domain;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mattias Hellborg Arthursson
 */
@Entry(objectClasses = {"groupOfNames", "top"}, base = "ou=Groups")
public final class Group {
    @Id
    private Name id;

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index=1)
    private String name;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "member")
    private Set<Name> members = new HashSet<Name>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Name> getMembers() {
        return  members;
    }

    public void addMember(Name newMember) {
        members.add(newMember);
    }

    public void removeMember(Name member) {
        members.remove(member);
    }

    public Name getId() {
        return id;
    }

    public void setId(Name id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
