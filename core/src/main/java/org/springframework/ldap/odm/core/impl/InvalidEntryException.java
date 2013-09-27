package org.springframework.ldap.odm.core.impl;

import org.springframework.ldap.odm.core.OdmException;

/**
 * Thrown to indicate that an instance is not suitable for persisting in the LDAP directory.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 *
 */
@SuppressWarnings("serial")
public class InvalidEntryException extends OdmException {
    public InvalidEntryException(String message) {
        super(message);
    }

    public InvalidEntryException(String message, Throwable reason) {
        super(message, reason);
    }
}
