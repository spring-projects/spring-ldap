package org.springframework.ldap.odm.core.impl;

import org.springframework.ldap.odm.core.OdmException;

/**
 * Thrown when an OdmManager method is called with a class
 * which is not being managed by the OdmManager.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 *
 */
@SuppressWarnings("serial")
public class UnmanagedClassException extends OdmException {
    public UnmanagedClassException(String message, Throwable reason) {
        super(message, reason);
    }
    
    public UnmanagedClassException(String message) {
        super(message);
    }
}
