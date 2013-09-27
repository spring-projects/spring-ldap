package org.springframework.ldap.odm.typeconversion.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;

/**
 * A utility class to allow {@link ConverterManagerImpl} instances to be easily configured via <code>spring.xml</code>.
 * <p>
 * The following shows a typical simple example which creates two {@link Converter} instances:
 * <ul>
 * <li><code>fromStringConverter</code></li>
 * <li><code>toStringConverter</code></li>
 * </ul>
 * Configured in an {@link ConverterManagerImpl} to:
 * <ul>
 * <li>Use <code>fromStringConverter</code> to convert from <code>String</code> to <code>Byte, Short, 
 * Integer, Long, Float, Double, Boolean</code> </li>
 * <li>Use <code>toStringConverter</code> to convert from <code>Byte, Short, 
 * Integer, Long, Float, Double, Boolean</code> to <code>String</code></li>
 * </ul>
 * <pre>
 * &lt;bean id="converterManager" class="org.springframework.ldap.odm.typeconversion.impl.ConverterManagerFactoryBean">
 *   &lt;property name="converterConfig">
 *     &lt;set>
 *       &lt;bean class="org.springframework.ldap.odm.typeconversion.impl.ConverterManagerFactoryBean$ConverterConfig">
 *         &lt;property name="fromClasses">
 *           &lt;set> 
 *             &lt;value>java.lang.String&lt;/value>
 *           &lt;/set>
 *         &lt;/property>
 *         &lt;property name="toClasses">
 *           &lt;set>
 *             &lt;value>java.lang.Byte&lt;/value>
 *             &lt;value>java.lang.Short&lt;/value>
 *             &lt;value>java.lang.Integer&lt;/value>
 *             &lt;value>java.lang.Long&lt;/value>
 *             &lt;value>java.lang.Float&lt;/value>
 *             &lt;value>java.lang.Double&lt;/value>
 *             &lt;value>java.lang.Boolean&lt;/value>
 *           &lt;/set>
 *         &lt;/property>
 *         &lt;property name="converter" ref="fromStringConverter" />
 *       &lt;/bean>
 *       &lt;bean class="org.springframework.ldap.odm.typeconversion.impl.ConverterManagerFactoryBean$ConverterConfig">
 *         &lt;property name="fromClasses">
 *           &lt;set>
 *             &lt;value>java.lang.Byte&lt;/value>
 *             &lt;value>java.lang.Short&lt;/value>
 *             &lt;value>java.lang.Integer&lt;/value>
 *             &lt;value>java.lang.Long&lt;/value>
 *             &lt;value>java.lang.Float&lt;/value>
 *             &lt;value>java.lang.Double&lt;/value>
 *             &lt;value>java.lang.Boolean&lt;/value>
 *           &lt;/set> 
 *         &lt;/property>
 *         &lt;property name="toClasses">
 *           &lt;set> 
 *             &lt;value>java.lang.String&lt;/value>
 *           &lt;/set>
 *         &lt;/property>
 *         &lt;property name="converter" ref="toStringConverter" />
 *       &lt;/bean>
 *     &lt;/set>
 *   &lt;/property>
 * &lt;/bean>
 * </pre>
 * {@link ConverterConfig} has a second constructor which takes an additional parameter to allow 
 * an LDAP syntax to be defined.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
public final class ConverterManagerFactoryBean implements FactoryBean {
    private static Log LOG = LogFactory.getLog(ConverterManagerFactoryBean.class);

    /** 
     * Configuration information for a single Converter instance.
     */
    public final static class ConverterConfig {
        // The set of classes the Converter will convert from.
        private Set<Class<?>> fromClasses = new HashSet<Class<?>>();

        // The (optional) LDAP syntax.
        private String syntax=null;

        // The set of classes the Converter will convert to.
        private Set<Class<?>> toClasses = new HashSet<Class<?>>();

        // The Converter to use.
        private Converter converter=null;
        
        public ConverterConfig() {
        }
        
        /**
         * @param fromClasses Comma separated list of classes the {@link Converter} should can convert from.
         */
        public void setFromClasses(Set<Class<?>> fromClasses) {
            this.fromClasses=fromClasses; 
        }
        
        /**
         * @param toClasses Comma separated list of classes the {@link Converter} can convert to.
         */
        public void setToClasses(Set<Class<?>> toClasses) {
            this.toClasses=toClasses;
            
        }
    
        /**
         * @param syntax An LDAP syntax supported by the {@link Converter}.
         */
        public void setSyntax(String syntax) {
            this.syntax=syntax;
        }
        
        /**
         * @param converter The {@link Converter} to use.
         */
        public void setConverter(Converter converter) {
            this.converter=converter;
        }
        
        @Override
        public String toString() {
            return String.format("fromClasses=%1$s, syntax=%2$s, toClasses=%3$s, converter=%4$s",
                                 fromClasses, syntax, toClasses, converter);
        }
    }
    
    private Set<ConverterConfig> converterConfigList=null;
    
    
    /**
     * @param converterConfigList
     */
    public void setConverterConfig(Set<ConverterConfig> converterConfigList) {
        this.converterConfigList=converterConfigList;
    }
    
    /**
     * Creates a ConverterManagerImpl populating it with Converter instances from the converterConfigList property.
     * 
     * @return The newly created {@link org.springframework.ldap.odm.typeconversion.ConverterManager}.
     * @throws ClassNotFoundException Thrown if any of the classes to be converted to or from cannot be found.
     * 
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        if (converterConfigList==null) {
            throw new FactoryBeanNotInitializedException("converterConfigList has not been set");
        }
        
        ConverterManagerImpl result = new ConverterManagerImpl();
        for (ConverterConfig converterConfig : converterConfigList) {
            if (converterConfig.fromClasses==null || 
                converterConfig.toClasses==null ||
                converterConfig.converter==null) {
                
                throw new FactoryBeanNotInitializedException(
                        String.format("All of fromClasses, toClasses and converter must be specified in bean %1$s",
                                      converterConfig.toString()));
            }
            for (Class<?> fromClass : converterConfig.fromClasses) {
                for (Class<?> toClass : converterConfig.toClasses) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Adding converter from %1$s to %2$s", fromClass, toClass));
                    }
                    result.addConverter(fromClass, converterConfig.syntax, toClass, converterConfig.converter);
                }
            }
        }
        return result;    
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class<?> getObjectType() {
        return ConverterManagerImpl.class;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }
}
