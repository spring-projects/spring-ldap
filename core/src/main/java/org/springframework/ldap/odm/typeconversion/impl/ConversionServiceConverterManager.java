/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.odm.typeconversion.impl;

import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.ldap.odm.typeconversion.ConverterManager;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.naming.Name;

/**
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class ConversionServiceConverterManager implements ConverterManager {
    private GenericConversionService conversionService;
    private static final String DEFAULT_CONVERSION_SERVICE_CLASS =
            "org.springframework.core.convert.support.DefaultConversionService";

    public ConversionServiceConverterManager(GenericConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public ConversionServiceConverterManager() {
        ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        if(ClassUtils.isPresent(DEFAULT_CONVERSION_SERVICE_CLASS, defaultClassLoader)) {
            try {
                Class<?> clazz = ClassUtils.forName(DEFAULT_CONVERSION_SERVICE_CLASS, defaultClassLoader);
                conversionService = (GenericConversionService) clazz.newInstance();
            } catch (Exception e) {
                ReflectionUtils.handleReflectionException(e);
            }
        } else {
            conversionService = new GenericConversionService();
        }

        prePopulateWithNameConverter();
    }

    private void prePopulateWithNameConverter() {
        conversionService.addConverter(new StringToNameConverter());
    }

    @Override
    public boolean canConvert(Class<?> fromClass, String syntax, Class<?> toClass) {
        return conversionService.canConvert(fromClass, toClass);
    }

    @Override
    public <T> T convert(Object source, String syntax, Class<T> toClass) {
        return conversionService.convert(source, toClass);
    }

    public final static class NameToStringConverter
            implements org.springframework.core.convert.converter.Converter<Name, String> {
        @Override
        public String convert(Name source) {
            if(source == null) {
                return null;
            }

            return source.toString();
        }
    }

    public static final class StringToNameConverter
            implements org.springframework.core.convert.converter.Converter<String, Name> {

        @Override
        public Name convert(String source) {
            if(source == null) {
                return null;
            }

            return LdapUtils.newLdapName(source);
        }
    }

}
