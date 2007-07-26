/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.typeconversion;

import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;

@NamingAttribute("cn")
@NamingSuffix({"ou = people", "dc = example", "dc=com"})
@ObjectClasses({"top", "person", "organizationalPerson", "inetorgperson"})
public class TestReferencedEntry
{
    @DirAttribute("cn")
    private String name;

    @DirAttribute("addr")
    private String address;

    @DirAttribute("mail")
    private String mail;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getMail()
    {
        return mail;
    }

    public void setMail(String mail)
    {
        this.mail = mail;
    }
}