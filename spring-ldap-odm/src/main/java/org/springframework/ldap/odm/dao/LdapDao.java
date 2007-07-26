/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.dao;

import javax.naming.Name;
import java.util.List;

/** A realization of the Data Access Object (DAO) pattern using object directory mapping.
 *
 * @see org.springframework.ldap.odm.mapping.ObjectDirectoryMap 
 * @see org.springframework.ldap.odm.mapping.ObjectDirectoryMapper
 */
public interface LdapDao
{
    /** Persists the mapped object dirObject in the LDAP repository. */
    void create(Object dirObject);

    /** Retrieves the entry in the LDAP repository corresponding to the mapped object dirObject,
     * and updates the attributes to match the values in dirObject.       
     */
    void update(Object dirObject);

    /** If an entry exists in the repository corresponding to the mapped object dirObject, it is
     * updated, otherwise it is created.
     */
    void createOrUpdate(Object dirObject);

    /** if an entry exists in the repository corresponding to the mapped object dirObject,
     * it is deleted.
     */
    void delete(Object dirObject);

    /** Retrieves a uniquely named entry from the repository and maps it the class returnType. */
    Object findByNamingAttribute(String namingValue, Class returnType);

    /** Retrieves a uniquely named entry from the repository and maps it the class returnType. */
    public Object findByDn(Name dn, Class returnType);

    /** Find all entries in the repository that match the object classes declared on ofType. */
    List findAll(Class ofType);

    /** Search for entries in the repository and map results to class returnType. */
    List filterByBeanProperty(String beanPropertyName, String value, Class returnType);
}
