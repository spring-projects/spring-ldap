/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ldap.support;

import java.util.SortedSet;

import javax.naming.Name;
import javax.naming.directory.DirContext;

/**
 * Interface for DirContextAdapter to simplify mock testing.
 * 
 * @author Mattias Arthursson
 */
public interface DirContextOperations extends DirContext,
        AttributeModificationsAware {

    /**
     * Gets the update mode. The update mode should be <code>true</code> for a
     * new entry and <code>true</code> for an existing entry that is being
     * updated.
     * 
     * @return update mode
     */
    public boolean isUpdateMode();

    /**
     * Creates a String array of the names of the attributes which have been
     * changed.
     * 
     * If this is a new entry, all set entries will be in the list. If this is
     * an updated entry, only changed and removed entries will be in the array.
     * 
     * @return Array of String
     */
    public String[] getNamesOfModifiedAttributes();

    /**
     * Get the value of a String attribute.
     * 
     * @param name
     *            name of the attribute.
     * @return the value of the attribute.
     */
    public String getStringAttribute(String name);

    /**
     * Get the value of an Object attribute.
     * 
     * @param name
     *            name of the attribute.
     * @return the attribute value as an object if it exists, or
     *         <code>null</code> otherwise.
     */
    public Object getObjectAttribute(String name);

    /**
     * Set the with the name <code>name</code> to the <code>value</code>.
     * 
     * @param name
     *            name of the attribute.
     * @param value
     *            value to set the attribute to.
     */
    public void setAttributeValue(String name, Object value);

    /**
     * Sets a multivalue attribute, disregarding the order of the values.
     * 
     * If value is null or value.length == 0 then the attribute will be removed.
     * 
     * If update mode, changes will be made only if the array has more or less
     * objects or if one or more object has changed. Reordering the objects will
     * not cause an update.
     * 
     * @param name
     *            The id of the attribute.
     * @param values
     *            Attribute values.
     */
    public void setAttributeValues(String name, Object[] values);

    /**
     * Sets a multivalue attribute.
     * 
     * If value is null or value.length == 0 then the attribute will be removed.
     * 
     * If update mode, changes will be made if the array has more or less
     * objects or if one or more string has changed.
     * 
     * Reordering the objects will only cause an update if orderMatters is set
     * to true.
     * 
     * @param name
     *            The id of the attribute.
     * @param values
     *            Attribute values.
     * @param orderMatters
     *            If <code>true</code>, it will be changed even if data was
     *            just reordered.
     */
    public void setAttributeValues(String name, Object[] values,
            boolean orderMatters);

    /**
     * Update the attributes. This will mean that the getters
     * (getStringAttribute methods) will return the updated values. Remove the
     * attributes to be updated.
     */
    public void update();

    /**
     * Get all values of a String attribute.
     * 
     * @param name
     *            name of the attribute.
     * 
     * @return all registered values of the attribute.
     */
    public String[] getStringAttributes(String name);

    /**
     * Get all String values of the attribute as a SortedSet.
     * 
     * @param name
     *            name of the attribute.
     * @return a SortedSet containing all values of the attribute.
     */
    public SortedSet getAttributeSortedStringSet(String name);

    /**
     * Returns DN, without the base path.
     * 
     * @return The distinguished name of the current context.
     * 
     * @see DirContextAdapter#getNameInNamespace()
     */
    public Name getDn();

    /**
     * Set the dn of this entry.
     * 
     * @param dn
     *            the dn.
     */
    public void setDn(Name dn);
}