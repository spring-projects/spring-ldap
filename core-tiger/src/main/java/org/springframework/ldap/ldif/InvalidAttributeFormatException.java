/**
 * 
 */
package org.springframework.ldap.ldif;

import org.springframework.ldap.NamingException;

/**
 * Thrown whenever a parsed attribute does not conform to LDAP specifications.
 * 
 * @author Keith Barlow
 *
 */
public class InvalidAttributeFormatException extends NamingException {

	private static final long serialVersionUID = -4529380160785322985L;

	/**
	 * @param msg
	 */
	public InvalidAttributeFormatException(String msg) {
		super(msg);
	}

	/**
	 * @param cause
	 */
	public InvalidAttributeFormatException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param msg
	 * @param cause
	 */
	public InvalidAttributeFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
