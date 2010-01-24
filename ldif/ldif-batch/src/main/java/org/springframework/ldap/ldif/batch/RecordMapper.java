package org.springframework.ldap.ldif.batch;

import org.springframework.ldap.core.LdapAttributes;

/**
 * This interface should be implemented to map {@link LdapAttributes LdapAttributes} objects to POJOs. The resulting 
 * implementations can be used in the {@link MappingLdifReader MappingLdifReader}.
 * 
 * @author Keith Barlow
 *
 * @param <T>
 */
public interface RecordMapper<T> {

	/**
	 * Maps an {@link LdapAttributes LdapAttributes} object to the specified type.
	 * 
	 * @param attributes
	 * @return object of type T
	 */
	T mapRecord(LdapAttributes attributes);
	
}
