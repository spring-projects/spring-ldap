/**
 * 
 */
package org.springframework.ldap.ldif;

import org.springframework.ldap.NamingException;

/**
 * Thrown whenever a parsed record does not conform to LDAP specifications.
 * 
 * @author Keith Barlow
 *
 */
public class InvalidRecordFormatException extends NamingException {

	private static final long serialVersionUID = -5047874723621065139L;

	/**
	 * @param msg
	 */
	public InvalidRecordFormatException(String msg) {
		super(msg);
	}

	/**
	 * @param cause
	 */
	public InvalidRecordFormatException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param msg
	 * @param cause
	 */
	public InvalidRecordFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
