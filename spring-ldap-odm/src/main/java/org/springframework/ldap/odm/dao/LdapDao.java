/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.dao;

import java.util.List;

import javax.naming.Name;

public interface LdapDao
{
    void create(Object dirObject);

    void update(Object dirObject);

    void createOrUpdate(Object dirObject);

    void delete(Object dirObject);

    Object findByNamingAttribute(String namingValue, Class returnType);

    public Object findByDn(Name dn, Class returnType);

    List findAll(Class ofType);   

    List filterByBeanProperty(String value, String beanPropertyName, Class returnType);
}
