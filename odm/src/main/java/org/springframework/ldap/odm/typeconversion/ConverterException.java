package org.springframework.ldap.odm.typeconversion;

import org.springframework.ldap.NamingException;

/**
 * Thrown by the conversion framework to indicate an error condition - typically a failed type conversion.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
@SuppressWarnings("serial")
public final class ConverterException extends NamingException {
    public ConverterException(final String message) {
        super(message);
    }

    public ConverterException(final String message, final Throwable e) {
        super(message, e);
    }
}
