/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.mapping;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.odm.annotations.DirAttribute;
import org.springframework.ldap.odm.annotations.NamingAttribute;
import org.springframework.ldap.odm.annotations.NamingSuffix;
import org.springframework.ldap.odm.annotations.ObjectClasses;

import java.lang.reflect.Field;

/**
 * An implementation of an {@link ObjectDirectoryMap} based on Annotations. A class that is
 * to be serialized to and from and LDAP repository must include the following annotations:
 * <ul>
 * <li>One {@link NamingAttribute}.</li>
 * <li>One {@link ObjectClasses}.</li>
 * <li>One {@link NamingSuffix}.</li>
 * <li>At least one {@link DirAttribute}.</li>
 * </ul>
 *
 * @see org.springframework.ldap.odm.annotations.NamingAttribute
 * @see org.springframework.ldap.odm.annotations.ObjectClasses
 * @see org.springframework.ldap.odm.annotations.NamingSuffix
 * @see org.springframework.ldap.odm.annotations.DirAttribute
 */
public class AnnotationObjectDirectoryMap extends AbstractObjectDirectoryMap
{

    public AnnotationObjectDirectoryMap(Class<?> clazz) throws MappingException
    {
        super(clazz);
    }

    protected String parseNamingAttribute() throws MappingException
    {
        NamingAttribute attnNamingAttr = (NamingAttribute) clazz.getAnnotation(NamingAttribute.class);
        String namingAttribute = attnNamingAttr != null ? attnNamingAttr.value() : null;

        if (namingAttribute == null)
        {
            throw new MappingException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". The @NamingAttribute annotation is required.");
        }
        return namingAttribute;
    }

    protected String[] parseObjectClasses() throws MappingException
    {
        ObjectClasses attnObjectClasses = (ObjectClasses) clazz.getAnnotation(ObjectClasses.class);
        String[] objectClasses = attnObjectClasses != null ? attnObjectClasses.value() : null;

        if (objectClasses == null)
        {
            throw new MappingException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". The @ObjectClasses annotation is required.");
        }
        if (objectClasses[0].contains(","))
        {
            throw new MappingException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". Object classes must be defined as separate elements, "
                    + "for example: @ObjectClasses({\"top\", \"inetorgperson\"}), and not: "
                    + "@ObjectClasses({\"top,inetorgperson\"})");
        }
        return objectClasses;
    }

    protected DistinguishedName parseNamingSuffix() throws MappingException
    {
        NamingSuffix attnNamingSuffix = (NamingSuffix) clazz.getAnnotation(NamingSuffix.class);
        String[] namingSuffixElements = attnNamingSuffix != null ? attnNamingSuffix.value() : null;

        if (namingSuffixElements == null || namingSuffixElements.length == 0)
        {
            throw new MappingException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". The @NamingSuffix annotation is required.");
        }

        if (namingSuffixElements[0].contains(","))
        {
            throw new MappingException("Error creating context mapper for class: "
                    + clazz.getSimpleName()
                    + ". Naming suffix elements must be declared separately, "
                    + "for example: @NamingSuffix({\"ou=people\", \"dc=example\", \"dc=com\"}), and not: "
                    + "@NamingSuffix({\"ou=people,dc=example,dc=com\"})");
        }

        DistinguishedName namingSuffix = new DistinguishedName();
        for (int i = namingSuffixElements.length - 1; i >= 0; i--)
        {
            String[] nameValue = namingSuffixElements[i].split("=");
            if (nameValue.length != 2)
            {
                throw new MappingException("Error creating context mapper for class: " + clazz.getSimpleName()
                        + ". Syntax error in naming suffix element: " + namingSuffixElements[i]);
            }
            namingSuffix.add(nameValue[0].trim(), nameValue[1].trim());
        }
        return namingSuffix;
    }

    protected void mapAttributesToBeanProperties()
    {
        for (Field field : clazz.getDeclaredFields())
        {
            DirAttribute dirAttribute = field.getAnnotation(DirAttribute.class);
            if (dirAttribute != null)
            {
                String beanPropertyName = field.getName();
                String attributeName = dirAttribute.value().equals("") ? field.getName() : dirAttribute.value();
                map(beanPropertyName, attributeName);
            }
        }
    }
}