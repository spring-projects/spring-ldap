package org.springframework.ldap.odm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation identifies a field in an {@link Entry} annotated class that 
 * should <em>not</em> be persisted to LDAP.
 * 
 * @author Paul Harvey <paul@pauls-place.me.uk>
 * 
 * @see Entry
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}
