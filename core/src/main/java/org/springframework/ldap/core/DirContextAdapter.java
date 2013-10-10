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
package org.springframework.ldap.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NoSuchAttributeException;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
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
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Adapter that implements the interesting methods of the DirContext interface.
 * In particular it contains utility methods for getting and setting attributes.
 * Using the
 * {@link org.springframework.ldap.core.support.DefaultDirObjectFactory} in your
 * <code>ContextSource</code> (which is the default) you will receive instances
 * of this class from searches and lookups. This can be particularly useful when
 * updating data, since this class implements
 * {@link AttributeModificationsAware}, providing a
 * {@link #getModificationItems()} method. When in update mode, an object of
 * this class keeps track of the changes made to its attributes, making them
 * available as an array of <code>ModificationItem</code> objects, suitable as
 * input to {@link LdapTemplate#modifyAttributes(DirContextOperations)}.
 * 
 * Note that this is not a complete implementation of DirContext. Several
 * methods are not relevant for the intended usage of this class, so they
 * throw UnsupportOperationException.
 *
 * @see #setAttributeValue(String, Object)
 * @see #setAttributeValues(String, Object[])
 * @see #getStringAttribute(String)
 * @see #getStringAttributes(String)
 * @see #getObjectAttribute(String)
 * @see #addAttributeValue(String, Object)
 * @see #removeAttributeValue(String, Object)
 * @see #setUpdateMode(boolean)
 * @see #isUpdateMode()
 * 
 * @author Magnus Robertsson
 * @author Andreas Ronge
 * @author Adam Skogman
 * @author Mattias Hellborg Arthursson
 */
public class DirContextAdapter implements DirContextOperations {

	private static final boolean DONT_ADD_IF_DUPLICATE_EXISTS = false;

	private static final String EMPTY_STRING = "";

	private static final boolean ORDER_DOESNT_MATTER = false;

	private static Logger log = LoggerFactory.getLogger(DirContextAdapter.class);

	private final Attributes originalAttrs;

	private LdapName dn;

	private LdapName base = LdapUtils.emptyLdapName();

	private boolean updateMode = false;

	private Attributes updatedAttrs;

	private String referralUrl;

	/**
	 * Default constructor.
	 */
	public DirContextAdapter() {
		this(null, null, null);
	}

	/**
	 * Create a new DirContextAdapter from the supplied DN String.
	 * @param dnString the DN string. Must be syntactically correct, or an
	 * exception will be thrown.
	 */
	public DirContextAdapter(String dnString) {
		this(LdapUtils.newLdapName(dnString));
	}

	/**
	 * Create a new adapter from the supplied dn.
	 * 
	 * @param dn the dn.
	 */
	public DirContextAdapter(Name dn) {
		this(null, dn);
	}

	/**
	 * Create a new adapter from the supplied attributes and dn.
	 * 
	 * @param attrs the attributes.
	 * @param dn the dn.
	 */
	public DirContextAdapter(Attributes attrs, Name dn) {
		this(attrs, dn, null);
	}

	/**
	 * Create a new adapter from the supplied attributes, dn, and base.
	 * 
	 * @param attrs the attributes.
	 * @param dn the dn.
	 * @param base the base name.
	 */
	public DirContextAdapter(Attributes attrs, Name dn, Name base) {
		this(attrs, dn, base, null);
	}

	/**
	 * Create a new adapter from the supplied attributes, dn, base, and referral
	 * url.
	 * @param attrs the attributes.
	 * @param dn the dn.
	 * @param base the base.
	 * @param referralUrl the referral url (if this instance results from a
	 * referral).
	 */
	public DirContextAdapter(Attributes attrs, Name dn, Name base,
			String referralUrl) {
		if (attrs != null) {
			this.originalAttrs = attrs;
		}
		else {
			this.originalAttrs = new BasicAttributes(true);
		}

        if (dn != null) {
            this.dn = LdapUtils.newLdapName(dn);
        }
        else {
            this.dn = LdapUtils.emptyLdapName();
        }
        if (base != null) {
            this.base = LdapUtils.newLdapName(base);
        }
        else {
            this.base = LdapUtils.emptyLdapName();
        }

        if (referralUrl != null) {
			this.referralUrl = referralUrl;
		}
		else {
			this.referralUrl = EMPTY_STRING;
		}
	}

	/**
	 * Constructor for cloning an existing adapter.
	 * 
	 * @param master The adapter to be copied.
	 */
	protected DirContextAdapter(DirContextAdapter master) {
		this.originalAttrs = (Attributes) master.originalAttrs.clone();
		this.dn = master.dn;
		this.updatedAttrs = (Attributes) master.updatedAttrs.clone();
		this.updateMode = master.updateMode;
	}

	/**
	 * Sets the update mode. The update mode should be <code>false</code> for a
	 * new entry and <code>true</code> for an existing entry that is being
	 * updated.
	 * 
	 * @param mode Update mode.
	 */
	public void setUpdateMode(boolean mode) {
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
	 * @seeorg.springframework.ldap.support.DirContextOperations#
	 * getNamesOfModifiedAttributes()
	 */
	public String[] getNamesOfModifiedAttributes() {

		List<String> tmpList = new ArrayList<String>();

		NamingEnumeration<? extends Attribute> attributesEnumeration;
		if (isUpdateMode()) {
			attributesEnumeration = updatedAttrs.getAll();
		}
		else {
			attributesEnumeration = originalAttrs.getAll();
		}

		try {
			while (attributesEnumeration.hasMore()) {
				Attribute oneAttribute = attributesEnumeration
						.next();
				tmpList.add(oneAttribute.getID());
			}
		}
		catch (NamingException e) {
			throw LdapUtils.convertLdapException(e);
		}
		finally {
			closeNamingEnumeration(attributesEnumeration);
		}

		return tmpList.toArray(new String[tmpList.size()]);
	}

	private void closeNamingEnumeration(NamingEnumeration<?> enumeration) {
		try {
			if (enumeration != null) {
				enumeration.close();
			}
		}
		catch (NamingException e) {
			// Never mind this
		}
	}

	/*
	 * @seeorg.springframework.ldap.support.AttributeModificationsAware#
	 * getModificationItems()
	 */
	public ModificationItem[] getModificationItems() {
		if (!updateMode) {
			return new ModificationItem[0];
		}

		List<ModificationItem> tmpList = new LinkedList<ModificationItem>();
		NamingEnumeration<? extends Attribute> attributesEnumeration = null;
		try {
			attributesEnumeration = updatedAttrs.getAll();

			// find attributes that have been changed, removed or added
			while (attributesEnumeration.hasMore()) {
				Attribute oneAttr = attributesEnumeration.next();

				collectModifications(oneAttr, tmpList);
			}
		}
		catch (NamingException e) {
			throw LdapUtils.convertLdapException(e);
		}
		finally {
			closeNamingEnumeration(attributesEnumeration);
		}

		if (log.isDebugEnabled()) {
			log.debug("Number of modifications:" + tmpList.size());
		}

		return tmpList.toArray(new ModificationItem[tmpList.size()]);
	}

	/**
	 * Collect all modifications for the changed attribute. If no changes have
	 * been made, return immediately. If modifications have been made, and the
	 * original size as well as the updated size of the attribute is 1, replace
	 * the attribute. If the size of the updated attribute is 0, remove the
	 * attribute. Otherwise, the attribute is a multi-value attribute; if it's
	 * an ordered one it should be replaced in its entirety to preserve the new
	 * ordering, if not all modifications to the original value (removals and
	 * additions) will be collected individually.
	 * 
	 * @param changedAttr the value of the changed attribute.
	 * @param modificationList the list in which to add the modifications.
	 * @throws NamingException if thrown by called Attribute methods.
	 */
	private void collectModifications(Attribute changedAttr,
			List<ModificationItem> modificationList) throws NamingException {
		Attribute currentAttribute = originalAttrs.get(changedAttr.getID());

		if (changedAttr.equals(currentAttribute)) {
			// No changes
			return;
		}
		else if (currentAttribute != null && currentAttribute.size() == 1
				&& changedAttr.size() == 1) {
			// Replace single-vale attribute.
			modificationList.add(new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE, changedAttr));
		}
		else if (changedAttr.size() == 0 && currentAttribute != null) {
			// Attribute has been removed.
			modificationList.add(new ModificationItem(
					DirContext.REMOVE_ATTRIBUTE, changedAttr));
		}
		else if ((currentAttribute == null || currentAttribute.size() == 0)
				&& changedAttr.size() > 0) {
			// Attribute has been added.
			modificationList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE,
					changedAttr));
		}
		else if (changedAttr.size() > 0 && changedAttr.isOrdered()) {
			// This is a multivalue attribute and it is ordered - the original
			// value should be replaced with the new values so that the ordering
			// is preserved.
			modificationList.add(new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE, changedAttr));
		}
		else if (changedAttr.size() > 0) {
			// Change of multivalue Attribute. Collect additions and removals
			// individually.
			List<ModificationItem> myModifications = new LinkedList<ModificationItem>();
			collectModifications(currentAttribute, changedAttr, myModifications);

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

	private void collectModifications(Attribute originalAttr,
			Attribute changedAttr, List<ModificationItem> modificationList)
			throws NamingException {

		Attribute originalClone = (Attribute) originalAttr.clone();
		Attribute addedValuesAttribute = new BasicAttribute(originalAttr
				.getID());

		for (int i = 0; i < changedAttr.size(); i++) {
			Object attributeValue = changedAttr.get(i);
			if (!originalClone.remove(attributeValue)) {
				addedValuesAttribute.add(attributeValue);
			}
		}

        // We have now traversed and removed all values from the original that
        // were also present in the new values. The remaining values in the
        // original must be the ones that were removed.
        if(originalClone.size() > 0 && originalClone.size() == originalAttr.size()) {
            // This is actually a complete replacement of the attribute values.
            // Fall back to REPLACE
            modificationList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    addedValuesAttribute));
        } else {
            if (originalClone.size() > 0) {
                modificationList.add(new ModificationItem(
                        DirContext.REMOVE_ATTRIBUTE, originalClone));
            }

            if (addedValuesAttribute.size() > 0) {
                modificationList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE,
                        addedValuesAttribute));
            }
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
		}
		catch (NamingException e) {
			return true;
		}
	}

	/**
	 * Compare the existing attribute <code>name</code> with the values on the
	 * array <code>values</code>. The order of the array must be the same order
	 * as the existing multivalued attribute.
	 * <p>
	 * Also handles the case where the values have been reset to the original
	 * values after a previous change. For example, changing
	 * <code>[a,b,c]</code> to <code>[a,b]</code> and then back to
	 * <code>[a,b,c]</code> again must result in this method returning
	 * <code>true</code> so the first change can be overwritten with the latest
	 * change.
	 * 
	 * @param name Name of the original multi-valued attribute.
	 * @param values Array of values to check if they have been changed.
	 * @return true if there has been a change compared to original attribute,
	 * or a previous update
	 */
	private boolean isChanged(String name, Object[] values, boolean orderMatters) {

		Attribute orig = originalAttrs.get(name);
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
            return orig != null;
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
				}
				else {
					// check all strings
					if (!ObjectUtils.containsElement(values, obj)) {
						return true;
					}
				}
			}

		}
		catch (NamingException e) {
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
					}
					else {
						// check all strings
						if (!ObjectUtils.containsElement(values, obj)) {
							return true;
						}
					}
				}

			}
			catch (NamingException e) {
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
	 * @param attr the attribute to check.
	 * @return true if attribute exists in entry.
	 */
	protected final boolean exists(Attribute attr) {
		return exists(attr.getID());
	}

	/**
	 * Checks if the attribute exists in this entry, either it was read or it
	 * has been added and update() has been called.
	 * 
	 * @param attrId id of the attribute to check.
	 * @return true if the attribute exists in the entry.
	 */
	protected final boolean exists(String attrId) {
		return originalAttrs.get(attrId) != null;
	}

	/*
	 * @see
	 * org.springframework.ldap.support.DirContextOperations#getStringAttribute
	 * (java.lang.String)
	 */
	public String getStringAttribute(String name) {
		return (String) getObjectAttribute(name);
	}

	/*
	 * @see org.springframework.ldap.support.DirContextOperations#getObjectAttribute
	 * (java.lang.String)
	 */
	public Object getObjectAttribute(String name) {
		Attribute oneAttr = originalAttrs.get(name);
		if (oneAttr == null || oneAttr.size() == 0) { // LDAP-215
			return null;
		}
		try {
			return oneAttr.get();
		}
		catch (NamingException e) {
			throw LdapUtils.convertLdapException(e);
		}
	}

	// LDAP-215
	/* (non-Javadoc)
	 * @see org.springframework.ldap.core.DirContextOperations#attributeExists(java.lang.String)
	 */
	public boolean attributeExists(String name) {
		Attribute oneAttr = originalAttrs.get(name);
        return oneAttr != null;
	}
	
	/*
	 * @see
	 * org.springframework.ldap.support.DirContextOperations#setAttributeValue
	 * (java.lang.String, java.lang.Object)
	 */
	public void setAttributeValue(String name, Object value) {
		// new entry
		if (!updateMode && value != null) {
			originalAttrs.put(name, value);
		}

		// updating entry
		if (updateMode) {
			BasicAttribute attribute = new BasicAttribute(name);
			if (value != null) {
				attribute.add(value);
			}
			updatedAttrs.put(attribute);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ldap.core.DirContextOperations#addAttributeValue(
	 * java.lang.String, java.lang.Object)
	 */
	public void addAttributeValue(String name, Object value) {
		addAttributeValue(name, value, DONT_ADD_IF_DUPLICATE_EXISTS);
	}

	public void addAttributeValue(String name, Object value,
			boolean addIfDuplicateExists) {
		if (!updateMode && value != null) {
			Attribute attr = originalAttrs.get(name);
			if (attr == null) {
				originalAttrs.put(name, value);
			}
			else {
				attr.add(value);
			}
		}
		else if (updateMode) {
			Attribute attr = updatedAttrs.get(name);
			if (attr == null) {
				if (originalAttrs.get(name) == null) {
					// No match in the original attributes -
					// add a new Attribute to updatedAttrs
					updatedAttrs.put(name, value);
				}
				else {
					// The attribute exists in the original attributes - clone
					// that and add the new entry to it
					attr = (Attribute) originalAttrs.get(name).clone();
					if (addIfDuplicateExists || !attr.contains(value)) {
						attr.add(value);
					}
					updatedAttrs.put(attr);
				}
			}
			else {
				attr.add(value);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ldap.core.DirContextOperations#removeAttributeValue
	 * (java.lang.String, java.lang.Object)
	 */
	public void removeAttributeValue(String name, Object value) {
		if (!updateMode && value != null) {
			Attribute attr = originalAttrs.get(name);
			if (attr != null) {
				attr.remove(value);
				if (attr.size() == 0) {
					originalAttrs.remove(name);
				}
			}
		}
		else if (updateMode) {
			Attribute attr = updatedAttrs.get(name);
			if (attr == null) {
				if (originalAttrs.get(name) != null) {
					attr = (Attribute) originalAttrs.get(name).clone();
					attr.remove(value);
					updatedAttrs.put(attr);
				}
			}
			else {
				attr.remove(value);
			}
		}
	}

	/*
	 * @see
	 * org.springframework.ldap.support.DirContextOperations#setAttributeValues
	 * (java.lang.String, java.lang.Object[])
	 */
	public void setAttributeValues(String name, Object[] values) {
		setAttributeValues(name, values, ORDER_DOESNT_MATTER);
	}

	/*
	 * @see
	 * org.springframework.ldap.support.DirContextOperations#setAttributeValues
	 * (java.lang.String, java.lang.Object[], boolean)
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
			originalAttrs.put(a);
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
		NamingEnumeration<? extends Attribute> attributesEnumeration = null;

		try {
			attributesEnumeration = updatedAttrs.getAll();

			// find what to update
			while (attributesEnumeration.hasMore()) {
				Attribute a = attributesEnumeration.next();

				// if it does not exist it should be added
				if (isEmptyAttribute(a)) {
					originalAttrs.remove(a.getID());
				}
				else {
					// Otherwise it should be set.
					originalAttrs.put(a);
				}
			}
		}
		catch (NamingException e) {
			throw LdapUtils.convertLdapException(e);
		}
		finally {
			closeNamingEnumeration(attributesEnumeration);
		}

		// Reset the attributes to be updated
		updatedAttrs = new BasicAttributes(true);
	}

	/*
	 * @see
	 * org.springframework.ldap.core.DirContextOperations#getStringAttributes
	 * (java.lang.String)
	 */
	public String[] getStringAttributes(String name) {
		try {
            List<String> objects = collectAttributeValuesAsList(name, String.class);
            return objects.toArray(new String[objects.size()]);
		}
		catch (NoSuchAttributeException e) {
			// The attribute does not exist - contract says to return null.
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ldap.core.DirContextOperations#getObjectAttributes
	 * (java.lang.String)
	 */
	public Object[] getObjectAttributes(String name) {
		try {
            List<Object> list = collectAttributeValuesAsList(name, Object.class);
            return list.toArray(new Object[list.size()]);
		}
		catch (NoSuchAttributeException e) {
			// The attribute does not exist - contract says to return null.
			return null;
		}
	}

	private <T> List<T> collectAttributeValuesAsList(String name, Class<T> clazz) {
		List<T> list = new LinkedList<T>();
		LdapUtils.collectAttributeValues(originalAttrs, name, list, clazz);
		return list;
	}

    @Override
	public SortedSet<String> getAttributeSortedStringSet(String name) {
		try {
			TreeSet<String> attrSet = new TreeSet<String>();
			LdapUtils.collectAttributeValues(originalAttrs, name, attrSet, String.class);
			return attrSet;
		}
		catch (NoSuchAttributeException e) {
			// The attribute does not exist - contract says to return null.
			return null;
		}
	}

	/**
	 * Set the supplied attribute.
	 * 
	 * @param attribute the attribute to set.
	 */
	public void setAttribute(Attribute attribute) {
		if (!updateMode) {
			originalAttrs.put(attribute);
		}
		else {
			updatedAttrs.put(attribute);
		}
	}

	/**
	 * Get all attributes.
	 * 
	 * @return all attributes.
	 */
	public Attributes getAttributes() {
		return originalAttrs;
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
		if (StringUtils.hasLength(name)) {
			throw new NameNotFoundException();
		}
		return (Attributes) originalAttrs.clone();
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
		if (StringUtils.hasLength(name)) {
			throw new NameNotFoundException();
		}

		Attributes a = new BasicAttributes(true);
		Attribute target;
        for (String attrId : attrIds) {
            target = originalAttrs.get(attrId);
            if (target != null) {
                a.put(target);
            }
        }

		return a;
	}

	/**
	 * @see javax.naming.directory.DirContext#modifyAttributes(javax.naming.Name,
	 * int, javax.naming.directory.Attributes)
	 */
	public void modifyAttributes(Name name, int modOp, Attributes attrs)
			throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#modifyAttributes(String, int,
	 * Attributes)
	 */
	public void modifyAttributes(String name, int modOp, Attributes attrs)
			throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#modifyAttributes(Name,
	 * ModificationItem[])
	 */
	public void modifyAttributes(Name name, ModificationItem[] mods)
			throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#modifyAttributes(String,
	 * ModificationItem[])
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
	 * Attributes)
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
	public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes,
			String[] attributesToReturn) throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#search(String, Attributes,
	 * String[])
	 */
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes,
			String[] attributesToReturn) throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#search(Name, Attributes)
	 */
	public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes)
			throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#search(String, Attributes)
	 */
	public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes)
			throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#search(Name, String,
	 * SearchControls)
	 */
	public NamingEnumeration<SearchResult> search(Name name, String filter,
			SearchControls cons) throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#search(String, String,
	 * SearchControls)
	 */
	public NamingEnumeration<SearchResult> search(String name, String filter,
			SearchControls cons) throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#search(Name, String, Object[],
	 * SearchControls)
	 */
	public NamingEnumeration<SearchResult> search(Name name, String filterExpr,
			Object[] filterArgs, SearchControls cons) throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.directory.DirContext#search(String, String, Object[],
	 * SearchControls)
	 */
	public NamingEnumeration<SearchResult> search(String name, String filterExpr,
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
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.Context#list(String)
	 */
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.Context#listBindings(Name)
	 */
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * @see javax.naming.Context#listBindings(String)
	 */
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
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
	public Hashtable<?, ?> getEnvironment() throws NamingException {
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
        if(base.size() == 0) {
            return dn.toString();
        }

        try {
            LdapName result = (LdapName) dn.clone();
            result.addAll(0, base);
            return result.toString();
        } catch (InvalidNameException e) {
            throw new org.springframework.ldap.InvalidNameException(e);
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.ldap.support.DirContextOperations#getDn()
	 */
	public Name getDn() {
		return LdapUtils.newLdapName(dn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ldap.support.DirContextOperations#setDn(javax.naming
	 * .Name)
	 */
	public final void setDn(Name dn) {
		if (!updateMode) {
            this.dn = LdapUtils.newLdapName(dn);
        }
		else {
			throw new IllegalStateException(
					"Not possible to call setDn() on a DirContextAdapter in update mode");
		}

	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirContextAdapter that = (DirContextAdapter) o;

        if (updateMode != that.updateMode) return false;
        if (base != null ? !base.equals(that.base) : that.base != null) return false;
        if (dn != null ? !dn.equals(that.dn) : that.dn != null) return false;
        if (originalAttrs != null ? !originalAttrs.equals(that.originalAttrs) : that.originalAttrs != null)
            return false;
        if (referralUrl != null ? !referralUrl.equals(that.referralUrl) : that.referralUrl != null) return false;
        if (updatedAttrs != null ? !updatedAttrs.equals(that.updatedAttrs) : that.updatedAttrs != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = originalAttrs != null ? originalAttrs.hashCode() : 0;
        result = 31 * result + (dn != null ? dn.hashCode() : 0);
        result = 31 * result + (base != null ? base.hashCode() : 0);
        result = 31 * result + (updateMode ? 1 : 0);
        result = 31 * result + (updatedAttrs != null ? updatedAttrs.hashCode() : 0);
        result = 31 * result + (referralUrl != null ? referralUrl.hashCode() : 0);
        return result;
    }

    /**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		builder.append(":");
		if (dn != null) {
			builder.append(" dn=").append(dn);
		}
		builder.append(" {");

		try {
			for (NamingEnumeration<? extends Attribute> i = originalAttrs.getAll(); i.hasMore();) {
				Attribute attribute = i.next();
				if (attribute.size() == 1) {
					builder.append(attribute.getID());
					builder.append('=');
					builder.append(attribute.get());
				}
				else {
					for (int j = 0; j < attribute.size(); j++) {
						if (j > 0) {
							builder.append(", ");
						}
						builder.append(attribute.getID());
						builder.append('[');
						builder.append(j);
						builder.append("]=");
						builder.append(attribute.get(j));
					}
				}

				if (i.hasMore()) {
					builder.append(", ");
				}
			}
		}
		catch (NamingException e) {
			log.warn("Error in toString()");
		}
		builder.append('}');

		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.ldap.core.DirContextOperations#getReferralUrl()
	 */
	public String getReferralUrl() {
		return referralUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.ldap.core.DirContextOperations#isReferral()
	 */
	public boolean isReferral() {
		return StringUtils.hasLength(referralUrl);
	}

}
