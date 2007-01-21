/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.dao.exception;

/**
 * A Serializable version of org.springframework.dao.EntryNotFoundException
 */
public class EntryNotFoundException extends DaoException
{

    public EntryNotFoundException(String message)
    {
        super(message);
    }
   
}
