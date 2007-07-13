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
import org.springframework.ldap.odm.contextmapping.AnnotatedClassContextMapper;
import org.springframework.ldap.odm.contextmapping.ContextMapperFactory;
import org.springframework.ldap.odm.contextmapping.exception.ContextMapperException;
import org.springframework.ldap.odm.dao.exception.DaoException;
import org.springframework.ldap.odm.dao.exception.DataIntegrityViolationException;
import org.springframework.ldap.odm.dao.exception.EntryNotFoundException;

public class LdapDaoImpl implements LdapDao
{
    static final Log LOGGER = LogFactory.getLog(LdapDaoImpl.class);
    LdapTemplate ldapTemplate;
    ContextMapperFactory ctxMapperFactory;

    public LdapDaoImpl(LdapTemplate ldapTemplate, ContextMapperFactory ctxMapperFactory)
    {
        this.ldapTemplate = ldapTemplate;
        this.ctxMapperFactory = ctxMapperFactory;
    }

    public void create(Object dirObject)
    {
        DirContextAdapter context = new DirContextAdapter();
        try
        {
            AnnotatedClassContextMapper ctxMapper = ctxMapperFactory.contextMapperForClass(dirObject.getClass());
            ctxMapper.mapToContext(dirObject, context);
            ldapTemplate.bind(ctxMapper.buildDn(dirObject), context, null);
        }
        catch (org.springframework.dao.DataIntegrityViolationException dive)
        {
            throw new DataIntegrityViolationException(dive.getMessage());
        }
        catch (ContextMapperException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }

    public void update(Object dirObject)
    {
        try
        {
            AnnotatedClassContextMapper ctxMapper = ctxMapperFactory.contextMapperForClass(dirObject.getClass());
            Name dn = ctxMapper.buildDn(dirObject);
            DirContextAdapter contextAdapter = (DirContextAdapter) ldapTemplate.lookup(dn);
            ctxMapper.mapToContext(dirObject, contextAdapter);
            ldapTemplate.modifyAttributes(dn, contextAdapter.getModificationItems());
        }
        catch (org.springframework.ldap.NameNotFoundException e)
        {
            //We need to rethrow a serializable exception if we are going to invoke
            //this as a remote service.
            throw new EntryNotFoundException(e.getMessage());
        }
        catch (ContextMapperException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }

    public void createOrUpdate(Object dirObject)
    {
        try
        {
            AnnotatedClassContextMapper ctxMapper = ctxMapperFactory.contextMapperForClass(dirObject.getClass());
            if (findByDn(ctxMapper.buildDn(dirObject), dirObject.getClass()) == null)
            {
                create(dirObject);
            }
            else
            {
                update(dirObject);
            }
        }
        catch (ContextMapperException e)
        {
            throw new DaoException(e.getMessage(), e);
        }

    }

    public void delete(Object dirObject)
    {
        try
        {
            AnnotatedClassContextMapper ctxMapper = ctxMapperFactory.contextMapperForClass(dirObject.getClass());
            ldapTemplate.unbind(ctxMapper.buildDn(dirObject));
        }
        catch (ContextMapperException e)
        {
            e.printStackTrace();
            throw new DaoException(e.getMessage(), e);
        }
    }

    public Object findByNamingAttribute(String namingAttributeValue, Class returnType)
    {
        try
        {
            AnnotatedClassContextMapper ctxMapper = ctxMapperFactory.contextMapperForClass(returnType);
            Name dn = ctxMapper.buildDn(namingAttributeValue);
            return findByDn(dn, returnType);
        }
        catch (ContextMapperException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }

    public Object findByDn(Name dn, Class returnType)
    {
        try
        {
            AnnotatedClassContextMapper ctxMapper = ctxMapperFactory.contextMapperForClass(returnType);
            return ldapTemplate.lookup(dn, ctxMapper);
        }
        catch (ContextMapperException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }


    public List filterByBeanProperty(String value, String beanPropertyName, Class returnType)
    {
        try
        {
            LOGGER.debug("Filtering on property: " + beanPropertyName + ", for value: " + value);
            AnnotatedClassContextMapper ctxMapper =
                    ctxMapperFactory.contextMapperForClass(returnType);

            try
            {
                String attributeName = ctxMapper.getContextMap().attributeNameFor(beanPropertyName);
                String filter = attributeName + "=" + value;
                List results = ldapTemplate.search(ctxMapper.getNamingSuffix().toString(), filter,
                        ctxMapperFactory.contextMapperForClass(returnType));
                LOGGER.debug(results.size() + " results found for filter: " + filter);
                return results;
            }
            catch (ContextMapperException e)
            {
                throw new DaoException(e.getMessage(), e);
            }
        }
        catch (ContextMapperException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }

    public List findAll(Class ofType)
    {
        try
        {
            AnnotatedClassContextMapper ctxMapper = ctxMapperFactory.contextMapperForClass(ofType);
            StringBuilder filterBuilder = new StringBuilder();
            filterBuilder.append("(&");
            for (String objectClass : ctxMapper.getObjectClasses())
            {
                filterBuilder.append("(objectClass=");
                filterBuilder.append(objectClass);
                filterBuilder.append(")");
            }
            filterBuilder.append(")");
            List results = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filterBuilder.toString(), ctxMapper);
            LOGGER.debug("Found " + results.size() + " entries of type " + ofType.getSimpleName());
            return results;
        }
        catch (ContextMapperException e)
        {
            throw new DaoException(e.getMessage(), e);
        }
    }
}
