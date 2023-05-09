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
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.Name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.StringUtils;

/*
 * An internal class to process the meta-data and reflection data for an entry.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
/* package */ final class ObjectMetaData implements Iterable<Field> {

	private static final Logger LOG = LoggerFactory.getLogger(ObjectMetaData.class);

	private AttributeMetaData idAttribute;

	private Map<Field, AttributeMetaData> fieldToAttribute = new HashMap<Field, AttributeMetaData>();

	private Set<AttributeMetaData> dnAttributes = new TreeSet<AttributeMetaData>(new Comparator<AttributeMetaData>() {
		@Override
		public int compare(AttributeMetaData a1, AttributeMetaData a2) {
			if (!a1.isDnAttribute() || !a2.isDnAttribute()) {
				// Not interesting to compare these.
				return 0;
			}

			return Integer.valueOf(a1.getDnAttribute().index()).compareTo(a2.getDnAttribute().index());
		}
	});

	private boolean indexedDnAttributes = false;

	private Set<CaseIgnoreString> objectClasses = new LinkedHashSet<CaseIgnoreString>();

	private Name base = LdapUtils.emptyLdapName();

	Set<CaseIgnoreString> getObjectClasses() {
		return this.objectClasses;
	}

	AttributeMetaData getIdAttribute() {
		return this.idAttribute;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Field> iterator() {
		return this.fieldToAttribute.keySet().iterator();
	}

	AttributeMetaData getAttribute(Field field) {
		return this.fieldToAttribute.get(field);
	}

	ObjectMetaData(Class<?> clazz) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Extracting metadata from %1$s", clazz));
		}

		// Get object class metadata - the @Entity annotation
		Entry entity = clazz.getAnnotation(Entry.class);
		if (entity != null) {
			// Default objectclass name to the class name unless it's specified
			// in @Entity(name={objectclass1, objectclass2});
			String[] localObjectClasses = entity.objectClasses();
			if (localObjectClasses != null && localObjectClasses.length > 0 && localObjectClasses[0].length() > 0) {
				for (String localObjectClass : localObjectClasses) {
					this.objectClasses.add(new CaseIgnoreString(localObjectClass));
				}
			}
			else {
				this.objectClasses.add(new CaseIgnoreString(clazz.getSimpleName()));
			}

			String base = entity.base();
			if (StringUtils.hasText(base)) {
				this.base = LdapUtils.newLdapName(base);
			}
		}
		else {
			throw new MetaDataException(
					String.format("Class %1$s must have a class level %2$s annotation", clazz, Entry.class));
		}

		// Check the class is final
		if (!Modifier.isFinal(clazz.getModifiers())) {
			LOG.warn(String.format("The Entry class %1$s should be declared final", clazz.getSimpleName()));
		}

		// Get field meta-data - the @Attribute annotation
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			// So we can write to private fields
			field.setAccessible(true);

			// Skip synthetic or static fields
			if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
				continue;
			}

			AttributeMetaData currentAttributeMetaData = new AttributeMetaData(field);
			if (currentAttributeMetaData.isId()) {
				if (this.idAttribute != null) {
					// There can be only one id field
					throw new MetaDataException(String.format(
							"You man have only one field with the %1$s annotation in class %2$s", Id.class, clazz));
				}
				this.idAttribute = currentAttributeMetaData;
			}
			this.fieldToAttribute.put(field, currentAttributeMetaData);

			if (currentAttributeMetaData.isDnAttribute()) {
				this.dnAttributes.add(currentAttributeMetaData);
			}
		}

		if (this.idAttribute == null) {
			throw new MetaDataException(
					String.format("All Entry classes must define a field with the %1$s annotation, error in class %2$s",
							Id.class, clazz));
		}

		postProcessDnAttributes(clazz);

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Extracted metadata from %1$s as %2$s", clazz, this));
		}
	}

	private void postProcessDnAttributes(Class<?> clazz) {
		boolean hasIndexed = false;
		boolean hasNonIndexed = false;

		for (AttributeMetaData dnAttribute : this.dnAttributes) {
			int declaredIndex = dnAttribute.getDnAttribute().index();

			if (declaredIndex != -1) {
				hasIndexed = true;
			}

			if (declaredIndex == -1) {
				hasNonIndexed = true;
			}
		}

		if (hasIndexed && hasNonIndexed) {
			throw new MetaDataException(String.format("At least one DnAttribute declared on class %s is indexed, "
					+ "which means that all DnAttributes must be indexed", clazz.toString()));
		}

		this.indexedDnAttributes = hasIndexed;
	}

	int size() {
		return this.fieldToAttribute.size();
	}

	boolean canCalculateDn() {
		return this.dnAttributes.size() > 0 && this.indexedDnAttributes;
	}

	Set<AttributeMetaData> getDnAttributes() {
		return this.dnAttributes;
	}

	Name getBase() {
		return this.base;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("objectsClasses=%1$s | idField=%2$s | attributes=%3$s", this.objectClasses,
				this.idAttribute.getName(), this.fieldToAttribute);
	}

}
