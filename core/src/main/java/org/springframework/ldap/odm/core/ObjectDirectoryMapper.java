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

package org.springframework.ldap.odm.core;

import org.springframework.LdapDataEntry;
import org.springframework.ldap.filter.Filter;

import javax.naming.Name;

/**
 * The ObjectDirectoryMapper keeps track of managed class metadata and is used by {@link org.springframework.ldap.core.LdapTemplate}
 * to map to/from entity objects annotated with the annotations specified in the {@link org.springframework.ldap.odm.annotations}
 * package. Instances of this class are typically intended for internal use only.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public interface ObjectDirectoryMapper {

    /**
     * Used to convert from Java representation of an Ldap Entry when writing to
     * the Ldap directory
     *
     * @param entry - The entry to convert.
     * @param context - The LDAP context to store the converted entry
     * @throws org.springframework.ldap.NamingException on error.
     */
    void mapToLdapDataEntry(Object entry, LdapDataEntry context);

    /**
     * Used to convert from the JNDI LDAP representation of an Entry to the Java representation when reading from LDAP.
     * @throws org.springframework.ldap.NamingException on error.
     */
    <T> T mapFromLdapDataEntry(LdapDataEntry ctx, Class<T> clazz);

    /**
     * Get the distinguished name for the specified object.
     *
     * @param entry the entry to get distinguished name for.
     * @return the distinguished name of the entry.
     * @throws org.springframework.ldap.NamingException on error.
     */
    Name getId(Object entry);

    /**
     * Set the distinguished name for the specified object.
     *
     * @param entry the entry to set the name on
     * @param id the name to set
     * @throws org.springframework.ldap.NamingException on error.
     */
    void setId(Object entry, Name id);

    Name getCalculatedId(Object entry);

    /**
     * Use the specified search filter and return a new one that only applies to entries of the specified class.
     * In effect this means padding the original filter with an objectclass condition.
     *
     * @param clazz the class.
     * @param baseFilter the filter we want to use.
     * @return the original filter, modified so that it only applies to entries of the specified class.
     * @throws org.springframework.ldap.NamingException on error.
     */
    Filter filterFor(Class<?> clazz, Filter baseFilter);

    /**
     * Get the attribute corresponding to the specified field name.
     * @param clazz the clazz.
     * @param fieldName the field name.
     * @return the attribute name.
     * @throws IllegalArgumentException if the fieldName is not present in the class or if
     * it is not mapped to an attribute.
     */
    String attributeFor(Class<?> clazz, String fieldName);

    /**
     * Check if the specified class is already managed by this instance; if not, check the metadata and add the class to the
     * managed classes.
     *
     * @param clazz the class to manage.
     * @return all relevant attribute names used in the given class.
     * @throws org.springframework.ldap.NamingException on error.
     */
    String[] manageClass(Class<?> clazz);
}
