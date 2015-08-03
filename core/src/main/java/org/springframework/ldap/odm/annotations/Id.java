package org.springframework.ldap.odm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation marks a Java field as containing the Distinguished Name of an LDAP Entry.
 * <p>
 * The marked field must be of type {@link javax.naming.Name} and must <em>not</em>
 * be annotated {@link Attribute}.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 * 
 * @see Attribute
 * @see javax.naming.Name
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
}
