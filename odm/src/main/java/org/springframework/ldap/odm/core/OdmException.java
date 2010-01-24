package org.springframework.ldap.odm.core;

import org.springframework.ldap.NamingException;

/**
 * The root of the Spring LDAP ODM exception hierarchy.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 *
 */
@SuppressWarnings("serial")
public class OdmException extends NamingException {
    public OdmException(String message) {
        super(message);
    }

    public OdmException(String message, Throwable e) {
        super(message, e);
    }
}
