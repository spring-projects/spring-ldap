/**
 * 
 */
package org.springframework.ldap.ldif.batch;

import org.springframework.ldap.core.LdapAttributes;

/**
 * This interface can be used to operate on skipped records in the {@link LdifReader LdifReader} and the 
 * {@link MappingLdifReader MappingLdifReader}.
 * 
 * @author Keith Barlow
 *
 */
public interface RecordCallbackHandler {

	/**
	 * Execute operations on the supplied record.
	 * 
	 * @param attributes
	 */
	void handleRecord(LdapAttributes attributes);
	
}
