/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.typeconversion;

public enum SavePolicy
{
    SUPPRESS_REFERENTIAL_INTEGRITY_EXCEPTIONS,
    THROW_REFERENTIAL_INTEGRITY_EXCEPTIONS,
    CREATE,
    CREATE_OR_UPDATE
}
