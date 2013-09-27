package org.springframework.ldap.odm.core.impl;

import org.springframework.ldap.odm.core.OdmException;

/**
 * Thrown to indicate an error in the annotated meta-data.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 *
 */
@SuppressWarnings("serial")
public class MetaDataException extends OdmException {
    public MetaDataException(String message) {
        super(message);
    }

    public MetaDataException(String message, Throwable reason) {
        super(message, reason);
    }
}
