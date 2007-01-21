/*
 * Copyright 2006 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */
package org.springframework.ldap.odm.dao.exception;

import java.util.List;

/**
 * A Serializable version of org.springframework.dao.DataIntegrityViolationException
 */
public class DataIntegrityViolationException extends DaoException
{

    private List<String> nonUniqueAttributes;

    public DataIntegrityViolationException(String message)
    {
        super(message);
    }

    public DataIntegrityViolationException(String message, Throwable cause)
    {
        super(message, cause);
    }


   

}
