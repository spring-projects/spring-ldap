package org.springframework.ldap.odm.core.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

/*
 * An internal class to process the meta-data and reflection data for an entry.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
/* package */ final class ObjectMetaData implements Iterable<Field> {
    private static final Log LOG = LogFactory.getLog(ObjectMetaData.class);

    private AttributeMetaData idAttribute;

    private Map<Field, AttributeMetaData> fieldToAttribute = new HashMap<Field, AttributeMetaData>();

    private Set<CaseIgnoreString> objectClasses = new HashSet<CaseIgnoreString>();

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
        Entry entity = (Entry)clazz.getAnnotation(Entry.class);
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

            // Skip transient and synthetic fields
            if (field.getAnnotation(Transient.class) != null || field.isSynthetic()) {
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
        }

        if (idAttribute == null) {
            throw new MetaDataException(
                    String.format("All Entry classes must define a field with the %1$s annotation, error in class %2$s", Id.class,
                                  clazz));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Extracted metadata from %1$s as %2$s", clazz, this));
        }
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
