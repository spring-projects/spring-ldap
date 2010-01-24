/**
 * 
 */
package org.springframework.ldap.ldif.batch;

import org.springframework.ldap.core.LdapAttributes;

/**
 * This default implementation simply returns the LdapAttributes object and is only intended for test.  As its not required
 * to return an object of a specific type to make the MappingLdifReader implementation work, this basic setting is sufficient
 * to demonstrate its function.
 * 
 * @author Keith Barlow
 *
 */
public class MyMapper implements RecordMapper<LdapAttributes> {

	public LdapAttributes mapRecord(LdapAttributes attributes) {
		return attributes;
	}

}
