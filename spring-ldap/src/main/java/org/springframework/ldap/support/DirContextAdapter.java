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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the interesting methods of the DirContext interface. In particular
 * it contains utility methods for getting and setting Attributes. Using the
 * {@link org.springframework.ldap.support.DefaultDirObjectFactory} in your
 * ContextSource you may receive instances of this class from searches and
 * lookups. This can be particularly useful when updating data, since this class
 * implements
 * {@link org.springframework.ldap.support.AttributeModificationsAware},
 * providing a {@link #getModificationItems()} method.
 * 
 * @author Magnus Robertsson
 * @author Andreas Ronge
 * @author Adam Skogman
 * @author Mattias Arthursson
 */
public class DirContextAdapter implements DirContextOperations {

    private static final boolean ORDER_DOESNT_MATTER = false;

    private static Log log = LogFactory.getLog(DirContextAdapter.class);

    private final Attributes attrs;

    private DistinguishedName dn;

    private DistinguishedName base;

    private boolean updateMode = false;

    private Attributes updatedAttrs;

    private NamingExceptionTranslator exceptionTranslator;

    /**
     * Default constructor.
     */
    public DirContextAdapter() {
        this(null, null, null);
    }

    /**
     * Create a new adapter from the supplied dn.
     * 
     * @param dn
     *            the dn.
     */
    public DirContextAdapter(Name dn) {
        this(null, dn);
    }

    /**
     * Create a new adapter from the supplied attributes and dn.
     * 
     * @param attrs
     *            the attributes.
     * @param dn
     *            the dn.
     */
    public DirContextAdapter(Attributes attrs, Name dn) {
        this(attrs, dn, null);
    }

    /**
     * Create a new adapter from the supplied attributes, dn, and base.
     * 
     * @param attrs
     *            the attributes.
     * @param dn
     *            the dn.
     * @param base
     *            the base name.
     */
    public DirContextAdapter(Attributes attrs, Name dn, Name base) {
        if (attrs != null) {
            this.attrs = attrs;
        } else {
            this.attrs = new BasicAttributes(true);
        }
        if (dn != null) {
            this.dn = new DistinguishedName(dn.toString());
        } else {
            this.dn = new DistinguishedName();
        }
        if (base != null) {
            this.base = new DistinguishedName(base.toString());
        } else {
            this.base = new DistinguishedName();
        }
    }

    /**
     * Constructor for cloning an existing adapter.
     * 
     * @param master
     *            The adapter to be copied.
     */
    protected DirContextAdapter(DirContextAdapter master) {
        this.attrs = (Attributes) master.attrs.clone();
        this.dn = master.dn;
        this.updatedAttrs = (Attributes) master.updatedAttrs.clone();
        this.updateMode = master.updateMode;
    }

    /**
     * Sets the update mode. The update mode should be <code>false</code> for
     * a new entry and <code>true</code> for an existing entry that is being
     * updated.
     * 
     * @param mode
     *            Update mode.
     */
    protected void setUpdateMode(boolean mode) {
        this.updateMode = mode;
        if (updateMode) {
            updatedAttrs = new BasicAttributes(true);
        }
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#isUpdateMode()
     */
    public boolean isUpdateMode() {
        return updateMode;
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#getNamesOfModifiedAttributes()
     */
    public String[] getNamesOfModifiedAttributes() {

        List tmpList = new ArrayList();

        NamingEnumeration attributesEnumeration;
        if (isUpdateMode()) {
            attributesEnumeration = updatedAttrs.getAll();
        } else {
            attributesEnumeration = attrs.getAll();
        }

        try {
            while (attributesEnumeration.hasMore()) {
                Attribute oneAttribute = (Attribute) attributesEnumeration
                        .next();
                tmpList.add(oneAttribute.getID());
            }
        } catch (NamingException e) {
            throw getExceptionTranslator().translate(e);
        } finally {
            closeNamingEnumeration(attributesEnumeration);
        }

        return (String[]) tmpList.toArray(new String[0]);
    }

    private void closeNamingEnumeration(NamingEnumeration enumeration) {
        try {
            if (enumeration != null) {
                enumeration.close();
            }
        } catch (NamingException e) {
            // Never mind this
        }
    }

    /*
     * @see org.springframework.ldap.support.AttributeModificationsAware#getModificationItems()
     */
    public ModificationItem[] getModificationItems() {
        if (!updateMode) {
            return new ModificationItem[0];
        }

        List tmpList = new LinkedList();
        NamingEnumeration attributesEnumeration = null;
        try {
            attributesEnumeration = updatedAttrs.getAll();

            // find attributes that have been changed, removed or added
            while (attributesEnumeration.hasMore()) {
                Attribute oneAttr = (Attribute) attributesEnumeration.next();

                collectModifications(oneAttr, tmpList);
            }
        } catch (NamingException e) {
            throw getExceptionTranslator().translate(e);
        } finally {
            closeNamingEnumeration(attributesEnumeration);
        }

        if (log.isDebugEnabled()) {
            log.debug("Number of modifications:" + tmpList.size());
        }

        return (ModificationItem[]) tmpList
                .toArray(new ModificationItem[tmpList.size()]);
    }

    /**
     * Collect all modifications for the changed attribute. If no changes have
     * been made, return immediately. If modifications have been made, and the
     * original size as well as the updated size of the attribute is 1, replace
     * the attribute. If the size of the updated attribute is 0, remove the
     * attribute. Otherwise, the attribute is a multi-value attribute, in which
     * case all modifications to the original value (removals and additions)
     * will be collected individually.
     * 
     * @param changedAttr
     *            the value of the changed attribute.
     * @param modificationList
     *            the list in which to add the modifications.
     * @throws NamingException
     *             if thrown by called Attribute methods.
     */
    private void collectModifications(Attribute changedAttr,
            List modificationList) throws NamingException {
        Attribute currentAttribute = attrs.get(changedAttr.getID());

        if (changedAttr.equals(currentAttribute)) {
            // No changes
            return;
        } else if (currentAttribute != null && currentAttribute.size() == 1
                && changedAttr.size() == 1) {
            // Replace single-vale attribute.
            modificationList.add(new ModificationItem(
                    DirContext.REPLACE_ATTRIBUTE, changedAttr));
        } else if (changedAttr.size() == 0) {
            // Attribute has been removed.
            modificationList.add(new ModificationItem(
                    DirContext.REMOVE_ATTRIBUTE, changedAttr));
        } else {
            // Collect all modifications to attribute individually (this also
            // covers additions to a previously non-existant attribute).
            Collection oldValues = new LinkedList();
            Collection newValues = new LinkedList();

            collectAttributeValues(oldValues, currentAttribute);
            collectAttributeValues(newValues, changedAttr);
            Collection myModifications = new LinkedList();

            Collection addedValues = CollectionUtils.subtract(newValues,
                    oldValues);
            Collection removedValues = CollectionUtils.subtract(oldValues,
                    newValues);

            collectModifications(DirContext.ADD_ATTRIBUTE, changedAttr,
                    addedValues, myModifications);
            collectModifications(DirContext.REMOVE_ATTRIBUTE, changedAttr,
                    removedValues, myModifications);

            if (myModifications.isEmpty()) {
                // This means that the attributes are not equal, but the
                // actual values are the same - thus the order must have
                // changed. This should result in a REPLACE_ATTRIBUTE operation.
                myModifications.add(new ModificationItem(
                        DirContext.REPLACE_ATTRIBUTE, changedAttr));
            }

            modificationList.addAll(myModifications);
        }
    }

    private void collectModifications(int modificationType, Attribute attr,
            Collection values, Collection c) {
        if (values.size() > 0) {
            BasicAttribute modificationAttribute = new BasicAttribute(attr
                    .getID());
            for (Iterator iter = values.iterator(); iter.hasNext();) {
                modificationAttribute.add(iter.next());
            }
            c
                    .add(new ModificationItem(modificationType,
                            modificationAttribute));
        }
    }

    private void collectAttributeValues(Collection valueCollection,
            Attribute attribute) throws NamingException {

        if (attribute == null) {
            return;
        }

        NamingEnumeration attributeValues = attribute.getAll();
        while (attributeValues.hasMoreElements()) {
            Object value = (Object) attributeValues.nextElement();
            valueCollection.add(value);
        }
    }

    /**
     * Compare the existing attribute <code>name</code> with the value in
     * <code>value</code>.
     * <p>
     * Also handles the case where the value has been reset to the original
     * value after a previous change. For example, changing <code>a</code> to
     * <code>b</code> and then back to <code>a</code> again must result in
     * this method returning <code>true</code> so the first change can be
     * overwritten with the latest change. TODO Do the null checks on the value
     * instead
     * 
     * @param name
     *            Name of the original attribute.
     * @param value
     *            Value to check if it has been changed.
     * @return true if there has been a change compared to original attribute,
     *         or a previous update
     */
    private boolean isChanged(String name, Object value) {
        Attribute orig = attrs.get(name);
        Attribute prev = updatedAttrs.get(name);

        // FALSE if both are null it is not changed
        // TODO Also include prev in null check
        if (orig == null && value == null) {
            return false;
        }

        // TRUE if existing value is null or does not contain one value
        if (orig == null || orig.size() != 1) {
            return true;
        }

        // TRUE if existing value is not null and the new one is null
        if (orig != null && value == null) {
            return true;
        }

        // TRUE if we can't access the value
        Object obj = null;
        try {
            obj = orig.get(0);
        } catch (NamingException e) {
            return true;
        }

        if (prev == null) {
            // TRUE if the value is not equal
            return !value.equals(obj);
        } else {
            // TRUE if we can't access the value
            Object prevObj = null;
            try {
                prevObj = prev.get(0);
            } catch (NamingException e) {
                return true;
            }
            // TRUE if the value is not equal
            return !value.equals(obj) || !value.equals(prevObj);
        }
    }

    /**
     * returns true if the attribute is empty. It is empty if a == null, size ==
     * 0 or get() == null or an exception if thrown when accessing the get
     * method
     */
    private boolean isEmptyAttribute(Attribute a) {
        try {
            return (a == null || a.size() == 0 || a.get() == null);
        } catch (NamingException e) {
            return true;
        }
    }

    /**
     * Compare the existing attribute <code>name</code> with the values on the
     * array <code>values</code>. The order of the array must be the same
     * order as the existing multivalued attribute.
     * <p>
     * Also handles the case where the values have been reset to the original
     * values after a previous change. For example, changing
     * <code>[a,b,c]</code> to <code>[a,b]</code> and then back to
     * <code>[a,b,c]</code> again must result in this method returning
     * <code>true</code> so the first change can be overwritten with the
     * latest change.
     * 
     * @param name
     *            Name of the original multi-valued attribute.
     * @param values
     *            Array of values to check if they have been changed.
     * @return true if there has been a change compared to original attribute,
     *         or a previous update
     */
    private boolean isChanged(String name, Object[] values, boolean orderMatters) {

        Attribute orig = attrs.get(name);
        Attribute prev = updatedAttrs.get(name);

        // values == null and values.length == 0 is treated the same way
        boolean emptyNewValue = (values == null || values.length == 0);

        // Setting to empty ---------------------
        if (emptyNewValue) {
            // FALSE: if both are null, it is not changed (both don't exist)
            // TRUE: if new value is null and old value exists (should be
            // removed)
            // TODO Also include prev in null check
            // TODO Also check if there is a single null element
            if (orig != null) {
                return true;
            }
            return false;
        }

        // NOT setting to empty -------------------

        // TRUE if existing value is null
        if (orig == null) {
            return true;
        }

        // TRUE if different length compared to original attributes
        if (orig.size() != values.length) {
            return true;
        }

        // TRUE if different length compared to previously updated attributes
        if (prev != null && prev.size() != values.length) {
            return true;
        }

        // Check contents of arrays

        // Order DOES matter, e.g. first names
        try {
            for (int i = 0; i < orig.size(); i++) {
                Object obj = orig.get(i);
                // TRUE if one value is not equal
                if (!(obj instanceof String)) {
                    return true;
                }
                if (orderMatters) {
                    // check only the string with same index
                    if (!values[i].equals(obj)) {
                        return true;
                    }
                } else {
                    // check all strings
                    if (!ArrayUtils.contains(values, obj)) {
                        return true;
                    }
                }
            }

        } catch (NamingException e) {
            // TRUE if we can't access the value
            return true;
        }

        if (prev != null) {
            // Also check against updatedAttrs, since there might have been
            // a previous update
            try {
                for (int i = 0; i < prev.size(); i++) {
                    Object obj = prev.get(i);
                    // TRUE if one value is not equal
                    if (!(obj instanceof String)) {
                        return true;
                    }
                    if (orderMatters) {
                        // check only the string with same index
                        if (!values[i].equals(obj)) {
                            return true;
                        }
                    } else {
                        // check all strings
                        if (!ArrayUtils.contains(values, obj)) {
                            return true;
                        }
                    }
                }

            } catch (NamingException e) {
                // TRUE if we can't access the value
                return true;
            }
        }
        // FALSE since we have compared all values
        return false;
    }

    /**
     * Checks if an entry has a specific attribute.
     * 
     * This method simply calls exists(String) with the attribute name.
     * 
     * @param attr
     *            the attribute to check.
     * @return true if attribute exists in entry.
     */
    protected final boolean exists(Attribute attr) {
        return exists(attr.getID());
    }

    /**
     * Checks if the attribute exists in this entry, either it was read or it
     * has been added and update() has been called.
     * 
     * @param attrId
     *            id of the attribute to check.
     * @return true if the attribute exists in the entry.
     */
    protected final boolean exists(String attrId) {
        return attrs.get(attrId) != null;
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#getStringAttribute(java.lang.String)
     */
    public String getStringAttribute(String name) {
        return (String) getObjectAttribute(name);
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#getObjectAttribute(java.lang.String)
     */
    public Object getObjectAttribute(String name) {
        Attribute oneAttr = attrs.get(name);
        if (oneAttr == null) {
            return null;
        }
        try {
            return oneAttr.get();
        } catch (NamingException e) {
            throw getExceptionTranslator().translate(e);
        }
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#setAttributeValue(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttributeValue(String name, Object value) {
        // new entry
        if (!updateMode && value != null) {
            attrs.put(name, value);
        }

        // updating entry
        if (updateMode && isChanged(name, value)) {
            BasicAttribute attribute = new BasicAttribute(name);
            if (value != null) {
                attribute.add(value);
            }
            updatedAttrs.put(attribute);
        }
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#setAttributeValues(java.lang.String,
     *      java.lang.Object[])
     */
    public void setAttributeValues(String name, Object[] values) {
        setAttributeValues(name, values, ORDER_DOESNT_MATTER);
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#setAttributeValues(java.lang.String,
     *      java.lang.Object[], boolean)
     */
    public void setAttributeValues(String name, Object[] values,
            boolean orderMatters) {
        Attribute a = new BasicAttribute(name, orderMatters);

        for (int i = 0; values != null && i < values.length; i++) {
            a.add(values[i]);
        }

        // only change the original attribute if not in update mode
        if (!updateMode && values != null && values.length > 0) {
            // don't save empty arrays
            attrs.put(a);
        }

        // possible to set an already existing attribute to an empty array
        if (updateMode && isChanged(name, values, orderMatters)) {
            updatedAttrs.put(a);
        }
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#update()
     */
    public void update() {
        NamingEnumeration attributesEnumeration = null;

        try {
            attributesEnumeration = updatedAttrs.getAll();

            // find what to update
            while (attributesEnumeration.hasMore()) {
                Attribute a = (Attribute) attributesEnumeration.next();

                // if it does not exist it should be added
                if (isEmptyAttribute(a)) {
                    attrs.remove(a.getID());
                } else {
                    // Otherwise it should be set.
                    attrs.put(a);
                }
            }
        } catch (NamingException e) {
            throw getExceptionTranslator().translate(e);
        } finally {
            closeNamingEnumeration(attributesEnumeration);
        }

        // Reset the attributes to be updated
        updatedAttrs = new BasicAttributes(true);
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#getStringAttributes(java.lang.String)
     */
    public String[] getStringAttributes(String name) {
        String[] attributes;

        Attribute attribute = attrs.get(name);
        if (attribute != null && attribute.size() > 0) {
            attributes = new String[attribute.size()];
            for (int i = 0; i < attribute.size(); i++) {
                try {
                    attributes[i] = (String) attribute.get(i);
                } catch (NamingException e) {
                    throw getExceptionTranslator().translate(e);
                }
            }
        } else {
            return null;
        }

        return attributes;
    }

    /*
     * @see org.springframework.ldap.support.DirContextOperations#getAttributeSortedStringSet(java.lang.String)
     */
    public SortedSet getAttributeSortedStringSet(String name) {
        TreeSet attrSet = new TreeSet();

        Attribute attribute = attrs.get(name);
        if (attribute != null) {
            for (int i = 0; i < attribute.size(); i++) {
                try {
                    attrSet.add(attribute.get(i));
                } catch (NamingException e) {
                    throw getExceptionTranslator().translate(e);
                }
            }
        } else {
            return null;
        }

        return attrSet;
    }

    /**
     * Set the supplied attribute.
     * 
     * @param attribute
     *            the attribute to set.
     */
    public void setAttribute(Attribute attribute) {
        attrs.put(attribute);
    }

    /**
     * Get all attributes.
     * 
     * @return all attributes.
     */
    public Attributes getAttributes() {
        return attrs;
    }

    /**
     * @see javax.naming.directory.DirContext#getAttributes(Name)
     */
    public Attributes getAttributes(Name name) throws NamingException {
        return getAttributes(name.toString());
    }

    /**
     * @see javax.naming.directory.DirContext#getAttributes(String)
     */
    public Attributes getAttributes(String name) throws NamingException {
        if (!StringUtils.isEmpty(name)) {
            throw new NameNotFoundException();
        }
        return (Attributes) attrs.clone();
    }

    /**
     * @see javax.naming.directory.DirContext#getAttributes(Name, String[])
     */
    public Attributes getAttributes(Name name, String[] attrIds)
            throws NamingException {
        return getAttributes(name.toString(), attrIds);
    }

    /**
     * @see javax.naming.directory.DirContext#getAttributes(String, String[])
     */
    public Attributes getAttributes(String name, String[] attrIds)
            throws NamingException {
        if (!StringUtils.isEmpty(name)) {
            throw new NameNotFoundException();
        }

        Attributes a = new BasicAttributes(true);
        Attribute target;
        for (int i = 0; i < attrIds.length; i++) {
            target = attrs.get(attrIds[i]);
            if (target != null) {
                a.put(target);
            }
        }

        return a;
    }

    /**
     * @see javax.naming.directory.DirContext#modifyAttributes(javax.naming.Name,
     *      int, javax.naming.directory.Attributes)
     */
    public void modifyAttributes(Name name, int modOp, Attributes attrs)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#modifyAttributes(String, int,
     *      Attributes)
     */
    public void modifyAttributes(String name, int modOp, Attributes attrs)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#modifyAttributes(Name,
     *      ModificationItem[])
     */
    public void modifyAttributes(Name name, ModificationItem[] mods)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#modifyAttributes(String,
     *      ModificationItem[])
     */
    public void modifyAttributes(String name, ModificationItem[] mods)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#bind(Name, Object, Attributes)
     */
    public void bind(Name name, Object obj, Attributes attrs)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#bind(String, Object, Attributes)
     */
    public void bind(String name, Object obj, Attributes attrs)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#rebind(Name, Object, Attributes)
     */
    public void rebind(Name name, Object obj, Attributes attrs)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#rebind(String, Object, Attributes)
     */
    public void rebind(String name, Object obj, Attributes attrs)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#createSubcontext(Name, Attributes)
     */
    public DirContext createSubcontext(Name name, Attributes attrs)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#createSubcontext(String,
     *      Attributes)
     */
    public DirContext createSubcontext(String name, Attributes attrs)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#getSchema(Name)
     */
    public DirContext getSchema(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#getSchema(String)
     */
    public DirContext getSchema(String name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#getSchemaClassDefinition(Name)
     */
    public DirContext getSchemaClassDefinition(Name name)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#getSchemaClassDefinition(String)
     */
    public DirContext getSchemaClassDefinition(String name)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#search(Name, Attributes, String[])
     */
    public NamingEnumeration search(Name name, Attributes matchingAttributes,
            String[] attributesToReturn) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#search(String, Attributes,
     *      String[])
     */
    public NamingEnumeration search(String name, Attributes matchingAttributes,
            String[] attributesToReturn) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#search(Name, Attributes)
     */
    public NamingEnumeration search(Name name, Attributes matchingAttributes)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#search(String, Attributes)
     */
    public NamingEnumeration search(String name, Attributes matchingAttributes)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#search(Name, String,
     *      SearchControls)
     */
    public NamingEnumeration search(Name name, String filter,
            SearchControls cons) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#search(String, String,
     *      SearchControls)
     */
    public NamingEnumeration search(String name, String filter,
            SearchControls cons) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#search(Name, String, Object[],
     *      SearchControls)
     */
    public NamingEnumeration search(Name name, String filterExpr,
            Object[] filterArgs, SearchControls cons) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.directory.DirContext#search(String, String, Object[],
     *      SearchControls)
     */
    public NamingEnumeration search(String name, String filterExpr,
            Object[] filterArgs, SearchControls cons) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#lookup(Name)
     */
    public Object lookup(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#lookup(String)
     */
    public Object lookup(String name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#bind(Name, Object)
     */
    public void bind(Name name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#bind(String, Object)
     */
    public void bind(String name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#rebind(Name, Object)
     */
    public void rebind(Name name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#rebind(String, Object)
     */
    public void rebind(String name, Object obj) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#unbind(Name)
     */
    public void unbind(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#unbind(String)
     */
    public void unbind(String name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#rename(Name, Name)
     */
    public void rename(Name oldName, Name newName) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#rename(String, String)
     */
    public void rename(String oldName, String newName) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#list(Name)
     */
    public NamingEnumeration list(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#list(String)
     */
    public NamingEnumeration list(String name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#listBindings(Name)
     */
    public NamingEnumeration listBindings(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#listBindings(String)
     */
    public NamingEnumeration listBindings(String name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#destroySubcontext(Name)
     */
    public void destroySubcontext(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#destroySubcontext(String)
     */
    public void destroySubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#createSubcontext(Name)
     */
    public Context createSubcontext(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#createSubcontext(String)
     */
    public Context createSubcontext(String name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#lookupLink(Name)
     */
    public Object lookupLink(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#lookupLink(String)
     */
    public Object lookupLink(String name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#getNameParser(Name)
     */
    public NameParser getNameParser(Name name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#getNameParser(String)
     */
    public NameParser getNameParser(String name) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#composeName(Name, Name)
     */
    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#composeName(String, String)
     */
    public String composeName(String name, String prefix)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#addToEnvironment(String, Object)
     */
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#removeFromEnvironment(String)
     */
    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#getEnvironment()
     */
    public Hashtable getEnvironment() throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#close()
     */
    public void close() throws NamingException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * @see javax.naming.Context#getNameInNamespace()
     */
    public String getNameInNamespace() {
        DistinguishedName result = new DistinguishedName(dn);
        result.prepend(base);
        return result.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.DirContextOperations#getDn()
     */
    public Name getDn() {
        return dn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.DirContextOperations#setDn(javax.naming.Name)
     */
    public final void setDn(Name dn) {
        if (!updateMode) {
            this.dn = new DistinguishedName(dn.toString());
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // A subclass with identical values should NOT be considered equal.
        // EqualsBuilder in commons-lang cannot handle subclasses correctly.
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getClass().getName());
        buf.append(":");
        if (dn != null) {
            buf.append(" dn=" + dn);
        }
        buf.append(" {");

        try {
            for (NamingEnumeration i = attrs.getAll(); i.hasMore();) {
                Attribute attribute = (Attribute) i.next();
                if (attribute.size() == 1) {
                    buf.append(attribute.getID());
                    buf.append('=');
                    buf.append(attribute.get());
                } else {
                    for (int j = 0; j < attribute.size(); j++) {
                        if (j > 0) {
                            buf.append(", ");
                        }
                        buf.append(attribute.getID());
                        buf.append('[');
                        buf.append(j);
                        buf.append("]=");
                        buf.append(attribute.get(j));
                    }
                }

                if (i.hasMore()) {
                    buf.append(", ");
                }
            }
        } catch (NamingException e) {
            log.warn("Error in toString()");
        }
        buf.append('}');

        return buf.toString();
    }

    /**
     * Get the NamingExceptionTranslator.
     * 
     * @return the NamingExceptionTranslator to use; if none is specified,
     *         {@link DefaultNamingExceptionTranslator} is used.
     */
    public NamingExceptionTranslator getExceptionTranslator() {
        if (exceptionTranslator == null) {
            exceptionTranslator = new DefaultNamingExceptionTranslator();
        }
        return exceptionTranslator;
    }

    /**
     * Set the NamingExceptionTranslator to use.
     * 
     * @param exceptionTranslator
     *            the NamingExceptionTranslator to use.
     */
    public void setExceptionTranslator(
            NamingExceptionTranslator exceptionTranslator) {
        this.exceptionTranslator = exceptionTranslator;
    }

}
