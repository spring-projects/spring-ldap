package org.springframework.ldap.ldif.parser;

import java.io.IOException;

import javax.naming.directory.Attributes;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * The Parser interface represents the required methods to be implemented by parser utilities.
 * These methods are the base set of methods needed to provide parsing ability.
 * @author Keith Barlow
 *
 */
public interface Parser extends InitializingBean {

	/**
	 * Sets the resource to parse.
	 * 
	 * @param resource The resource to parse.
	 */
	public void setResource(Resource resource);
	
	/**
	 * Opens the resource: the resource must be opened prior to parsing.
	 * 
	 * @throws IOException if a problem is encountered while trying to open the resource.
	 */
	public void open() throws IOException;
	
	/**
	 * Closes the resource after parsing.
	 * 
	 * @throws IOException if a problem is encountered while trying to close the resource.
	 */
	public void close() throws IOException;
	
	/**
	 * Resets the line read parser.
	 * 
	 * @throws Exception if a problem is encountered while trying to reset the resource.
	 */
	public void reset() throws IOException;
	
	/**
	 * True if the resource contains more records; false otherwise.
	 * 
	 * @return boolean indicating whether or not the end of record has been reached.
	 * @throws IOException if a problem is encountered while trying to validate the resource is ready.
	 */
	public boolean hasMoreRecords() throws IOException;
	
	/**
	 * Parses the next record from the resource.
	 * 
	 * @return LdapAttributes object representing the record parsed.
	 * @throws IOException if a problem is encountered while trying to read from the resource.
	 */
	public Attributes getRecord() throws IOException;
	
}
