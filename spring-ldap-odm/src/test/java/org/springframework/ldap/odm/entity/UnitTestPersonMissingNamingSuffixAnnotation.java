/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.entity;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.ObjectClasses;

import java.util.Date;

@NamingAttribute("uid")
@ObjectClasses({"person", "organizationalPerson", "inetorgperson"})
public class UnitTestPersonMissingNamingSuffixAnnotation
{
    /**
     * ******************************** Directory mapped fields ***********************************
     */
    @DirAttribute("uid")
    private String identifier;

    @DirAttribute("sn")
    private String lastName;

    @DirAttribute("givenName")
    private String givenName;

    @DirAttribute("cn")
    private String fullName;

    @DirAttribute("mail")
    private String emailAddress;

    @DirAttribute("userpassword")
    private byte[] password;

    @DirAttribute
    private String[] description;

    @DirAttribute("acceptemails")
    private Boolean acceptEmails;

    @DirAttribute("loginresettime")
    private Date resetLogin;

    @DirAttribute("failedlogins")
    private Integer failedLogins;

    @DirAttribute("creatorname")
    private DistinguishedName creator;

    /**
     * ********************************************************************************************
     */

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getGivenName()
    {
        return givenName;
    }

    public void setGivenName(String givenName)
    {
        this.givenName = givenName;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
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