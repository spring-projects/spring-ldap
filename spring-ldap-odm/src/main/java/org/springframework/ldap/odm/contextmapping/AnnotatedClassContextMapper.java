/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.contextmapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Name;
import javax.naming.directory.Attribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.ldap.core.ContextAssembler;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;
import org.springframework.ldap.odm.attributetypes.LdapTypeConverter;
import org.springframework.ldap.odm.attributetypes.ReferencedEntryEditorFactory;
import org.springframework.ldap.odm.attributetypes.ValidConversionType;
import org.springframework.ldap.odm.attributetypes.exception.ReferencedEntryEditorCreationException;
import org.springframework.ldap.odm.contextmapping.exception.ContextMapperException;
import org.springframework.util.StringUtils;

public class AnnotatedClassContextMapper implements ContextMapper, ContextAssembler
{
    private static final Log LOGGER = LogFactory.getLog(AnnotatedClassContextMapper.class);

    private LdapTypeConverter typeConverter;
    private ReferencedEntryEditorFactory refEditorFactory;

    private Class<?> clazz;
    private String namingAttribute;
    private String[] objectClasses;
    private String[] namingSuffixElements;
    private DistinguishedName namingSuffix;

    private ContextMap contextMap;
    private Map<String, Method> propertyGetters;
    private Map<String, Method> propertySetters;


    public AnnotatedClassContextMapper(Class<?> clazz, LdapTypeConverter typeConverter,
                                       ReferencedEntryEditorFactory refEditorFactory) throws ContextMapperException
    {
        this.clazz = clazz;
        this.typeConverter = typeConverter;
        this.refEditorFactory = refEditorFactory;
        checkConfigured();
        checkRequiredMetaData();
        buildNamingSuffix();
        mapAttributesToBeanProperties();
        cacheGettersAndSetters();
        registerReferencedEntries();
    }

    private void checkConfigured() throws ContextMapperException
    {
        if (clazz == null)
        {
            throw new ContextMapperException("Error creating context mapper."
                    + ". Class<?> arugment is null");
        }

        if (this.typeConverter == null)
        {
            throw new ContextMapperException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". AttributeTypeTranslator arugment is null");
        }

        if (this.refEditorFactory == null)
        {
            throw new ContextMapperException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". ReferencedEntryEditorFactory arugment is null");
        }
    }

    private void checkRequiredMetaData() throws ContextMapperException
    {
        NamingAttribute attnNamingAttr = clazz.getAnnotation(NamingAttribute.class);
        ObjectClasses attnObjectClasses = clazz.getAnnotation(ObjectClasses.class);
        NamingSuffix attnNamingSuffix = clazz.getAnnotation(NamingSuffix.class);
        this.namingAttribute = attnNamingAttr != null ? attnNamingAttr.value() : null;
        this.objectClasses = attnObjectClasses != null ? attnObjectClasses.value() : null;
        this.namingSuffixElements = attnNamingSuffix != null ? attnNamingSuffix.value() : null;

        if (this.namingAttribute == null)
        {
            throw new ContextMapperException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". The @NamingAttribute annotation is required.");
        }

        if (this.objectClasses == null)
        {
            throw new ContextMapperException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". The @ObjectClasses annotation is required.");
        }

        if (namingSuffixElements == null || namingSuffixElements.length == 0)
        {
            throw new ContextMapperException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". The @NamingSuffix annotation is required.");
        }

        if (namingSuffixElements[0].contains(","))
        {
            throw new ContextMapperException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". Naming path nodes must be defined as separate elements, "
                    + "for example: @NamingSuffix({\"ou=people\", \"dc=example\", \"dc=com\"}), and not: "
                    + "@NamingSuffix({\"ou=people,dc=example,dc=com\"})");
        }
    }

    private void buildNamingSuffix() throws ContextMapperException
    {
        namingSuffix = new DistinguishedName();
        for (int i = namingSuffixElements.length - 1; i >= 0; i--)
        {
            String[] nameValue = namingSuffixElements[i].split("=");
            if (nameValue.length != 2)
            {
                throw new ContextMapperException("Error creating context mapper for class: " + clazz.getSimpleName()
                        + ". Syntax error in naming suffix element: " + namingSuffixElements[i]);
            }
            namingSuffix.add(nameValue[0].trim(), nameValue[1].trim());
        }
    }

    private void mapAttributesToBeanProperties()
    {
        contextMap = new ContextMap();
        for (Field field : clazz.getDeclaredFields())
        {
            DirAttribute dirAttribute = field.getAnnotation(DirAttribute.class);
            if (dirAttribute != null)
            {
                String beanPropertyName = StringUtils.capitalize(field.getName());
                String attributeName = dirAttribute.value().equals("") ? field.getName() : dirAttribute.value();
                contextMap.map(beanPropertyName, attributeName);
            }
        }
    }

    private void cacheGettersAndSetters() throws ContextMapperException
    {
        propertyGetters = new HashMap<String, Method>();
        propertySetters = new HashMap<String, Method>();

        for (String beanPropertyName : contextMap.beanPropertyNames())
        {
            try
            {
                Method getter = clazz.getMethod("get" + beanPropertyName);
                propertyGetters.put(beanPropertyName, getter);
                Method setter = clazz.getMethod("set" + beanPropertyName, getter.getReturnType());
                propertySetters.put(beanPropertyName, setter);
            }
            catch (NoSuchMethodException e)
            {
                throw new ContextMapperException("Error creating context mapper for class: "
                        + clazz.getSimpleName()
                        + ". " + clazz.getSimpleName() + " is missing a getter/setter for property: "
                        + beanPropertyName, e);
            }
        }
    }

    private void registerReferencedEntries() throws ContextMapperException
    {
        for (Method getter : propertyGetters.values())
        {
            Class<?> returnType = getter.getReturnType();
            if (!ValidConversionType.isValidConversionType(returnType))
            {
                try
                {
                    Class componentType = returnType.isArray() ? returnType.getComponentType() : returnType;
                    typeConverter.registerCustomEditor(componentType,
                            refEditorFactory.referencedEntryEditorForClass(componentType));
                }
                catch (ReferencedEntryEditorCreationException e)
                {
                    throw new ContextMapperException(clazz.getSimpleName() + "."
                            + getter.getName()
                            + "() has invalid return type. Return type must be a context mapped "
                            + "directory object, or one of the following: "
                            + ValidConversionType.listTypes(), e);
                }
            }
        }
    }

    public Object mapFromContext(Object ctx)
    {
        DirContextAdapter ctxAdapter = (DirContextAdapter) ctx;
        Object instance;
        try
        {
            instance = clazz.newInstance();
            for (String beanPropertyName : contextMap.beanPropertyNames())
            {
                Class attributeType = propertyGetters.get(beanPropertyName).getReturnType();
                Attribute attribute =
                        ctxAdapter.getAttributes().get(contextMap.attributeNameFor(beanPropertyName));
                if (attribute != null)
                {
                    try
                    {
                        Object beanPropertyValue = typeConverter.convertIfNecessary(
                                new AttributeDecorator(attribute).getAllAsObject(), attributeType);
                        propertySetters.get(beanPropertyName).invoke(instance, beanPropertyValue);
                    }
                    catch (TypeMismatchException e)
                    {
                        throw new ContextMapperException("Cannot set bean property '" + beanPropertyName
                                + "' in class " + clazz.getSimpleName() + ", " + e.getMessage(), e);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        return instance;
    }

    // TODO: Simplify this method: When bug with DirContextAdapter.setAttribute() (LDAP-15) is fixed,
    // TODO: register a type converter to convert to type Attribute
    public void mapToContext(Object beanInstance, Object ctx)
    {
        DirContextAdapter ctxAdapter = (DirContextAdapter) ctx;
        ctxAdapter.setAttributeValues("objectclass", objectClasses);
        for (String attributeName : contextMap.attributeNames())
        {
            try
            {
                Object beanPropertyValue = propertyGetters.get(
                        contextMap.beanPropertyNameFor(attributeName)).invoke(beanInstance);
                LOGGER.trace("mapToContext() attribute:" + attributeName + ", value: " + beanPropertyValue);

                if (beanPropertyValue != null)
                {
                    if (beanPropertyValue instanceof byte[])
                    {
                        ctxAdapter.setAttributeValue(attributeName, beanPropertyValue);
                    }
                    else if (beanPropertyValue instanceof Object[])
                    {
                        ctxAdapter.setAttributeValues(attributeName,
                                typeConverter.getAllAsText((Object[]) beanPropertyValue));
                    }
                    else
                    {
                        ctxAdapter.setAttributeValue(attributeName,
                                typeConverter.getAsText(beanPropertyValue));
                    }
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public Name buildDn(Object beanInstance) throws ContextMapperException
    {
        if (beanInstance == null)
        {
            throw new ContextMapperException("Can't build Dn from beanInstance, beanInstance is null");
        }
        return buildDn(namingAttributeValue(beanInstance));
    }

    public Name buildDn(String namingAttributeValue) throws ContextMapperException
    {
        if (namingAttributeValue == null)
        {
            throw new ContextMapperException("Can't build Dn from namingAttributeValue, namingAttributeValue is null");
        }
        DistinguishedName dn = (DistinguishedName) namingSuffix.clone();
        dn.add(namingAttribute, namingAttributeValue);
        LOGGER.trace("buildDn(): " + dn);
        return dn;
    }

    public String namingAttributeValue(Object beanInstance)
    {
        Method method = propertyGetters.get(contextMap.beanPropertyNameFor(namingAttribute));
        try
        {
            return (String) method.invoke(clazz.cast(beanInstance));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public ContextMap getContextMap()
    {
        return contextMap;
    }

    public String[] getObjectClasses()
    {
        return objectClasses;
    }

    public String getNamingAttribute()
    {
        return namingAttribute;
    }

    public DistinguishedName getNamingSuffix()
    {
        return namingSuffix;
    }
}