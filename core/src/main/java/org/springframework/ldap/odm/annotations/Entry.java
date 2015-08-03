package org.springframework.ldap.odm.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a Java class to be persisted in an LDAP directory.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Entry {
    /**
     * A list of LDAP object classes that the annotated Java class represents.
     * <p>
     * All fields will be persisted to LDAP unless annotated {@link Transient}.
     * 
     * @return A list of LDAP classes which the annotated Java class represents.
     */
    String[] objectClasses();

    /**
     * The base DN of this entry. If specified, this will be prepended to all calculated
     * distinguished names for entries of the annotated class.
     *
     * @return the base DN for entries of this class
     */
    String base() default "";
}
