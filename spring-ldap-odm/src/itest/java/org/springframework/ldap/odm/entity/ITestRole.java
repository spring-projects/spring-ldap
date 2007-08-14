/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.entity;

import com.thoughtworks.xstream.XStream;
import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;

import java.util.Set;
import java.util.Queue;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;

@NamingAttribute("cn")
@NamingSuffix({"ou=roles"})
@ObjectClasses({"top", "organizationalRole"})
public class ITestRole
{
    /**
     * ******************************** Directory mapped fields ***********************************
     */
    @DirAttribute("cn")
    private String roleName;

    @DirAttribute
    private String description;

    @DirAttribute("roleOccupant")
    private Collection<ITestPerson> members;

    public String getRoleName()
    {
        return roleName;
    }

    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Collection<ITestPerson> getMembers()
    {
        return members;
    }

    public void setMembers(Collection<ITestPerson> members)
    {
        this.members = members;
    }

    public void addMember(ITestPerson member)
    {
        this.members.add(member);
    }



    /**
     * ********************************************************************************************
     */



    public String toString()
    {
        XStream xStream = new XStream();
        xStream.alias("ITestRole", ITestRole.class);
        xStream.alias("ITestPerson", ITestPerson.class);
        return "\n" + xStream.toXML(this);
    }

}
