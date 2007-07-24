/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.annotations;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * The <code>ObjectClasses</code> annotation describes which Object Classes an LDAP entry contains
 * attributes for.
 * Example: <pre>@ObjectClasses({"person", "organizationalPerson", "inetorgperson"})</pre> 
 */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface ObjectClasses
{
    String[] value();
}
