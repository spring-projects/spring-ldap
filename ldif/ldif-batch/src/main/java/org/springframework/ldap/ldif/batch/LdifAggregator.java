/**
 * 
 */
package org.springframework.ldap.ldif.batch;

import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.ldap.core.LdapAttributes;

/**
 * The {@link LdifAggregator LdifAggregator} object is an implementation of the {@link org.springframework.batch.item.file.transform.LineAggregator LineAggregator}
 * interface for use with a {@link org.springframework.batch.item.file.FlatFileItemWriter FlatFileItemWriter} to write LDIF records to a file.
 * 
 * @author Keith Barlow
 *
 */
public class LdifAggregator implements LineAggregator<LdapAttributes> {

	/**
	 * Returns a {@link java.lang.String String} containing a properly formated LDIF.
	 * 
	 * @param item LdapAttributes object to convert to string.
	 * @return string representation of the object LDIF format (in accordance with RFC 2849).
	 */
	public String aggregate(LdapAttributes item) {
		return item.toString();
	}

}
