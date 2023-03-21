package org.springframework.ldap.core;

/**
 * Callback interface to be used in the authentication methods in {@link LdapOperations}
 * for performing operations when there are authentication errors. Can be useful when the
 * cause of the authentication failure needs to be retrieved.
 *
 * @author Ulrik Sandberg
 * @since 1.3.1
 */
public interface AuthenticationErrorCallback {

	/**
	 * This method will be called with the authentication exception in case there is a
	 * problem with the authentication.
	 * @param e the exception that was caught in the authentication method
	 */
	void execute(Exception e);

}
