package org.springframework.ldap.odm.test;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Attribute.Type;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

// Simple LDAP entry for testing 
@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" })
public final class Person {
    public Person() {
    }

    public Person(Name dn, String surname, List<String> desc, int telephoneNumber, byte[]  jpegPhoto) {
        this.dn = dn;
        this.surname = surname;
        this.desc = desc;
        this.telephoneNumber = telephoneNumber;
        this.jpegPhoto = jpegPhoto;
        objectClasses = new ArrayList<String>();
        objectClasses.add("top");
        objectClasses.add("person");
        objectClasses.add("organizationalPerson");
        objectClasses.add("inetOrgPerson");
        int size = dn.size();
        if (size > 1) {
            cn = dn.get(size - 1).split("=")[1];
        } else {
            cn = "";
        }
    }

    @Transient
    private String someRandomField = null;

    @Transient
    private List<String> someRandomList = new ArrayList<String>();

    @Attribute(name = "objectClass")
    private List<String> objectClasses;

    @Id
    private Name dn;

    // No annotation on purpose!
    private String cn;

    @Attribute(name = "sn")
    private String surname;

    // Everything should be sets and in search operations also as results can be in any order
    @Attribute(name = "description")
    private List<String> desc;

    @Attribute
    private int telephoneNumber;

    @Attribute(type = Type.BINARY)
    byte[] jpegPhoto;

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

    public List<String> getDesc() {
        return desc;
    }

    public void setDesc(List<String> desc) {
        this.desc = desc;
    }

    public int getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(int telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public byte[] getJpegPhoto() {
        return jpegPhoto;
    }

    public void setJpegPhoto(byte[] jpegPhoto) {
        this.jpegPhoto = jpegPhoto;
    }

    public List<String> getObjectClasses() {
        return objectClasses;
    }

    @Override
    public String toString() {
        StringBuilder jpegString=new StringBuilder();
        if (jpegPhoto!=null) {
            for (byte b:jpegPhoto) {
                jpegString.append(Byte.toString(b));
            }
        }
        
        return String.format(
                "objectClasses=%1$s | dn=%2$s | cn=%3$s | sn=%4$s | desc=%5$s | telephoneNumber=%6$s | jpegPhoto=%7$s",
                objectClasses, dn, cn, surname, desc, telephoneNumber, jpegString);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cn == null) ? 0 : cn.hashCode());
        result = prime * result + ((desc == null) ? 0 : new HashSet<String>(desc).hashCode());
        result = prime * result + ((dn == null) ? 0 : dn.hashCode());
        result = prime * result + Arrays.hashCode(jpegPhoto);
        result = prime * result + ((objectClasses == null) ? 0 : new HashSet<String>(objectClasses).hashCode());
        result = prime * result + ((someRandomField == null) ? 0 : someRandomField.hashCode());
        result = prime * result + ((someRandomList == null) ? 0 : someRandomList.hashCode());
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
        result = prime * result + telephoneNumber;
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
        Person other = (Person) obj;
        if (cn == null) {
            if (other.cn != null)
                return false;
        } else if (!cn.equals(other.cn))
            return false;
        if (desc == null) {
            if (other.desc != null)
                return false;
        } else if (desc.size()!=other.desc.size() || !(new HashSet<String>(desc)).equals(new HashSet<String>(other.desc)))
            return false;
        if (dn == null) {
            if (other.dn != null)
                return false;
        } else if (!dn.equals(other.dn))
            return false;
        if (!Arrays.equals(jpegPhoto, other.jpegPhoto))
            return false;
        if (objectClasses == null) {
            if (other.objectClasses != null)
                return false;
        } else if (desc.size()!=other.desc.size() || !(new HashSet<String>(objectClasses)).equals(new HashSet<String>(other.objectClasses)))
            return false;
        if (someRandomField == null) {
            if (other.someRandomField != null)
                return false;
        } else if (!someRandomField.equals(other.someRandomField))
            return false;
        if (someRandomList == null) {
            if (other.someRandomList != null)
                return false;
        } else if (!someRandomList.equals(other.someRandomList))
            return false;
        if (surname == null) {
            if (other.surname != null)
                return false;
        } else if (!surname.equals(other.surname))
            return false;
        if (telephoneNumber != other.telephoneNumber)
            return false;
        return true;
    }
}
