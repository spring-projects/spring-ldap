/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.entity;

import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;

@NamingAttribute("cn")
@NamingSuffix({"ou=roles"})
@ObjectClasses({"top", "organizationalRole"})
public class UnitTestRole
{
    @DirAttribute("cn")
    private String roleName;

    @DirAttribute
    private String description;

    @DirAttribute("roleOccupant")
    private UnitTestPerson[] members;

    public UnitTestRole(String roleName, String description, UnitTestPerson[] members)
    {
        this.roleName = roleName;
        this.description = description;
        this.members = members;
    }

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

    public UnitTestPerson[] getMembers()
    {
        return members;
    }

    public void setMembers(UnitTestPerson[] members)
    {
        this.members = members;
    }
}

