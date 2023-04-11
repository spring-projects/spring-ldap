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

package org.springframework.ldap.odm.core.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.LdapDataEntry;
import org.springframework.core.SpringVersion;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.odm.typeconversion.ConverterManager;
import org.springframework.ldap.odm.typeconversion.impl.ConversionServiceConverterManager;
import org.springframework.ldap.odm.typeconversion.impl.ConverterManagerImpl;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Default implementation of {@link ObjectDirectoryMapper}. Unless you need to explicitly
 * configure converters there is typically no reason to explicitly consider yourself with
 * this class.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class DefaultObjectDirectoryMapper implements ObjectDirectoryMapper {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultObjectDirectoryMapper.class);

	// The converter manager to use to translate values between LDAP and Java
	private ConverterManager converterManager;

	private static final String OBJECT_CLASS_ATTRIBUTE = "objectclass";

	private static final CaseIgnoreString OBJECT_CLASS_ATTRIBUTE_CI = new CaseIgnoreString(OBJECT_CLASS_ATTRIBUTE);

	public DefaultObjectDirectoryMapper() {
		this.converterManager = createDefaultConverterManager();
	}

	private static ConverterManager createDefaultConverterManager() {
		String springVersion = SpringVersion.getVersion();
		if (springVersion == null) {
			LOG.debug(
					"Could not determine the Spring Version. Guessing > Spring 3.0. If this does not work, please ensure to explicitly set converterManager");
			return new ConversionServiceConverterManager();
		}
		else if (springVersion.compareTo("3.0") > 0) {
			return new ConversionServiceConverterManager();
		}
		else {
			return new ConverterManagerImpl();
		}
	}

	public void setConverterManager(ConverterManager converterManager) {
		this.converterManager = converterManager;
	}

	static final class EntityData {

		final ObjectMetaData metaData;

		final Filter ocFilter;

		private EntityData(ObjectMetaData metaData, Filter ocFilter) {
			this.metaData = metaData;
			this.ocFilter = ocFilter;
		}

	}

	// A map of managed classes to to meta data about those classes
	private final ConcurrentMap<Class<?>, EntityData> metaDataMap = new ConcurrentHashMap<Class<?>, EntityData>();

	private EntityData getEntityData(Class<?> managedClass) {
		EntityData result = this.metaDataMap.get(managedClass);
		if (result == null) {
			return addManagedClass(managedClass);
		}
		return result;
	}

	@Override
	public String[] manageClass(Class<?> clazz) {
		// This throws exception if data is invalid
		EntityData entityData = getEntityData(clazz);
		Set<String> managedAttributeNames = new HashSet<String>();
		// extract all relevant attributes
		for (Field field : entityData.metaData) {
			AttributeMetaData attributeMetaData = entityData.metaData.getAttribute(field);
			// skip transient fields
			if (attributeMetaData.isTransient()) {
				continue;
			}
			String[] attributesOfField = attributeMetaData.getAttributes();
			if (attributesOfField != null && attributesOfField.length > 0) {
				// attribute names are either given through annotation
				managedAttributeNames.addAll(Arrays.asList(attributesOfField));
			}
			else {
				// or implicitly by relying on the field name
				managedAttributeNames.add(field.getName());
			}
		}
		// always add the mandatory attribute objectclass (which is always used for the
		// mapping)
		managedAttributeNames.add(OBJECT_CLASS_ATTRIBUTE);
		return managedAttributeNames.toArray(new String[managedAttributeNames.size()]);
	}

	/**
	 * Adds an {@link org.springframework.ldap.odm.annotations} annotated class to the set
	 * managed by this OdmManager.
	 * @param managedClass The class to add to the managed set.
	 */
	private EntityData addManagedClass(Class<?> managedClass) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Adding class %1$s to managed set", managedClass));
		}

		// Extract the meta-data from the class
		ObjectMetaData metaData = new ObjectMetaData(managedClass);

		// Check we can construct the target type - it must have a zero argument public
		// constructor
		try {
			managedClass.getConstructor();
		}
		catch (NoSuchMethodException e) {
			throw new InvalidEntryException(
					String.format("The class %1$s must have a zero argument constructor to be an Entry", managedClass),
					e);
		}

		// Check we have all of the necessary converters for the class
		for (Field field : metaData) {
			AttributeMetaData attributeInfo = metaData.getAttribute(field);
			if (!attributeInfo.isTransient() && !attributeInfo.isId() && !(attributeInfo.isObjectClass())) {
				verifyConversion(managedClass, field, attributeInfo);
			}
		}

		// Filter so we only read the object classes supported by the managedClass
		AndFilter ocFilter = new AndFilter();
		for (CaseIgnoreString oc : metaData.getObjectClasses()) {
			ocFilter.and(new EqualsFilter(OBJECT_CLASS_ATTRIBUTE, oc.toString()));
		}

		EntityData newValue = new EntityData(metaData, ocFilter);
		EntityData previousValue = this.metaDataMap.putIfAbsent(managedClass, newValue);
		// Just in case someone beat us to it
		if (previousValue != null) {
			return previousValue;
		}

		return newValue;
	}

	private void verifyConversion(Class<?> managedClass, Field field, AttributeMetaData attributeInfo) {
		Class<?> jndiClass = attributeInfo.getJndiClass();
		Class<?> javaClass = attributeInfo.getValueClass();
		if (!this.converterManager.canConvert(jndiClass, attributeInfo.getSyntax(), javaClass)) {
			throw new InvalidEntryException(
					String.format("Missing converter from %1$s to %2$s, this is needed for field %3$s on Entry %4$s",
							jndiClass, javaClass, field.getName(), managedClass));
		}
		if (!attributeInfo.isReadOnly()
				&& !this.converterManager.canConvert(javaClass, attributeInfo.getSyntax(), jndiClass)) {
			throw new InvalidEntryException(
					String.format("Missing converter from %1$s to %2$s, this is needed for field %3$s on Entry %4$s",
							javaClass, jndiClass, field.getName(), managedClass));
		}
	}

	@Override
	public void mapToLdapDataEntry(Object entry, LdapDataEntry context) {
		ObjectMetaData metaData = getEntityData(entry.getClass()).metaData;

		Attribute objectclassAttribute = context.getAttributes().get(OBJECT_CLASS_ATTRIBUTE);
		if (objectclassAttribute == null || objectclassAttribute.size() == 0) {
			// Object classes are set from the metadata obtained from the @Entity
			// annotation,
			// but only if this is a new entry.
			int numOcs = metaData.getObjectClasses().size();
			CaseIgnoreString[] metaDataObjectClasses = metaData.getObjectClasses()
					.toArray(new CaseIgnoreString[numOcs]);

			String[] stringOcs = new String[numOcs];
			for (int ocIndex = 0; ocIndex < numOcs; ocIndex++) {
				stringOcs[ocIndex] = metaDataObjectClasses[ocIndex].toString();
			}

			context.setAttributeValues(OBJECT_CLASS_ATTRIBUTE, stringOcs);
		}

		// Loop through each of the fields in the object to write to LDAP
		for (Field field : metaData) {
			// Grab the meta data for the current field
			AttributeMetaData attributeInfo = metaData.getAttribute(field);
			// We dealt with the object class field about, and the DN is set by the call
			// to write the object to LDAP
			if (!attributeInfo.isTransient() && !attributeInfo.isId() && !(attributeInfo.isObjectClass())
					&& !(attributeInfo.isReadOnly())) {
				try {
					// If this is a "binary" object the JNDI expects a byte[] otherwise a
					// String
					Class<?> targetClass = attributeInfo.getJndiClass();
					// Multi valued?
					if (!attributeInfo.isCollection()) {
						populateSingleValueAttribute(entry, context, field, attributeInfo, targetClass);

					}
					else {
						// Multi-valued
						populateMultiValueAttribute(entry, context, field, attributeInfo, targetClass);

					}
				}
				catch (IllegalAccessException e) {
					throw new InvalidEntryException(String.format("Can't set attribute %1$s", attributeInfo.getName()),
							e);
				}
			}
		}
	}

	private void populateMultiValueAttribute(Object entry, LdapDataEntry context, Field field,
			AttributeMetaData attributeInfo, Class<?> targetClass) throws IllegalAccessException {
		// We need to build up a list of of the values
		List<Object> attributeValues = new ArrayList<Object>();
		// Get the list of values
		Collection<?> fieldValues = (Collection<?>) field.get(entry);
		// Ignore null lists
		if (fieldValues != null) {
			for (final Object o : fieldValues) {
				// Ignore null values
				if (o != null) {
					attributeValues.add(this.converterManager.convert(o, attributeInfo.getSyntax(), targetClass));
				}
			}
			context.setAttributeValues(attributeInfo.getName().toString(), attributeValues.toArray());
		}
	}

	private void populateSingleValueAttribute(Object entry, LdapDataEntry context, Field field,
			AttributeMetaData attributeInfo, Class<?> targetClass) throws IllegalAccessException {
		// Single valued - get the value of the field
		Object fieldValue = field.get(entry);
		// Ignore null field values
		if (fieldValue != null) {
			// Convert the field value to the required type and write it into the JNDI
			// context
			context.setAttributeValue(attributeInfo.getName().toString(),
					this.converterManager.convert(fieldValue, attributeInfo.getSyntax(), targetClass));
		}
		else {
			context.setAttributeValue(attributeInfo.getName().toString(), null);
		}
	}

	@Override
	public <T> T mapFromLdapDataEntry(LdapDataEntry context, Class<T> clazz) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Converting to Java Entry class %1$s from %2$s", clazz, context));
		}

		// The Java representation of the LDAP entry
		T result;

		ObjectMetaData metaData = getEntityData(clazz).metaData;

		try {
			// The result class must have a zero argument constructor
			result = clazz.newInstance();

			// Build a map of JNDI attribute names to values
			Map<CaseIgnoreString, Attribute> attributeValueMap = new HashMap<CaseIgnoreString, Attribute>();
			// Get a NamingEnumeration to loop through the JNDI attributes in the entry
			Attributes attributes = context.getAttributes();
			NamingEnumeration<? extends Attribute> attributesEnumeration = attributes.getAll();
			// Loop through all of the JNDI attributes
			while (attributesEnumeration.hasMoreElements()) {
				Attribute currentAttribute = attributesEnumeration.nextElement();
				// Add the current attribute to the map keyed on the lowercased (case
				// indep) id of the attribute
				attributeValueMap.put(new CaseIgnoreString(currentAttribute.getID()), currentAttribute);
			}

			// If this is the objectclass attribute then check that values correspond to
			// the metadata we have
			// for the Java representation
			Attribute ocAttribute = attributeValueMap.get(OBJECT_CLASS_ATTRIBUTE_CI);
			if (ocAttribute != null) {
				// Get all object class values from the JNDI attribute
				Set<CaseIgnoreString> objectClassesFromJndi = new HashSet<CaseIgnoreString>();
				NamingEnumeration<?> objectClassesFromJndiEnum = ocAttribute.getAll();
				while (objectClassesFromJndiEnum.hasMoreElements()) {
					objectClassesFromJndi.add(new CaseIgnoreString((String) objectClassesFromJndiEnum.nextElement()));
				}
				// OK - checks its the same as the meta-data we have
				if (!collectionContainsAll(objectClassesFromJndi, metaData.getObjectClasses())) {
					return null;
				}
			}
			else {
				throw new InvalidEntryException(
						String.format("No object classes were returned for class %1$s", clazz.getName()));
			}

			// Now loop through all the fields in the Java representation populating it
			// with values from the
			// attributeValueMap
			for (Field field : metaData) {
				// Get the current field
				AttributeMetaData attributeInfo = metaData.getAttribute(field);
				// We deal with the Id field specially
				Name dn = context.getDn();
				if (!attributeInfo.isTransient() && !attributeInfo.isId()) {
					// Not the ID - but is is multi valued?
					if (!attributeInfo.isCollection()) {
						// No - its single valued, grab the JNDI attribute that
						// corresponds to the metadata on the
						// current field
						populateSingleValueField(result, attributeValueMap, field, attributeInfo);
					}
					else {
						// We are dealing with a multi valued attribute
						populateMultiValueField(result, attributeValueMap, field, attributeInfo);
					}
				}
				else if (attributeInfo.isId()) { // The id field
					field.set(result, this.converterManager.convert(dn, attributeInfo.getSyntax(),
							attributeInfo.getValueClass()));
				}

				DnAttribute dnAttribute = attributeInfo.getDnAttribute();
				if (dnAttribute != null) {
					String dnValue;
					int index = dnAttribute.index();

					if (index != -1) {
						dnValue = LdapUtils.getStringValue(dn, index);
					}
					else {
						dnValue = LdapUtils.getStringValue(dn, dnAttribute.value());
					}
					field.set(result, dnValue);
				}
			}
		}
		catch (NamingException ne) {
			throw new InvalidEntryException(String.format("Problem creating %1$s from LDAP Entry %2$s", clazz, context),
					ne);
		}
		catch (IllegalAccessException iae) {
			throw new InvalidEntryException(
					String.format("Could not create an instance of %1$s could not access field", clazz.getName()), iae);
		}
		catch (InstantiationException ie) {
			throw new InvalidEntryException(String.format("Could not instantiate %1$s", clazz), ie);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Converted object - %1$s", result));
		}

		return result;
	}

	private <T> void populateMultiValueField(T result, Map<CaseIgnoreString, Attribute> attributeValueMap, Field field,
			AttributeMetaData attributeInfo) throws NamingException, IllegalAccessException {
		// We need to build up a list of values
		Collection<Object> fieldValues = attributeInfo.newCollectionInstance();
		// Grab the attribute from the JNDI representation
		Attribute currentAttribute = attributeValueMap.get(attributeInfo.getName());
		// There is no guarantee that this attribute is present in the directory - so
		// ignore nulls
		if (currentAttribute != null) {
			// Loop through the values of the JNDI attribute
			NamingEnumeration<?> valuesEmumeration = currentAttribute.getAll();
			while (valuesEmumeration.hasMore()) {
				// Get the current value
				Object value = valuesEmumeration.nextElement();
				// Check the value is not null
				if (value != null) {
					// Convert the value to its Java representation and add it to our
					// working list
					fieldValues.add(this.converterManager.convert(value, attributeInfo.getSyntax(),
							attributeInfo.getValueClass()));
				}
			}
		}
		// Now we need to set the List in to a Java object
		field.set(result, fieldValues);
	}

	private <T> void populateSingleValueField(T result, Map<CaseIgnoreString, Attribute> attributeValueMap, Field field,
			AttributeMetaData attributeInfo) throws NamingException, IllegalAccessException {
		Attribute attribute = attributeValueMap.get(attributeInfo.getName());
		// There is no guarantee that this attribute is present in the directory - so
		// ignore nulls
		if (attribute != null) {
			// Grab the JNDI value
			Object value = attribute.get();
			// Check the value is not null
			if (value != null) {
				// Convert the JNDI value to its Java representation - this will throw if
				// the
				// conversion fails
				Object convertedValue = this.converterManager.convert(value, attributeInfo.getSyntax(),
						attributeInfo.getValueClass());
				// Set it in the Java version
				field.set(result, convertedValue);
			}
		}
	}

	@Override
	public Name getId(Object entry) {
		try {
			return (Name) getIdField(entry).get(entry);
		}
		catch (Exception e) {
			throw new InvalidEntryException(String.format("Can't get Id field from Entry %1$s", entry), e);
		}
	}

	private Field getIdField(Object entry) {
		return getEntityData(entry.getClass()).metaData.getIdAttribute().getField();
	}

	@Override
	public void setId(Object entry, Name id) {
		try {
			getIdField(entry).set(entry, id);
		}
		catch (Exception e) {
			throw new InvalidEntryException(String.format("Can't set Id field on Entry %s to %s", entry, id), e);
		}
	}

	@Override
	public Name getCalculatedId(Object entry) {
		Assert.notNull(entry, "Entry must not be null");
		EntityData entityData = getEntityData(entry.getClass());
		if (entityData.metaData.canCalculateDn()) {
			Set<AttributeMetaData> dnAttributes = entityData.metaData.getDnAttributes();
			LdapNameBuilder ldapNameBuilder = LdapNameBuilder.newInstance(entityData.metaData.getBase());

			for (AttributeMetaData dnAttribute : dnAttributes) {
				Object dnFieldValue = ReflectionUtils.getField(dnAttribute.getField(), entry);
				if (dnFieldValue == null) {
					throw new IllegalStateException(
							String.format("DnAttribute for field %s on class %s is null; cannot build DN",
									dnAttribute.getField().getName(), entry.getClass().getName()));
				}

				ldapNameBuilder.add(dnAttribute.getDnAttribute().value(), dnFieldValue.toString());
			}

			return ldapNameBuilder.build();
		}

		return null;
	}

	@Override
	public Filter filterFor(Class<?> clazz, Filter baseFilter) {
		Filter ocFilter = getEntityData(clazz).ocFilter;

		if (baseFilter == null) {
			return ocFilter;
		}

		AndFilter andFilter = new AndFilter();
		return andFilter.append(ocFilter).append(baseFilter);
	}

	@Override
	public String attributeFor(Class<?> clazz, String fieldName) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			AttributeMetaData attributeMetaData = getEntityData(clazz).metaData.getAttribute(field);
			return attributeMetaData.getName().toString();
		}
		catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(String.format("Field %s cannot be found in class %s", fieldName, clazz),
					e);
		}
	}

	// For testing purposes
	ConcurrentMap<Class<?>, EntityData> getMetaDataMap() {
		return this.metaDataMap;
	}

	static boolean collectionContainsAll(Collection<?> collection, Set<?> shouldBePresent) {
		for (Object o : shouldBePresent) {
			if (!collection.contains(o)) {
				return false;
			}
		}

		return true;
	}

}
