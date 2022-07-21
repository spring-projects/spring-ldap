/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.odm.typeconversion.impl;

import org.springframework.ldap.odm.typeconversion.ConverterException;
import org.springframework.ldap.odm.typeconversion.ConverterManager;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link org.springframework.ldap.odm.typeconversion.ConverterManager}.
 * <p>
 * The algorithm used is to:
 * <ol>
 * <li>Try to find and use a {@link Converter} registered for the 
 * <code>fromClass</code>, <code>syntax</code> and <code>toClass</code> and use it.</li>
 * <li>If this fails, then if the <code>toClass isAssignableFrom</code> 
 * the <code>fromClass</code> then just assign it.</li>
 * <li>If this fails try to find and use a {@link Converter} registered for the <code>fromClass</code> and 
 * the <code>toClass</code> ignoring the <code>syntax</code>.</li>
 * <li>If this fails then throw a {@link org.springframework.ldap.odm.typeconversion.ConverterException}.</li>
 * </ol>
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 */
public final class ConverterManagerImpl implements ConverterManager {
    /**
     * Separator used to form keys into the converters Map.
     */
    private static final String KEY_SEP = ":";

    /**
     * Map of keys created via makeConverterKey to Converter instances.
     */
    private final Map<String, Converter> converters = new HashMap<String, Converter>();

    /**
     * Make a key into the converters map - the keys is formed from the <code>fromClass</code>, syntax and <code>toClass</code>
     * 
     * @param fromClass The class to convert from.
     * @param syntax The LDAP syntax.
     * @param toClass The class to convert to.
     * @return key
     */
    private String makeConverterKey(Class<?> fromClass, String syntax, Class<?> toClass) {
        StringBuilder key = new StringBuilder();
        if (syntax==null) {
            syntax="";
        }
        key.append(fromClass.getName()).append(KEY_SEP).append(syntax).append(KEY_SEP).append(toClass.getName());
        return key.toString();
    }

    /**
     * Create an empty ConverterManagerImpl
     */
    public ConverterManagerImpl() {
    }

    /**
     * Used to help in the process of dealing with primitive types by mapping them to 
     * their equivalent boxed class.
     */
    private static Map<Class<?>, Class<?>> primitiveTypeMap = new HashMap<Class<?>, Class<?>>();
    static {
        primitiveTypeMap.put(Byte.TYPE, Byte.class);
        primitiveTypeMap.put(Short.TYPE, Short.class);
        primitiveTypeMap.put(Integer.TYPE, Integer.class);
        primitiveTypeMap.put(Long.TYPE, Long.class);
        primitiveTypeMap.put(Float.TYPE, Float.class);
        primitiveTypeMap.put(Double.TYPE, Double.class);
        primitiveTypeMap.put(Boolean.TYPE, Boolean.class);
        primitiveTypeMap.put(Character.TYPE, Character.class);
    }

   
    /* 
     * (non-Javadoc)
     * @see org.springframework.ldap.odm.typeconversion.ConverterManager#canConvert(java.lang.Class, java.lang.String, java.lang.Class)
     */
    public boolean canConvert(Class<?> fromClass, String syntax, Class<?> toClass) {
        Class<?> fixedToClass = toClass;
        if (toClass.isPrimitive()) {
            fixedToClass = primitiveTypeMap.get(toClass);
        }
        Class<?> fixedFromClass = fromClass;
        if (fromClass.isPrimitive()) {
            fixedFromClass = primitiveTypeMap.get(fromClass);
        }
        return fixedToClass.isAssignableFrom(fixedFromClass) ||
                (converters.get(makeConverterKey(fixedFromClass, syntax, fixedToClass)) != null) ||
                (converters.get(makeConverterKey(fixedFromClass, null, fixedToClass)) != null);
    }


    /* 
     * (non-Javadoc)
     * @see org.springframework.ldap.odm.typeconversion.ConverterManager#convert(java.lang.Object, java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(Object source, String syntax, Class<T> toClass) {
        Object result = null;

        // What are we converting form
        Class<?> fromClass = source.getClass();

        // Deal with primitives
        Class<?> targetClass = toClass;
        if (toClass.isPrimitive()) {
            targetClass = primitiveTypeMap.get(toClass);
        }

        // Try to convert with any syntax we have been given
        Converter syntaxConverter = converters.get(makeConverterKey(fromClass, syntax, targetClass));
        if (syntaxConverter != null) {
            try {
                result = syntaxConverter.convert(source, targetClass);
            } catch (Exception e) {
                // Ignore as we may still be able to convert successfully
            }
        }

        // Do we actually need to do any conversion?
        if (result == null && targetClass.isAssignableFrom(fromClass)) {
            result = source;
        }

        // If we were given a syntax and we failed to convert drop back to any mapping
        // that will work from class -> to class
        if (result == null && syntax != null) {
            Converter nullSyntaxConverter = converters.get(makeConverterKey(fromClass, null, targetClass));
            if (nullSyntaxConverter != null) {
                try {
                    result = nullSyntaxConverter.convert(source, targetClass);
                } catch (Exception e) {
                    // Handled at the end of the method
                }
            }
        }

        if (result == null) {
            throw new ConverterException(String.format(
                    "Cannot convert %1$s of class %2$s via syntax %3$s to class %4$s", source, source.getClass(),
                    syntax, toClass));
        }

        // We cannot do the safe thing of doing a .cast as we need to rely on auto-unboxing to deal with primitives!
        return (T)result;
    }

    /**
     * Add a {@link Converter} to this <code>ConverterManager</code>.
     * 
     * @param fromClass The class the <code>Converter</code> should be used to convert from.
     * @param syntax The LDAP syntax that the <code>Converter</code> should be used for.
     * @param toClass The class the <code>Converter</code> should be used to convert to.
     * @param converter The <code>Converter</code> to add.
     */
    public void addConverter(Class<?> fromClass, String syntax, Class<?> toClass, Converter converter) {
        converters.put(makeConverterKey(fromClass, syntax, toClass), converter);
    }
}
