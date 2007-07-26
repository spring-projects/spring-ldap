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
 * The <code>NamingSuffix</code> annotation describes where in an LDAP repository an entry resides.
 * For example an object annotated with:
 * <pre>@NamingSuffix({"ou=people", "dc=example", "dc=com"})</pre>
 * will be persisted under the branch 'com/example/people' in the LDAP repository.
 *
 * <p> The <code>NamingSuffix</code> together with a {@link NamingAttribute} tell the 
 * {@link org.springframework.ldap.odm.mapping.ObjectDirectoryMapper} how to assemble a
 * Distinguished Name for an object.</p>
 *
 * @see NamingAttribute
 */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface NamingSuffix
{
    String[] value();
}
