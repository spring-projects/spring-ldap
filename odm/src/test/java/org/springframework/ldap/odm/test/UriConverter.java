package org.springframework.ldap.odm.test;

import java.net.URI;

import org.springframework.ldap.odm.typeconversion.impl.Converter;

/**
 * A bi-directional converter between {@link java.net.URI} and {@link java.lang.String}.
 * 
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk>
 */
public class UriConverter implements Converter {

	/* (non-Javadoc)
	 * @see org.springframework.ldap.odm.typeconversion.impl.Converter#convert(java.lang.Object, java.lang.Class)
	 */
	public <T> T convert(Object source, Class<T> toClass) throws Exception {
		T result = null;
		if (String.class.isAssignableFrom(source.getClass()) && toClass == URI.class) {
			result = toClass.cast(new URI((String)source));
		} else {
			if (URI.class.isAssignableFrom(source.getClass()) && toClass == String.class) {
				result = toClass.cast(source.toString());
			}
		}

		return result;
	}
}
