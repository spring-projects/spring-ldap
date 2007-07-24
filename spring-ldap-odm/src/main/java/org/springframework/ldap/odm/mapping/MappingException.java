/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.mapping;

/** Thrown when an attempt to create an Object Directory Map for a given class is unsuccessful. */
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
