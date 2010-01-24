package org.springframework.ldap.odm.typeconversion.impl.converters;

import org.springframework.ldap.odm.typeconversion.impl.Converter;


/**
 * A Converter from any class to a {@link java.lang.String} via the <code>toString</code> method.
 * <p>
 * This should only be used as a fall-back converter, as a last attempt.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
public final class ToStringConverter implements Converter {

    /* (non-Javadoc)
     * @see org.springframework.ldap.odm.typeconversion.impl.Converter#convert(java.lang.Object, java.lang.Class)
     */
    public <T> T convert(Object source, Class<T> toClass) {
        return toClass.cast(source.toString());
    }
}
