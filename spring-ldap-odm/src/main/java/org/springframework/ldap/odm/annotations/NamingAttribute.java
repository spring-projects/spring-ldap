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
 * The <code>NamingAttribute</code> annotation identifies the name of the attribute that forms
 * the first part of a Distinguished Name. For example, in the Distinguished Name
 * 'uid=x232, ou=people' the naming attribute is 'uid'. The <code>NamingAttribute</code> together
 * with a <code>NamingSuffix</code> tell the Object Directory Mapper how to serialize a java object
 * to and from an LDAP repository.
 * Example: <pre>@NamingAttribute("uid")</pre>
 *
 * @see NamingSuffix
 */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface NamingAttribute
{
    String value();
}
