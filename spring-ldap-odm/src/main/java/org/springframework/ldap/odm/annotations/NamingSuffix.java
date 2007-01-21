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
/*
 * Copyright 2006 by Sensis. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface NamingSuffix
{
    String[] value();
}
