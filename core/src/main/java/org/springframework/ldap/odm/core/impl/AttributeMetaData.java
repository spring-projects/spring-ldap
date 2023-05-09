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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.Name;

import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

/*
 * Extract attribute meta-data from the @Attribute annotation, the @Id annotation
 * and via reflection.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
/* package */ final class AttributeMetaData {

	private static final CaseIgnoreString OBJECT_CLASS_ATTRIBUTE_CI = new CaseIgnoreString("objectclass");

	// Name of the LDAP attribute from the @Attribute annotation
	private CaseIgnoreString name;

	// Syntax of the LDAP attribute from the @Attribute annotation
	private String syntax;

	// Whether this attribute is binary from the @Attribute annotation
	private boolean isBinary;

	// The Java field corresponding to this meta-data
	private final Field field;

	// The Java class of the field corresponding to this meta data
	// This is the actual scalar type meaning that if the field is
	// List<String> then the valueClass will be String
	private Class<?> valueClass;

	// Is this field annotated @Id
	private boolean isId;

	// Is this field multi-valued represented by a List
	private boolean isCollection;

	private Class<? extends Collection> collectionClass;

	// Is this the objectClass attribute
	private boolean isObjectClass;

	private boolean isTransient = false;

	private boolean isReadOnly = false;

	private String[] attributes;

	private DnAttribute dnAttribute;

	// Extract information from the @Attribute annotation:
	// syntax, isBinary, isObjectClass and name.
	private boolean processAttributeAnnotation(Field field) {
		// Default to no syntax specified
		this.syntax = "";

		// Default to a String based attribute
		this.isBinary = false;

		// Default name of attribute to the name of the field
		this.name = new CaseIgnoreString(field.getName());

		// We have not yet found the @Attribute annotation
		boolean foundAnnotation = false;

		// Grab the @Attribute annotation
		Attribute attribute = field.getAnnotation(Attribute.class);

		List<String> attrList = new ArrayList<String>();
		// Did we find the annotation?
		if (attribute != null) {
			// Pull attribute name, syntax and whether attribute is binary
			// from the annotation
			foundAnnotation = true;
			String localAttributeName = attribute.name();
			// Would be more efficient to use !isEmpty - but that then makes us Java 6
			// dependent
			if (localAttributeName != null && localAttributeName.length() > 0) {
				this.name = new CaseIgnoreString(localAttributeName);
				attrList.add(localAttributeName);
			}
			this.syntax = attribute.syntax();
			this.isBinary = attribute.type() == Attribute.Type.BINARY;
			this.isReadOnly = attribute.readonly();
		}
		this.attributes = attrList.toArray(new String[attrList.size()]);

		this.isObjectClass = this.name.equals(OBJECT_CLASS_ATTRIBUTE_CI);

		return foundAnnotation;
	}

	// Extract reflection information from the field:
	// valueClass, isList
	private void determineFieldType(Field field) {
		// Determine the class of data stored in the field
		Class<?> fieldType = field.getType();

		this.isCollection = Collection.class.isAssignableFrom(fieldType);

		this.valueClass = null;
		if (!this.isCollection) {
			// It's not a list so assume its single valued - so just take the field type
			this.valueClass = fieldType;
		}
		else {
			determineCollectionClass(fieldType);
			// It's multi-valued - so we need to look at the signature in
			// the class file to find the generic type - this is supported for class file
			// format 49 and greater which corresponds to java 5 and later.
			ParameterizedType paramType;
			try {
				paramType = (ParameterizedType) field.getGenericType();
			}
			catch (ClassCastException ex) {
				throw new MetaDataException(
						String.format("Can't determine destination type for field %1$s in Entry class %2$s", field,
								field.getDeclaringClass()),
						ex);
			}
			Type[] actualParamArguments = paramType.getActualTypeArguments();
			if (actualParamArguments.length == 1) {
				if (actualParamArguments[0] instanceof Class) {
					this.valueClass = (Class<?>) actualParamArguments[0];
				}
				else {
					if (actualParamArguments[0] instanceof GenericArrayType) {
						// Deal with arrays
						Type type = ((GenericArrayType) actualParamArguments[0]).getGenericComponentType();
						if (type instanceof Class) {
							this.valueClass = Array.newInstance((Class<?>) type, 0).getClass();
						}
					}
				}
			}
		}

		// Check we have been able to determine the value class
		if (this.valueClass == null) {
			throw new MetaDataException(String.format("Can't determine destination type for field %1$s in class %2$s",
					field, field.getDeclaringClass()));
		}
	}

	@SuppressWarnings("unchecked")
	private void determineCollectionClass(Class<?> fieldType) {
		if (fieldType.isInterface()) {
			if (Collection.class.equals(fieldType) || List.class.equals(fieldType)) {
				this.collectionClass = ArrayList.class;
			}
			else if (SortedSet.class.equals(fieldType)) {
				this.collectionClass = TreeSet.class;
			}
			else if (Set.class.isAssignableFrom(fieldType)) {
				this.collectionClass = LinkedHashSet.class;
			}
			else {
				throw new MetaDataException(String.format("Collection class %s is not supported", fieldType));
			}
		}
		else {
			this.collectionClass = (Class<? extends Collection>) fieldType;
		}
	}

	@SuppressWarnings("unchecked")
	Collection<Object> newCollectionInstance() {
		try {
			return (Collection<Object>) this.collectionClass.newInstance();
		}
		catch (Exception ex) {
			throw new UncategorizedLdapException("Failed to instantiate collection class", ex);
		}
	}

	// Extract information from the @Id annotation:
	// isId
	private boolean processIdAnnotation(Field field, Class<?> fieldType) {
		// Are we dealing with the Id field?
		this.isId = field.getAnnotation(Id.class) != null;

		if (this.isId) {
			// It must be of type Name or a subclass of that of
			if (!Name.class.isAssignableFrom(fieldType)) {
				throw new MetaDataException(String.format(
						"The id field must be of type javax.naming.Name or a subclass that of in Entry class %1$s",
						field.getDeclaringClass()));
			}
		}

		return this.isId;
	}

	// Extract meta-data from the given field
	AttributeMetaData(Field field) {
		this.field = field;

		this.dnAttribute = field.getAnnotation(DnAttribute.class);
		if (this.dnAttribute != null && !field.getType().equals(String.class)) {
			throw new MetaDataException(
					String.format("%s is of type %s, but only String attributes can be declared as @DnAttributes",
							field.toString(), field.getType().toString()));
		}

		Transient transientAnnotation = field.getAnnotation(Transient.class);
		if (transientAnnotation != null) {
			this.isTransient = true;
			return;
		}

		// Reflection data
		determineFieldType(field);

		// Data from the @Attribute annotation
		boolean foundAttributeAnnotation = processAttributeAnnotation(field);

		// Data from the @Id annotation
		boolean foundIdAnnoation = processIdAnnotation(field, this.valueClass);

		// Check that the field has not been annotated with both @Attribute and with @Id
		if (foundAttributeAnnotation && foundIdAnnoation) {
			throw new MetaDataException(String.format(
					"You may not specifiy an %1$s annoation and an %2$s annotation on the same field, error in field %3$s in Entry class %4$s",
					Id.class, Attribute.class, field.getName(), field.getDeclaringClass()));
		}

		// If this is the objectclass attribute then it must be of type List<String>
		if (isObjectClass() && (!isCollection() || this.valueClass != String.class)) {
			throw new MetaDataException(
					String.format("The type of the objectclass attribute must be List<String> in classs %1$s",
							field.getDeclaringClass()));
		}
	}

	String getSyntax() {
		return this.syntax;
	}

	boolean isBinary() {
		return this.isBinary;
	}

	Field getField() {
		return this.field;
	}

	CaseIgnoreString getName() {
		return this.name;
	}

	boolean isCollection() {
		return this.isCollection;
	}

	boolean isId() {
		return this.isId;
	}

	boolean isReadOnly() {
		return this.isReadOnly;
	}

	boolean isTransient() {
		return this.isTransient;
	}

	DnAttribute getDnAttribute() {
		return this.dnAttribute;
	}

	boolean isDnAttribute() {
		return this.dnAttribute != null;
	}

	boolean isObjectClass() {
		return this.isObjectClass;
	}

	Class<?> getValueClass() {
		return this.valueClass;
	}

	String[] getAttributes() {
		return this.attributes;
	}

	Class<?> getJndiClass() {
		if (isBinary()) {
			return byte[].class;
		}
		else if (Name.class.isAssignableFrom(this.valueClass)) {
			return Name.class;
		}
		else {
			return String.class;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"name=%1$s | field=%2$s | valueClass=%3$s | syntax=%4$s| isBinary=%5$s | isId=%6$s | isReadOnly=%7$s |  isList=%8$s | isObjectClass=%9$s",
				getName(), getField(), getValueClass(), getSyntax(), isBinary(), isId(), isReadOnly(), isCollection(),
				isObjectClass());
	}

}
