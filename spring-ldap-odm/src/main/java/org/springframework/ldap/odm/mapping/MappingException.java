/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.mapping;

/** Thrown when an attempt to create an {@link ObjectDirectoryMap} for a given class is unsuccessful.
 * May indicate semantic or syntax errors in the mapping information, or some other exceptional
 * condition related to the creation of an {@link ObjectDirectoryMap}.
 */
public class MappingException extends Exception
{

    public MappingException(String message)
    {
        super(message);
    }

    public MappingException(String message, Throwable cause)
    {
        super(message, cause);
    }


}
