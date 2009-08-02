package org.springframework.ldap.schema;

import javax.naming.NamingException;

/**
 * The specification interface is implemented to declare rules that
 * a record must conform to.  The motivation behind this class was 
 * to provide a mechanism to enable schema validations.
 * 
 * @author Keith Barlow
 *
 * @param <T>
 */
public interface Specification<T> {

	boolean isSatisfiedBy(T record) throws NamingException;
	
}
