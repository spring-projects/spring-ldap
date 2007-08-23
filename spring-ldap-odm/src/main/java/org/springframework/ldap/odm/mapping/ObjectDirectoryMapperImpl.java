/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.typeconversion.ConversionType;
import org.springframework.ldap.odm.typeconversion.LdapTypeConverter;
import org.springframework.ldap.odm.typeconversion.MultiValueType;
import org.springframework.ldap.odm.typeconversion.ReferencedEntryEditorFactory;
import org.springframework.ldap.odm.util.AttributeWrapper;
import org.springframework.util.StringUtils;

import javax.naming.Name;
import javax.naming.directory.Attribute;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An implemtation of the {@link ObjectDirectoryMapper} interface.
 */
public class ObjectDirectoryMapperImpl implements ObjectDirectoryMapper
{
    private static final Log LOGGER = LogFactory.getLog(ObjectDirectoryMapperImpl.class);

    private ObjectDirectoryMap odm;
    private LdapTypeConverter typeConverter;
    private ReferencedEntryEditorFactory refEditorFactory;

    private Map<String, Method> propertyGetters;
    private Map<String, Method> propertySetters;


    public ObjectDirectoryMapperImpl(ObjectDirectoryMap odm, LdapTypeConverter typeConverter,
                                     ReferencedEntryEditorFactory refEditorFactory) throws MappingException
    {
        this.odm = odm;
        this.typeConverter = typeConverter;
        this.refEditorFactory = refEditorFactory;
        checkConfigured();
        cacheGettersAndSetters();
        mapReferencedEntries();
    }

    public Object mapFromContext(Object ctx)
    {
        DirContextAdapter ctxAdapter = (DirContextAdapter) ctx;
        Object instance;
        try
        {
            instance = odm.getClazz().newInstance();
            for (String beanPropertyName : odm.beanPropertyNames())
            {
                String attributeName = odm.attributeNameFor(beanPropertyName);
                Attribute attribute = ctxAdapter.getAttributes().get(attributeName);
                if (attribute != null)
                {
                    try
                    {
                        Class targetType = propertyGetters.get(beanPropertyName).getReturnType();
                        MethodParameter methodParameter =
                                MethodParameter.forMethodOrConstructor(propertySetters.get(beanPropertyName), 0);
                        Object beanPropertyValue = typeConverter.convertIfNecessary(
                                new AttributeWrapper(attribute).getAllAsObject(), targetType, methodParameter);
                        propertySetters.get(beanPropertyName).invoke(instance, beanPropertyValue);

                    }
                    catch (TypeMismatchException e)
                    {
                        throw new MappingException("Cannot set bean property '" + beanPropertyName
                                + "' in class " + odm.getClazz().getSimpleName() + ", " + e.getMessage(), e);
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

    public void mapToContext(Object beanInstance, Object ctx)
    {
        DirContextAdapter ctxAdapter = (DirContextAdapter) ctx;
        ctxAdapter.setAttributeValues("objectclass", odm.getObjectClasses());
        for (String attributeName : odm.attributeNames())
        {
            try
            {
                Method propertyGetter = propertyGetters.get(odm.beanPropertyNameFor(attributeName));
                Object beanPropertyValue = propertyGetter.invoke(beanInstance);
                if (beanPropertyValue != null)
                {
                    if (beanPropertyValue instanceof byte[])
                    {
                        ctxAdapter.setAttributeValue(attributeName, beanPropertyValue);
                    }
                    else if (MultiValueType.isAssignableTo(beanPropertyValue.getClass()))
                    {
                        ctxAdapter.setAttributeValues(attributeName,
                                typeConverter.getAllAsText(beanPropertyValue));
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

    public Name buildDn(Object beanInstance) throws MappingException
    {
        if (beanInstance == null)
        {
            throw new MappingException("Can't build Dn from beanInstance, beanInstance is null");
        }
        return buildDn(namingAttributeValue(beanInstance));
    }

    public Name buildDn(String namingAttributeValue) throws MappingException
    {
        if (namingAttributeValue == null)
        {
            throw new MappingException("Can't build Dn from namingAttributeValue, namingAttributeValue is null");
        }
        DistinguishedName dn = (DistinguishedName) odm.getNamingSuffix().clone();
        dn.add(odm.getNamingAttribute(), namingAttributeValue);
        LOGGER.trace("buildDn(): " + dn);
        return dn;
    }

    public ObjectDirectoryMap getObjectDirectoryMap()
    {
        return odm;
    }

    /**
     * ****************Private Methods *******************************************
     */

    private void checkConfigured()
    {
        if (odm == null)
        {
            throw new IllegalArgumentException("Error creating mapper."
                    + ". ObjectDirectoryMap argugment is null");
        }

        if (this.typeConverter == null)
        {
            throw new IllegalArgumentException("Error creating mapper for class: "
                    + odm.getClazz().getSimpleName()
                    + ". LdapTypeConverter arugment is null");
        }

        if (this.refEditorFactory == null)
        {
            throw new IllegalArgumentException("Error creating context mapper for class: "
                    + odm.getClazz().getSimpleName()
                    + ". ReferencedEntryEditorFactory arugment is null");
        }
    }

    //TODO: handle non 'get' style method names eg 'isFoo()', 'hasBar()'
    private void cacheGettersAndSetters() throws MappingException
    {
        propertyGetters = new HashMap<String, Method>();
        propertySetters = new HashMap<String, Method>();

        Class clazz = odm.getClazz();
        for (String beanPropertyName : odm.beanPropertyNames())
        {
            try
            {
                String methodSuffix = StringUtils.capitalize(beanPropertyName);
                Method getter = clazz.getMethod("get" + methodSuffix);
                propertyGetters.put(beanPropertyName, getter);
                Method setter = clazz.getMethod("set" + methodSuffix, getter.getReturnType());
                propertySetters.put(beanPropertyName, setter);
            }
            catch (NoSuchMethodException e)
            {
                throw new MappingException("Error creating context mapper for class: "
                        + clazz.getSimpleName()
                        + ". " + clazz.getSimpleName() + " is missing a getter/setter for property: "
                        + beanPropertyName, e);
            }
        }
    }

    private void mapReferencedEntries() throws MappingException
    {
        for (Method getter : propertyGetters.values())
        {
            if (!ConversionType.isConversionType(getter.getReturnType()))
            {
                try
                {
                    Class componentType = componentTypeFor(getter);
                    typeConverter.registerCustomEditor(componentType,
                            refEditorFactory.referencedEntryEditorForClass(componentType));
                }
                catch (MappingException e)
                {
                    throw new MappingException(odm.getClazz().getSimpleName() + "."
                            + getter.getName()
                            + "() has invalid return type. Return type must be a directory " +
                            "mapped object, or one of the following: \n\n"
                            + ConversionType.listTypes() + "\n\n"
                            + MultiValueType.listTypes(), e);
                }
            }
        }
    }

    private Class componentTypeFor(Method getter) throws MappingException
    {
        Class returnType = getter.getReturnType();
        if (Collection.class.isAssignableFrom(returnType))
        {
            ParameterizedType parameterizedType = (ParameterizedType) getter.getGenericReturnType();
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs[0] != null)
            {
                return (Class) typeArgs[0];
            }
            else
            {
                throw new MappingException("Must declare a generic type. "
                        + "Example: List<Integer> getScores()");
            }
        }
        else if (getter.getReturnType().isArray())
        {
            return getter.getReturnType().getComponentType();
        }
        else
        {
            return getter.getReturnType();
        }
    }

    private String namingAttributeValue(Object beanInstance)
    {
        Method method = propertyGetters.get(odm.beanPropertyNameFor(odm.getNamingAttribute()));
        try
        {
            return (String) method.invoke(odm.getClazz().cast(beanInstance));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
