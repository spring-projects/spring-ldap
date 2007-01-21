/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.entity;

import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;

import com.thoughtworks.xstream.XStream;

@NamingAttribute("cn")
@NamingSuffix({"ou=roles"})
@ObjectClasses({"top", "organizationalRole"})

public class TestRole
{
    /*********************************** Directory mapped fields ************************************/
    @DirAttribute("cn")
    private String roleName;

    @DirAttribute
    private String description;

    @DirAttribute("roleOccupant")
    private TestPerson[] members;
    
    /************************************************************************************************/

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

    public TestPerson[] getMembers()
    {
        return members;
    }

    public void setMembers(TestPerson[] members)
    {
        this.members = members;
    }

    public String toString()
    {
        XStream xStream = new XStream();
        xStream.alias("TestRole", TestRole.class);
        xStream.alias("TestPerson", TestPerson.class);
        return "\n" + xStream.toXML(this);
    }

}
