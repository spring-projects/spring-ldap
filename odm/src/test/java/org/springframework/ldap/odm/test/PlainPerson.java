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

package org.springframework.ldap.odm.test;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.List;

// Simple LDAP entry for testing 
@Entry(objectClasses = { "person", "top" })
public final class PlainPerson {
    public PlainPerson() {
    }

    public PlainPerson(Name dn, String commonName, String surname) {
        this.dn = dn;
        this.surname = surname;
        objectClasses = new ArrayList<String>();
        objectClasses.add("top");
        objectClasses.add("person");
        cn = commonName;
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
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlainPerson that = (PlainPerson) o;

        if (cn != null ? !cn.equals(that.cn) : that.cn != null) return false;
        if (dn != null ? !dn.equals(that.dn) : that.dn != null) return false;
        if (objectClasses != null ? !objectClasses.equals(that.objectClasses) : that.objectClasses != null)
            return false;
        if (surname != null ? !surname.equals(that.surname) : that.surname != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = objectClasses != null ? objectClasses.hashCode() : 0;
        result = 31 * result + (dn != null ? dn.hashCode() : 0);
        result = 31 * result + (cn != null ? cn.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        return result;
    }
}
