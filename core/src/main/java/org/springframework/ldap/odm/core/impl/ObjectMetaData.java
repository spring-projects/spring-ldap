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

package org.springframework.ldap.odm.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.StringUtils;

import javax.naming.Name;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
            if(!a1.isDnAttribute() || !a2.isDnAttribute()) {
                // Not interesting to compare these.
                return 0;
            }

            return Integer.valueOf(a1.getDnAttribute().index()).compareTo(a2.getDnAttribute().index());
        }
    });

    private boolean indexedDnAttributes = false;

    private Set<CaseIgnoreString> objectClasses = new LinkedHashSet<CaseIgnoreString>();

    private Name base = LdapUtils.emptyLdapName();

    public Set<CaseIgnoreString> getObjectClasses() {
        return objectClasses;
    }

    public AttributeMetaData getIdAttribute() {
        return idAttribute;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Field> iterator() {
        return fieldToAttribute.keySet().iterator();
    }

    public AttributeMetaData getAttribute(Field field) {
        return fieldToAttribute.get(field);
    }
    
    public ObjectMetaData(Class<?> clazz) {
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
                for (String localObjectClass:localObjectClasses) {
                    objectClasses.add(new CaseIgnoreString(localObjectClass));
                }
            } else {
                objectClasses.add(new CaseIgnoreString(clazz.getSimpleName()));
            }

            String base = entity.base();
            if(StringUtils.hasText(base)) {
                this.base = LdapUtils.newLdapName(base);
            }
        } else {
            throw new MetaDataException(String.format("Class %1$s must have a class level %2$s annotation", clazz,
                    Entry.class));
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

            AttributeMetaData currentAttributeMetaData=new AttributeMetaData(field);
            if (currentAttributeMetaData.isId()) {
                if (idAttribute!=null) {
                    // There can be only one id field
                    throw new MetaDataException(
                          String.format("You man have only one field with the %1$s annotation in class %2$s", Id.class, clazz));
                }
                idAttribute=currentAttributeMetaData;
            }
            fieldToAttribute.put(field, currentAttributeMetaData);

            if(currentAttributeMetaData.isDnAttribute()) {
                dnAttributes.add(currentAttributeMetaData);
            }
        }

        if (idAttribute == null) {
            throw new MetaDataException(
                    String.format("All Entry classes must define a field with the %1$s annotation, error in class %2$s", Id.class,
                                  clazz));
        }

        postProcessDnAttributes(clazz);

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Extracted metadata from %1$s as %2$s", clazz, this));
        }
    }

    private void postProcessDnAttributes(Class<?> clazz) {
        boolean hasIndexed = false;
        boolean hasNonIndexed = false;

        for (AttributeMetaData dnAttribute : dnAttributes) {
            int declaredIndex = dnAttribute.getDnAttribute().index();

            if(declaredIndex != -1) {
                hasIndexed = true;
            }

            if(declaredIndex == -1) {
                hasNonIndexed = true;
            }
        }

        if(hasIndexed && hasNonIndexed) {
            throw new MetaDataException(String.format("At least one DnAttribute declared on class %s is indexed, " +
                    "which means that all DnAttributes must be indexed", clazz.toString()));
        }

        indexedDnAttributes = hasIndexed;
    }

    int size() {
        return fieldToAttribute.size();
    }

    boolean canCalculateDn() {
        return dnAttributes.size() > 0 && indexedDnAttributes;
    }

    public Set<AttributeMetaData> getDnAttributes() {
        return dnAttributes;
    }

    Name getBase() {
        return base;
    }

    /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
    @Override
    public String toString() {
        return String.format("objectsClasses=%1$s | idField=%2$s | attributes=%3$s", 
                objectClasses, idAttribute.getName(), fieldToAttribute);
    }
}
