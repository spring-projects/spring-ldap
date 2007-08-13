/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.dao;

import java.util.List;

import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.mapping.MappingException;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapper;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapperFactory;
import org.springframework.ldap.NameNotFoundException;

/**
 * An implementation of the <code>LdapDao</code> interface. 
 */
public class LdapDaoImpl implements LdapDao
{
    static final Log LOGGER = LogFactory.getLog(LdapDaoImpl.class);
    LdapTemplate ldapTemplate;
    ObjectDirectoryMapperFactory odmFactory;

    public LdapDaoImpl(LdapTemplate ldapTemplate, ObjectDirectoryMapperFactory odmFactory)
    {
        this.ldapTemplate = ldapTemplate;
        this.odmFactory = odmFactory;
    }

    public void create(Object dirObject)
    {
        DirContextAdapter context = new DirContextAdapter();
        try
        {
            ObjectDirectoryMapper mapper =
                    odmFactory.objectDirectoryMapperForClass(dirObject.getClass());
            mapper.mapToContext(dirObject, context);
            ldapTemplate.bind(mapper.buildDn(dirObject), context, null);
        }
        catch (MappingException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }

    public void update(Object dirObject)
    {
        try
        {
            ObjectDirectoryMapper mapper =
                    odmFactory.objectDirectoryMapperForClass(dirObject.getClass());
            Name dn = mapper.buildDn(dirObject);
            DirContextAdapter contextAdapter = (DirContextAdapter) ldapTemplate.lookup(dn);
            mapper.mapToContext(dirObject, contextAdapter);
            ldapTemplate.modifyAttributes(dn, contextAdapter.getModificationItems());
        }
        catch (MappingException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }

    public void createOrUpdate(Object dirObject)
    {
        try
        {
            ObjectDirectoryMapper mapper =
                    odmFactory.objectDirectoryMapperForClass(dirObject.getClass());
            if (findByDn(mapper.buildDn(dirObject), dirObject.getClass()) == null)
            {
                create(dirObject);
            }
            else
            {
                update(dirObject);
            }
        }
        catch (MappingException e)
        {
            throw new DaoException(e.getMessage(), e);
        }

    }

    public void delete(Object dirObject)
    {
        try
        {
            ObjectDirectoryMapper mapper =
                    odmFactory.objectDirectoryMapperForClass(dirObject.getClass());
            ldapTemplate.unbind(mapper.buildDn(dirObject));
        }
        catch (MappingException e)
        {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e);
        }
    }

    public Object findByNamingAttribute(String namingAttributeValue, Class returnType)
    {
        try
        {
            ObjectDirectoryMapper mapper =
                    odmFactory.objectDirectoryMapperForClass(returnType);
            Name dn = mapper.buildDn(namingAttributeValue);
            return findByDn(dn, returnType);
        }
        catch (MappingException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }

    public Object findByDn(Name dn, Class returnType)
    {
        try
        {
            ObjectDirectoryMapper mapper =
                    odmFactory.objectDirectoryMapperForClass(returnType);
            return ldapTemplate.lookup(dn, mapper);
        }
        catch (MappingException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
        catch (NameNotFoundException e)
        {
            return null;
        }
    }


    public List filterByBeanProperty(String beanPropertyName, String value, Class returnType)
    {
        try
        {
            LOGGER.debug("Filtering on property: " + beanPropertyName + ", for value: " + value);
            ObjectDirectoryMapper mapper = odmFactory.objectDirectoryMapperForClass(returnType);
            String filter = mapper.getObjectDirectoryMap().attributeNameFor(beanPropertyName)
                    + "=" + value;
            List results = ldapTemplate.search(mapper.getObjectDirectoryMap().getNamingSuffix().toString(),
                    filter, mapper);
            LOGGER.debug(results.size() + " results found for filter: " + filter);
            return results;
        }
        catch (MappingException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }

    public List findAll(Class ofType)
    {
        try
        {
            ObjectDirectoryMapper mapper = odmFactory.objectDirectoryMapperForClass(ofType);
            StringBuilder filterBuilder = new StringBuilder();
            filterBuilder.append("(&");
            for (String objectClass : mapper.getObjectDirectoryMap().getObjectClasses())
            {
                filterBuilder.append("(objectClass=");
                filterBuilder.append(objectClass);
                filterBuilder.append(")");
            }
            filterBuilder.append(")");
            List results = ldapTemplate.search(
                    DistinguishedName.EMPTY_PATH, filterBuilder.toString(), mapper);
            LOGGER.debug("Found " + results.size() + " entries of type " + ofType.getSimpleName());
            return results;
        }
        catch (MappingException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }
}
