package org.springframework.ldap.odm.typeconversion.impl.converters;

import java.lang.reflect.Constructor;

import org.springframework.ldap.odm.typeconversion.impl.Converter;

/**
 * A Converter from a {@link java.lang.String} to any class which has a single argument 
 * public constructor taking a {@link java.lang.String}.
 * <p>
 * This should only be used as a fall-back converter, as a last attempt.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
public final class FromStringConverter implements Converter {

    /* (non-Javadoc)
     * @see org.springframework.ldap.odm.typeconversion.impl.Converter#convert(java.lang.Object, java.lang.Class)
     */
    public <T> T convert(Object source, Class<T> toClass) throws Exception {
        Constructor<T> constructor = toClass.getConstructor(java.lang.String.class);
        return constructor.newInstance(source);
    }
}
