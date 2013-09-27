package org.springframework.ldap.odm.typeconversion;

/**
 * A simple interface to be implemented to provide type conversion functionality.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
public interface ConverterManager {
    /**
     * Determine whether this converter manager is able to carry out a specified conversion.
     * 
     * @param fromClass Convert from the <code>fromClass</code>.
     * @param syntax Using the LDAP syntax (may be null).
     * @param toClass To the <code>toClass</code>.
     * @return <code>True</code> if the conversion is supported, <code>false</code> otherwise.
     */
    boolean canConvert(Class<?> fromClass, String syntax, Class<?> toClass);

    /**
     * Convert a given source object with an optional LDAP syntax to an instance of a given class.
     * 
     * @param <T> The class to convert to.
     * @param source The object to convert.
     * @param syntax The LDAP syntax to use (may be null).
     * @param toClass The class to convert to.
     * @return The converted object.
     * 
     * @throws ConverterException If the conversion can not be successfully completed.
     */
    <T> T convert(Object source, String syntax, Class<T> toClass);
}
