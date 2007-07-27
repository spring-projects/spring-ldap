/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.annotations;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * The <code>DirAttribute</code> describes the mapping between a bean property and an LDAP attribute.
 * For example:
 * <pre>@DirAttribute("uid")
 * private String uniqueIdentifier;</pre>
 * maps the field 'uniqueIdentifier' to the directory attribute 'uid'. A <code>DirAttribute</code>
 * without a value designates that the attribute has the same name as the bean property.  
 */
@Documented
@Target({FIELD})
@Retention(RUNTIME)
public @interface DirAttribute
{
    String value() default "";
}

