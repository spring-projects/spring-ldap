/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.entity;

import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;
import org.springframework.ldap.odm.annotations.DirAttribute;

import java.util.Date;
import java.util.List;
import java.util.Set;

@NamingAttribute("uid")
@NamingSuffix({"ou=foobars"})
@ObjectClasses({"top", "organizationalFooBar"})
public class SomeEntityWithGenericCollection
{

    @DirAttribute("uid")
    private String identifier;

    @DirAttribute("activedates")
    private List<Date> activeDates;

    @DirAttribute("scores")
    private Set<Integer> scores;

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public List<Date> getActiveDates()
    {
        return activeDates;
    }

    public void setActiveDates(List<Date> activeDates)
    {
        this.activeDates = activeDates;
    }

    public Set<Integer> getScores()
    {
        return scores;
    }

    public void setScores(Set<Integer> scores)
    {
        this.scores = scores;
    }
}
