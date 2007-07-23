/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.entity;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;

import java.util.Date;

@NamingAttribute("cn")
@NamingSuffix({"ou = people", "dc = example", "dc=com"})
@ObjectClasses({"top", "person", "organizationalPerson", "inetorgperson"})
public class ITestEntity
{

    @DirAttribute("cn")
    private String name;

    @DirAttribute("addr")
    private String address;

    @DirAttribute("mail")
    private String mail;

    @DirAttribute("userpassword")
    private byte[] password;

    @DirAttribute("desc")
    private String[] description;

    @DirAttribute("acceptemails")
    private Boolean acceptEmails;

    @DirAttribute("loginresettime")
    private Date resetLogin;

    @DirAttribute("failedlogins")
    private Integer failedLogins;

    @DirAttribute("creatorname")
    private DistinguishedName creator;

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

    public byte[] getPassword()
    {
        return password;
    }

    public void setPassword(byte[] password)
    {
        this.password = password;
    }

    public String[] getDescription()
    {
        return description;
    }

    public void setDescription(String[] description)
    {
        this.description = description;
    }

    public Boolean getAcceptEmails()
    {
        return acceptEmails;
    }

    public void setAcceptEmails(Boolean acceptEmails)
    {
        this.acceptEmails = acceptEmails;
    }

    public Date getResetLogin()
    {
        return resetLogin;
    }

    public void setResetLogin(Date resetLogin)
    {
        this.resetLogin = resetLogin;
    }

    public Integer getFailedLogins()
    {
        return failedLogins;
    }

    public void setFailedLogins(Integer failedLogins)
    {
        this.failedLogins = failedLogins;
    }

    public DistinguishedName getCreator()
    {
        return creator;
    }

    public void setCreator(DistinguishedName creator)
    {
        this.creator = creator;
    }
}
