package org.springframework.ldap.odm.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.odm.core.OdmException;
import org.springframework.ldap.odm.core.OdmManager;
import org.springframework.ldap.odm.typeconversion.ConverterManager;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link org.springframework.ldap.odm.core.OdmManager} which
 * uses {@link org.springframework.ldap.odm.typeconversion.ConverterManager} to 
 * convert between Java and LDAP representations of attribute values.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 *
 */
public final class OdmManagerImpl implements OdmManager {
    private static final Log LOG = LogFactory.getLog(OdmManagerImpl.class);
   
    // The link to the LDAP directory
    private final LdapOperations ldapTemplate;

    // The converter manager to use to translate values between LDAP and Java
    private final ConverterManager converterManager;
    
    private static String OBJECT_CLASS_ATTRIBUTE="objectclass";
    private static CaseIgnoreString OBJECT_CLASS_ATTRIBUTE_CI=new CaseIgnoreString(OBJECT_CLASS_ATTRIBUTE);
    
    private static final class EntityData {
        private final ObjectMetaData metaData;
        private final String ocFilter;
        
        private EntityData(ObjectMetaData metaData, String ocFilter) {
            this.metaData=metaData;
            this.ocFilter=ocFilter;
        }
    }
    
    // A map of managed classes to to meta data about those classes
    private final Map<Class<?>, EntityData> metaDataMap=new HashMap<Class<?>, EntityData>();

    public OdmManagerImpl(ConverterManager converterManager,
                          LdapOperations ldapOperations,
                          Set<Class<?>> managedClasses) {
        this.converterManager=converterManager;
        this.ldapTemplate = ldapOperations;
        if (managedClasses!=null) {
            for (Class<?> managedClass: managedClasses) {
                addManagedClass(managedClass);
            }
        }
    }

    public OdmManagerImpl(ConverterManager converterManager, 
                          ContextSource contextSource,
                          Set<Class<?>> managedClasses) {
        this(converterManager, new LdapTemplate(contextSource), managedClasses);
    }
    
    public OdmManagerImpl(ConverterManager converterManager, 
                          ContextSource contextSource) {
        this(converterManager, contextSource, null);
    }

    private EntityData getEntityData(Class<?> managedClass) {
        EntityData result=metaDataMap.get(managedClass);
        if (result==null) {
            throw new UnmanagedClassException(String.format("The %1$s class is not managed by this OdmManager", managedClass));
        }
        return result;
    }

    /**
     * Adds an {@link org.springframework.ldap.odm.annotations} annotated class to the set
     * managed by this OdmManager.
     * 
     * @param managedClass The class to add to the managed set.
     */
    public void addManagedClass(Class<?> managedClass) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Adding class %1$s to managed set", managedClass));
        }
        
        // Extract the meta-data from the class
        ObjectMetaData metaData=new ObjectMetaData(managedClass);
        
        // Check we can construct the target type - it must have a zero argument public constructor
        try {
            managedClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new InvalidEntryException(String.format(
                    "The class %1$s must have a zero argument constructor to be an Entry", managedClass));
        }
        
        // Check we have all of the necessary converters for the class
        for (Field field : metaData) {
            AttributeMetaData attributeInfo = metaData.getAttribute(field);
            if (!attributeInfo.isId() && !(attributeInfo.isObjectClass())) {
                Class<?> jndiClass = (attributeInfo.isBinary()) ? byte[].class : String.class;
                Class<?> javaClass = attributeInfo.getValueClass();
                if (!converterManager.canConvert(jndiClass, attributeInfo.getSyntax(), javaClass)) {
                    throw new InvalidEntryException(String.format(
                            "Missing converter from %1$s to %2$s, this is needed for field %3$s on Entry %4$s",
                            jndiClass, javaClass, field.getName(), managedClass));
                }
                if (!converterManager.canConvert(javaClass, attributeInfo.getSyntax(), jndiClass)) {
                    throw new InvalidEntryException(String.format(
                            "Missing converter from %1$s to %2$s, this is needed for field %3$s on Entry %4$s",
                            javaClass, jndiClass, field.getName(), managedClass));
                }
            }
        }
        
        // Filter so we only read the object classes supported by the managedClass
        AndFilter ocFilter = new AndFilter();
        for (CaseIgnoreString oc : metaData.getObjectClasses()) {
            ocFilter.and(new EqualsFilter(OBJECT_CLASS_ATTRIBUTE, oc.toString()));
        }

        metaDataMap.put(managedClass, new EntityData(metaData, ocFilter.encode())); 
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.odm.core.OdmManager#create(java.lang.Object)
     */
    public <T> T read(Class<T> clazz, Name dn) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Reading Entry at - %s$1", dn));
        }
        
        getEntityData(clazz);
        
        T result = clazz.cast(ldapTemplate.lookup(dn, new GenericContextMapper<T>(clazz)));
        if (result==null) {
            throw new OdmException(String.format("Entry %1$s does not have the required objectclasses ", dn));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found entry - %s$1", result));
        }
        
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.odm.core.OdmManager#create(java.lang.Object)
     */
    public void create(Object entry) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating entry - %s$1", entry));
        }
        
        DirContextAdapter context = new DirContextAdapter(getId(entry));
        mapToContext(entry, context);
        ldapTemplate.bind(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.odm.core.OdmManager#update(java.lang.Object, boolean)
     */
    public void update(Object entry) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating entry - %s$1", entry));
        }
        
        DirContextOperations context = ldapTemplate.lookupContext(getId(entry));
        mapToContext(entry, context);
        ldapTemplate.modifyAttributes(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.odm.core.OdmManager#delete(javax.naming.Name)
     */
    public void delete(Object entry) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deleting %s$1", entry));
        }
        
        // Just to check that this is a managed class
        getEntityData(entry.getClass());
        ldapTemplate.unbind(getId(entry));
    }

    private Name getId(Object entry) {
        try {
            return (Name)getEntityData(entry.getClass()).metaData.getIdAttribute().getField().get(entry);
        } catch (Exception e) {
            throw new InvalidEntryException(String.format("Can't get Id field from Entry %1$s", entry), 
                    e);
        }
    }
   
    /* (non-Javadoc)
     * @see org.springframework.ldap.odm.core.OdmManager#search(java.lang.Class, javax.naming.Name, java.lang.String, javax.naming.directory.SearchControls)
     */
    public <T> List<T> search(Class<T> managedClass, Name base, String filter, SearchControls scope) {
        EntityData entityData=getEntityData(managedClass);
        
        // Add a filter so we only read the object class we can deal with
        String finalFilter = entityData.ocFilter;
        if (filter != null && filter.length() != 0) {
            StringBuilder fixedFilter = new StringBuilder();
            fixedFilter.append("(&(").append(filter).append(")").append(entityData.ocFilter).append(")");
            finalFilter = fixedFilter.toString();
        }

        // Search from the root if we are not told where to search from
        Name localBase = base;
        if (base == null || base.size() == 0) {
            localBase = DistinguishedName.EMPTY_PATH;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Searching - base=%1$s, finalFilter=%2$s, scope=%3$s", base, finalFilter, scope));
        }

        @SuppressWarnings("unchecked")
        List<T> result = ldapTemplate.search(localBase, finalFilter, scope, new GenericContextMapper<T>(managedClass));
        result.remove(null);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found %1$s Entries - %2$s", result.size(), result));
        }
        
        return result;
    }

   
    /* 
     * (non-Javadoc)
     * @see org.springframework.ldap.odm.core.OdmManager#findAll(javax.naming.Name, javax.naming.directory.SearchControls)
     */
    public <T> List<T> findAll(Class<T> managedClass, Name base, SearchControls scope) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Searching for all Entries with objectClass=%1$s, with base=%2$s, scope=%3$s", 
                    getEntityData(managedClass).metaData.getObjectClasses(), base, scope));
        }
        
        return search(managedClass, base, null, scope);
    }

    /**
     * Used to convert from Java representation of an Ldap Entry when writing to 
     * the Ldap directory
     * 
     * @param entry - The entry to convert.
     * @param context - The LDAP context to store the converted entry
     * @throws javax.naming.NamingEnumeration on error.
     */
    private void mapToContext(Object entry, DirContextOperations context) {
        ObjectMetaData metaData=getEntityData(entry.getClass()).metaData;

        Attribute objectclassAttribute = context.getAttributes().get(OBJECT_CLASS_ATTRIBUTE);
        if(objectclassAttribute == null || objectclassAttribute.size() == 0) {
            // Object classes are set from the metadata obtained from the @Entity annotation,
            // but only if this is a new entry.
            int numOcs=metaData.getObjectClasses().size();
            CaseIgnoreString[] metaDataObjectClasses=metaData.getObjectClasses().toArray(new CaseIgnoreString[numOcs]);

            String[] stringOcs=new String[numOcs];
            for (int ocIndex=0; ocIndex<numOcs; ocIndex++) {
                stringOcs[ocIndex]=metaDataObjectClasses[ocIndex].toString();
            }

            context.setAttributeValues(OBJECT_CLASS_ATTRIBUTE, stringOcs);
        }

        // Loop through each of the fields in the object to write to LDAP
        for (Field field : metaData) {
            // Grab the meta data for the current field
            AttributeMetaData attributeInfo = metaData.getAttribute(field);
            // We dealt with the object class field about, and the DN is set by the call to write the object to LDAP
            if (!attributeInfo.isId() && !(attributeInfo.isObjectClass())) {
                try {
                    // If this is a "binary" object the JNDI expects a byte[] otherwise a String
                    Class<?> targetClass = (attributeInfo.isBinary()) ? byte[].class : String.class;
                    // Multi valued?
                    if (!attributeInfo.isList()) {
                        // Single valued - get the value of the field
                        Object fieldValue = field.get(entry);
                        // Ignore null field values
                        if (fieldValue != null) {
                            // Convert the field value to the required type and write it into the JNDI context
                            context.setAttributeValue(attributeInfo.getName().toString(), converterManager.convert(fieldValue,
                                    attributeInfo.getSyntax(), targetClass));
                        }
                    } else { // Multi-valued
                        // We need to build up a list of of the values
                        List<String> attributeValues = new ArrayList<String>();
                        // Get the list of values
                        Collection<?> fieldValues = (Collection<?>)field.get(entry);
                        // Ignore null lists
                        if (fieldValues != null) {
                            for (final Object o : fieldValues) {
                                // Ignore null values
                                if (o != null) {
                                    attributeValues.add((String)converterManager.convert(o, attributeInfo.getSyntax(),
                                            targetClass));
                                }
                            }
                            context.setAttributeValues(attributeInfo.getName().toString(), attributeValues.toArray());
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new InvalidEntryException(String.format("Can't set attribute %1$s", attributeInfo.getName()),
                            e);
                }
            }
        }
    }

    /**
     * Used to convert from the JNDI LDAP representation of an Entry to the Java representation when reading from LDAP
     */
    private class GenericContextMapper<T> implements ParameterizedContextMapper<T> {
        private final Class<T> managedClass;
        
        private GenericContextMapper(Class<T> managedClass) {
            this.managedClass=managedClass;
        }
        
        // Called by Spring LDAP to do the conversion
        /*
         * (non-Javadoc)
         * 
         * @see org.springframework.ldap.core.simple.ParameterizedContextMapper#mapFromContext(java.lang.Object)
         */
        public T mapFromContext(Object object) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Converting to Java Entry class %1$s from %2$s", managedClass, object));
            }
            
            // The Java representation of the LDAP entry
            T result = null;

            // This is guaranteed by Spring LDAP to be a DirContextOperations
            DirContextOperations context = (DirContextOperations)object;

            ObjectMetaData metaData=getEntityData(managedClass).metaData;
            
            try {
                // The result class must have a zero argument constructor
                result = managedClass.newInstance();

                // Build a map of JNDI attribute names to values
                Map<CaseIgnoreString, Attribute> attributeValueMap = new HashMap<CaseIgnoreString, Attribute>();
                // Get a NamingEnumeration to loop through the JNDI attributes in the entry
                Attributes attributes = context.getAttributes();
                NamingEnumeration<? extends Attribute> attributesEnumeration = attributes.getAll();
                // Loop through all of the JNDI attributes
                while (attributesEnumeration.hasMoreElements()) {
                    Attribute currentAttribute = (Attribute)attributesEnumeration.nextElement();
                    // Add the current attribute to the map keyed on the lowercased (case indep) id of the attribute
                    attributeValueMap.put(new CaseIgnoreString(currentAttribute.getID()), currentAttribute);
                }

                // Now loop through all the fields in the Java representation populating it with values from the
                // attributeValueMap
                for (Field field : metaData) {
                    // Get the current field
                    AttributeMetaData attributeInfo = metaData.getAttribute(field);
                    // We deal with the Id field specially
                    if (!attributeInfo.isId()) {
                        // Not the ID - but is is multi valued?
                        if (!attributeInfo.isList()) {
                            // No - its single valued, grab the JNDI attribute that corresponds to the metadata on the
                            // current field
                            Attribute attribute = attributeValueMap.get(attributeInfo.getName());
                            // There is no guarantee that this attribute is present in the directory - so ignore nulls
                            if (attribute != null) {
                                // Grab the JNDI value
                                Object value = attribute.get();
                                // Check the value is not null
                                if (value != null) {
                                    // Convert the JNDI value to its Java representation - this will throw if the
                                    // conversion fails
                                    Object convertedValue = converterManager.convert(value, attributeInfo.getSyntax(),
                                            attributeInfo.getValueClass());
                                    // Set it in the Java version
                                    field.set(result, convertedValue);
                                }
                            }
                        } else { // We are dealing with a multi valued attribute
                            // We need to build up a list of values
                            List<Object> fieldValues = new ArrayList<Object>();
                            // Grab the attribute from the JNDI representation
                            Attribute currentAttribute = attributeValueMap.get(attributeInfo.getName());
                            // There is no guarantee that this attribute is present in the directory - so ignore nulls
                            if (currentAttribute != null) {
                                // Loop through the values of the JNDI attribute
                                NamingEnumeration<?> valuesEmumeration = currentAttribute.getAll();
                                while (valuesEmumeration.hasMore()) {
                                    // Get the current value
                                    Object value = valuesEmumeration.nextElement();
                                    // Check the value is not null
                                    if (value != null) {
                                        // Convert the value to its Java representation and add it to our working list
                                        fieldValues.add(converterManager.convert(value, attributeInfo.getSyntax(),
                                                attributeInfo.getValueClass()));
                                    }
                                }
                            }
                            // Now we need to set the List in to a Java object
                            field.set(result, fieldValues);
                        }
                    } else { // The id field
                        field.set(result, converterManager.convert(context.getDn(), attributeInfo.getSyntax(),
                                attributeInfo.getValueClass()));
                    }
                }

                // If this is the objectclass attribute then check that values correspond to the metadata we have
                // for the Java representation
                Attribute ocAttribute = attributeValueMap.get(OBJECT_CLASS_ATTRIBUTE_CI);
                if (ocAttribute != null) {
                    // Get all object class values from the JNDI attribute
                    Set<CaseIgnoreString> objectClassesFromJndi = new HashSet<CaseIgnoreString>();
                    NamingEnumeration<?> objectClassesFromJndiEnum = ocAttribute.getAll();
                    while (objectClassesFromJndiEnum.hasMoreElements()) {
                        objectClassesFromJndi.add(new CaseIgnoreString((String)objectClassesFromJndiEnum.nextElement()));
                    }
                    // OK - checks its the same as the meta-data we have
                    if(!collectionContainsAll(objectClassesFromJndi, metaData.getObjectClasses())) {
                        return null;
                    }
                } else {
                    throw new InvalidEntryException(String.format("No object classes were returned for class %1$s",
                            managedClass.getName()));
                }

            } catch (NamingException ne) {
                throw new InvalidEntryException(String.format("Problem creating %1$s from LDAP Entry %2$s",
                        managedClass, object), ne);
            } catch (IllegalAccessException iae) {
                throw new InvalidEntryException(String.format(
                        "Could not create an instance of %1$s could not access field", managedClass.getName()), iae);
            } catch (InstantiationException ie) {
                throw new InvalidEntryException(String.format("Could not instantiate %1$s", managedClass), ie);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Converted object - %1$s", result));
            }
            
            return result;
        }
    }

    static boolean collectionContainsAll(Collection<?> collection, Set<?> shouldBePresent) {
        for (Object o : shouldBePresent) {
            if(!collection.contains(o)) {
                return false;
            }
        }

        return true;
    }

}
