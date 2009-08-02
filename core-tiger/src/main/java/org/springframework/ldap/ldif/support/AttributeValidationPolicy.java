package org.springframework.ldap.ldif.support;

import javax.naming.directory.Attribute;

/**
 * Interface defining the required methods for AttributeValidationPolicies.
 *
 * @author Keith Barlow
 */
public interface AttributeValidationPolicy {

	/**
	 * Validates attribute contained in the buffer and returns an LdapAttribute.
	 * 
	 * @param buffer
	 * @return LdapAttribute representing the attribute parsed.
	 */
	Attribute parse(String buffer);
	
}
