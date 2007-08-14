/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.entity;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;


@NamingAttribute("uid")
@NamingSuffix({"ou=people"})
@ObjectClasses({"person", "organizationalPerson", "inetorgperson"})
public class ITestPerson
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

    public String toString()
    {
        XStream xStream = new XStream();
        xStream.alias("ITestPerson", ITestPerson.class);
        return "\n" + xStream.toXML(this);
    }

    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
