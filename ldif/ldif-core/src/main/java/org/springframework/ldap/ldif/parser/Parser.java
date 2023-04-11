/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.ldif.parser;

import java.io.IOException;

import javax.naming.directory.Attributes;

import org.springframework.core.io.Resource;

/**
 * The Parser interface represents the required methods to be implemented by parser
 * utilities. These methods are the base set of methods needed to provide parsing ability.
 *
 * @author Keith Barlow
 */
public interface Parser {

	/**
	 * Sets the resource to parse.
	 * @param resource The resource to parse.
	 */
	void setResource(Resource resource);

	/**
	 * Sets the control parameter for specifying case sensitivity on creation of the
	 * {@link Attributes} object.
	 * @param caseInsensitive The resource to parse.
	 */
	void setCaseInsensitive(boolean caseInsensitive);

	/**
	 * Opens the resource: the resource must be opened prior to parsing.
	 * @throws IOException if a problem is encountered while trying to open the resource.
	 */
	void open() throws IOException;

	/**
	 * Closes the resource after parsing.
	 * @throws IOException if a problem is encountered while trying to close the resource.
	 */
	void close() throws IOException;

	/**
	 * Resets the line read parser.
	 * @throws IOException if a problem is encountered while trying to reset the resource.
	 */
	void reset() throws IOException;

	/**
	 * True if the resource contains more records; false otherwise.
	 * @return boolean indicating whether or not the end of record has been reached.
	 * @throws IOException if a problem is encountered while trying to validate the
	 * resource is ready.
	 */
	boolean hasMoreRecords() throws IOException;

	/**
	 * Parses the next record from the resource.
	 * @return LdapAttributes object representing the record parsed.
	 * @throws IOException if a problem is encountered while trying to read from the
	 * resource.
	 */
	Attributes getRecord() throws IOException;

	/**
	 * Indicates whether or not the parser is ready to to return results.
	 * @return boolean indicator
	 * @throws IOException if there is a problem with the underlying resource.
	 */
	boolean isReady() throws IOException;

}
