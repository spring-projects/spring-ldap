package org.springframework.ldap.core.simple;

import javax.naming.Binding;
import javax.naming.directory.SearchResult;

import org.springframework.ldap.core.ContextMapper;

/**
 * Extension of the {@link ContextMapper} interface. Uses Java 5 covariant
 * return types to override the return type of the
 * {@link #mapFromContext(Object)} method to be the type parameter T.
 * 
 * @param <T>
 */
public interface ParameterizedContextMapper<T> extends ContextMapper {

	/**
	 * Map a single LDAP Context to an object. The supplied Object
	 * <code>ctx</code> is the object from a single {@link SearchResult},
	 * {@link Binding}, or a lookup operation.
	 * 
	 * @param ctx the context to map to an object.
	 * @return an object built from the data in the context.
	 */
	public T mapFromContext(Object ctx);
}
