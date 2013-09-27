package org.springframework.ldap.odm.typeconversion.impl;

/**
 * Interface specifying the conversion between two classes
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
public interface Converter {
    /**
     * Attempt to convert a given object to a named class.
     * 
     * @param <T> The class to convert to.
     * @param source The object to convert.
     * @param toClass The class to convert to.
     * @return The converted class or null if the conversion was not possible.
     * @throws Exception Any exception may be throw by a Converter on error.
     */
    <T> T convert(Object source, Class<T> toClass) throws Exception;
}
