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

package org.springframework.ldap.odm.typeconversion;

/**
 * A simple interface to be implemented to provide type conversion functionality.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 */
public interface ConverterManager {
    /**
     * Determine whether this converter manager is able to carry out a specified conversion.
     * 
     * @param fromClass Convert from the <code>fromClass</code>.
     * @param syntax Using the LDAP syntax (may be null).
     * @param toClass To the <code>toClass</code>.
     * @return <code>True</code> if the conversion is supported, <code>false</code> otherwise.
     */
    boolean canConvert(Class<?> fromClass, String syntax, Class<?> toClass);

    /**
     * Convert a given source object with an optional LDAP syntax to an instance of a given class.
     * 
     * @param <T> The class to convert to.
     * @param source The object to convert.
     * @param syntax The LDAP syntax to use (may be null).
     * @param toClass The class to convert to.
     * @return The converted object.
     * 
     * @throws ConverterException If the conversion can not be successfully completed.
     */
    <T> T convert(Object source, String syntax, Class<T> toClass);
}
