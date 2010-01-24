package org.springframework.ldap.odm.core.impl;

import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.odm.typeconversion.ConverterManager;

/**
 * A Spring Factory bean which creates {@link OdmManagerImpl} instances.
 * <p>
 * Typical configuration would appear as follows:
 * <pre>
 *   &lt;bean id="odmManager" class="org.springframework.ldap.odm.core.impl.OdmManagerImplFactoryBean">
 *       &lt;property name="converterManager" ref="converterManager" />
 *       &lt;property name="contextSource" ref="contextSource" />
 *       &lt;property name="managedClasses">
 *           &lt;set>
 *               &lt;value>org.myorg.myldapentries.Person&lt;/value>
 *               &lt;value>org.myorg.myldapentries.OrganizationalUnit&lt;/value>
 *           &lt;/set>
 *       &lt;/property>
 *   &lt;/bean>
 * </pre>
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
public final class OdmManagerImplFactoryBean implements FactoryBean {
    private ContextSource contextSource=null;
    private Set<Class<?>> managedClasses=null;
    private ConverterManager converterManager=null;
    
    /**
     * Set the ContextSource to use to interact with the LDAP directory.
     * @param contextSource The ContextSource to use.
     */
    public void setContextSource(ContextSource contextSource) {
        this.contextSource=contextSource;
    }
    
    /**
     * Set the list of {@link org.springframework.ldap.odm.annotations} 
     * annotated classes the OdmManager will process.
     * @param managedClasses The list of classes to manage.
     */
    public void setManagedClasses(Set<Class<?>> managedClasses) {
        this.managedClasses=managedClasses;
    }
    
    /**
     * Set the ConverterManager to use to convert between LDAP
     * and Java representations of attributes.
     * @param converterManager The ConverterManager to use.
     */
    public void setConverterManager(ConverterManager converterManager) {
        this.converterManager=converterManager;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        if (contextSource==null) {        
            throw new FactoryBeanNotInitializedException("contextSource property has not been set");
        }
        if (managedClasses==null) {        
            throw new FactoryBeanNotInitializedException("managedClasses property has not been set");
        }
        if (converterManager==null) {        
            throw new FactoryBeanNotInitializedException("converterManager property has not been set");
        }
        
        return new OdmManagerImpl(converterManager, contextSource, managedClasses);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class<?> getObjectType() {
        return OdmManagerImpl.class;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }
}
