/*
 * Copyright 2005-2013 the original author or authors.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.ldif.InvalidRecordFormatException;
import org.springframework.ldap.ldif.support.AttributeValidationPolicy;
import org.springframework.ldap.ldif.support.DefaultAttributeValidationPolicy;
import org.springframework.ldap.ldif.support.LineIdentifier;
import org.springframework.ldap.ldif.support.SeparatorPolicy;
import org.springframework.ldap.schema.DefaultSchemaSpecification;
import org.springframework.ldap.schema.Specification;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The {@link LdifParser LdifParser} is the main class of the
 * {@link org.springframework.ldap.ldif} package. This class reads lines from a resource
 * and assembles them into an {@link LdapAttributes LdapAttributes} object. The
 * {@link LdifParser LdifParser} does ignores <i>changetype</i> LDIF entries as their
 * usefulness in the context of an application has yet to be determined.
 * <p>
 * <b>Design</b><br>
 * {@link LdifParser LdifParser} provides the main interface for operation but requires
 * three supporting classes to enable operation:
 * <ul>
 * <li>{@link SeparatorPolicy SeparatorPolicy} - establishes the mechanism by which lines
 * are assembled into attributes.</li>
 * <li>{@link AttributeValidationPolicy AttributeValidationPolicy} - ensures that
 * attributes are correctly structured prior to parsing.</li>
 * <li>{@link Specification Specification} - provides a mechanism by which object
 * structure can be validated after assembly.</li>
 * </ul>
 * Together, these 4 classes read from the resource line by line and translate the data
 * into objects for use.
 * <p>
 * <b>Usage</b><br>
 * {@link #getRecord() getRecord()} reads the next available record from the resource.
 * Lines are read and passed to the {@link SeparatorPolicy SeparatorPolicy} for
 * interpretation. The parser continues to read lines and appends them to the buffer until
 * it encounters the start of a new attribute or an end of record delimiter. When the new
 * attribute or end of record is encountered, the buffer is passed to the
 * {@link AttributeValidationPolicy AttributeValidationPolicy} which ensures the buffer
 * conforms to a valid attribute definition as defined in RFC2849 and returns an
 * {@link org.springframework.ldap.core.LdapAttribute LdapAttribute} object which is then
 * added to the record, an {@link LdapAttributes LdapAttributes} object. Upon encountering
 * the end of record, the record is validated by the {@link Specification Specification}
 * policy and, if valid, returned to the requester.
 * <p>
 * <i>NOTE: By default, objects are not validated. If validation is required, an
 * appropriate specification object must be set.</i>
 * <p>
 * The parser requires the resource to be {@link #open() open()} prior to an invocation of
 * {@link #getRecord() getRecord()}. {@link #hasMoreRecords() hasMoreRecords()} can be
 * used to loop over the resource until all records have been retrieved. Likewise, the
 * {@link #reset() reset()} method will reset the resource.
 * <p>
 * Objects implementing the {@link javax.naming.directory.Attributes Attributes} interface
 * are required to support a case sensitivity setting which controls whether or not the
 * attribute IDs of the object are case sensitive. The {@link #caseInsensitive
 * caseInsensitive} setting of the {@link LdifParser LdifParser} is passed to the
 * constructor of any {@link javax.naming.directory.Attributes Attributes} created. The
 * default value for this setting is true so that case insensitive objects are created.
 *
 * @author Keith Barlow
 *
 */
public class LdifParser implements Parser, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(LdifParser.class);

	/**
	 * The resource to parse.
	 */
	private Resource resource;

	/**
	 * A BufferedReader to read the file.
	 */
	private BufferedReader reader;

	/**
	 * The SeparatorPolicy to use for interpreting attributes from the lines of the
	 * resource.
	 */
	private SeparatorPolicy separatorPolicy = new SeparatorPolicy();

	/**
	 * The AttributeValidationPolicy to use to interpret attributes.
	 */
	private AttributeValidationPolicy attributePolicy = new DefaultAttributeValidationPolicy();

	/**
	 * The RecordSpecification for validating records produced.
	 */
	private Specification<LdapAttributes> specification = new DefaultSchemaSpecification();

	/**
	 * This setting is used to control the case sensitivity of LdapAttribute objects
	 * returned by the parser.
	 */
	private boolean caseInsensitive = true;

	/**
	 * Default constructor.
	 */
	public LdifParser() {

	}

	/**
	 * Creates a LdifParser with the indicated case sensitivity setting.
	 * @param caseInsensitive Case sensitivity setting for LdapAttributes objects returned
	 * by the parser.
	 */
	public LdifParser(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}

	/**
	 * Creates an LdifParser for the specified resource with the provided case sensitivity
	 * setting.
	 * @param resource The resource to parse.
	 * @param caseInsensitive Case sensitivity setting for LdapAttributes objects returned
	 * by the parser.
	 */
	public LdifParser(Resource resource, boolean caseInsensitive) {
		this.resource = resource;
		this.caseInsensitive = caseInsensitive;
	}

	/**
	 * Convenience constructor for resource specification.
	 * @param resource The resource to parse.
	 */
	public LdifParser(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Convenience constructor: accepts a File object.
	 * @param file The file to parse.
	 */
	public LdifParser(File file) {
		this.resource = new FileSystemResource(file);
	}

	/**
	 * Set the separator policy.
	 *
	 * The default separator policy should suffice for most needs.
	 * @param separatorPolicy Separator policy.
	 */
	public void setSeparatorPolicy(SeparatorPolicy separatorPolicy) {
		this.separatorPolicy = separatorPolicy;
	}

	/**
	 * Policy object enforcing the rules for acceptable attributes.
	 * @param avPolicy Attribute validation policy.
	 */
	public void setAttributeValidationPolicy(AttributeValidationPolicy avPolicy) {
		this.attributePolicy = avPolicy;
	}

	/**
	 * Policy object for enforcing rules to acceptable LDAP objects.
	 *
	 * This policy may be used to enforce schema restrictions.
	 * @param specification
	 */
	public void setRecordSpecification(Specification<LdapAttributes> specification) {
		this.specification = specification;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setCaseInsensitive(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}

	public void open() throws IOException {
		Assert.notNull(this.resource, "Resource must be set.");
		this.reader = new BufferedReader(new InputStreamReader(this.resource.getInputStream()));
	}

	public boolean isReady() throws IOException {
		return this.reader.ready();
	}

	public void close() throws IOException {
		if (this.resource.isOpen())
			this.reader.close();
	}

	public void reset() throws IOException {
		Assert.notNull(this.reader, "A reader has not been obtained.");
		this.reader.reset();
	}

	public boolean hasMoreRecords() throws IOException {
		return this.reader.ready();
	}

	public LdapAttributes getRecord() throws IOException {
		Assert.notNull(this.reader, "A reader must be obtained: parser not open.");

		if (!this.reader.ready()) {
			LOG.debug("Reader not ready!");
			return null;
		}

		LdapAttributes record = null;
		StringBuilder builder = new StringBuilder();

		String line = this.reader.readLine();

		while (true) {

			LineIdentifier identifier = this.separatorPolicy.assess(line);

			switch (identifier) {
			case NewRecord:
				LOG.trace("Starting new record.");
				// Start new record.
				record = new LdapAttributes(this.caseInsensitive);
				builder = new StringBuilder(line);

				break;

			case Control:
				LOG.trace("'control' encountered.");

				// Log WARN and discard record.
				LOG.warn("LDIF change records have no implementation: record will be ignored.");
				builder = null;
				record = null;

				break;

			case ChangeType:
				LOG.trace("'changetype' encountered.");

				// Log WARN and discard record.
				LOG.warn("LDIF change records have no implementation: record will be ignored.");
				builder = null;
				record = null;

				break;

			case Attribute:
				// flush buffer.
				addAttributeToRecord(builder.toString(), record);

				LOG.trace("Starting new attribute.");
				// Start new attribute.
				builder = new StringBuilder(line);

				break;

			case Continuation:
				LOG.trace("...appending line to buffer.");
				// Append line to buffer.
				builder.append(line.replaceFirst(" ", ""));

				break;

			case EndOfRecord:
				LOG.trace("...done parsing record. (EndOfRecord)");

				// Validate record and return.
				if (record == null) {
					return null;
				}
				else {
					try {
						// flush buffer.
						addAttributeToRecord(builder.toString(), record);

						if (this.specification.isSatisfiedBy(record)) {
							LOG.debug("record parsed:\n" + record);
							return record;

						}
						else {
							throw new InvalidRecordFormatException(
									"Record [dn: " + record.getDN() + "] does not conform to specification.");
						}
					}
					catch (NamingException e) {
						LOG.error("Error adding attribute to record", e);
						return null;
					}
				}

			default:
				// Take no action -- applies to VersionIdentifier, Comments, and voided
				// records.
			}

			line = this.reader.readLine();
			if (line == null && record == null) {
				// Never encountered a valid record.
				return null;
			}
		}

	}

	private void addAttributeToRecord(String buffer, LdapAttributes record) {
		try {
			if (StringUtils.hasLength(buffer) && record != null) {
				// Validate previous attribute and add to record.
				Attribute attribute = this.attributePolicy.parse(buffer);

				if (attribute.getID().equalsIgnoreCase("dn")) {
					LOG.trace("...adding DN to record.");

					String dn;
					if (attribute.get() instanceof byte[]) {
						dn = new String((byte[]) attribute.get());
					}
					else {
						dn = (String) attribute.get();
					}

					record.setName(LdapUtils.newLdapName(dn));

				}
				else {
					LOG.trace("...adding attribute to record.");
					Attribute attr = record.get(attribute.getID());

					if (attr != null) {
						attr.add(attribute.get());
					}
					else {
						record.put(attribute);
					}
				}
			}
		}
		catch (NamingException e) {
			LOG.error("Error adding attribute to record", e);
		}
		catch (NoSuchElementException e) {
			LOG.error("Error adding attribute to record", e);
		}
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.resource, "A resource to parse is required.");
		Assert.isTrue(this.resource.exists(), this.resource.getDescription() + ": resource does not exist!");
		Assert.isTrue(this.resource.isReadable(), "Resource is not readable.");
	}

}
