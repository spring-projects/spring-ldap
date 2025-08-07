/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.ldif;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.ldif.parser.LdifParser;
import org.springframework.ldap.schema.BasicSchemaSpecification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit test for LdifParser.
 *
 * Test results in complete end to end test of all LdifParser functionality: 1.) Open a
 * file 2.) Read lines and compose an attribute. 3.) Parse the attribute and create a
 * LdapAttribute object. 4.) Repeat until end of record (Identify end of record). 5.)
 * Return a valid LdapAttributes object. 6.) Close file upon completion.
 *
 * Provided test file is comprised of sample LDIFs from RFC2849 and exhausts the full
 * range of the functionality prescribed by RFC2849 for the LDAP Data Interchange Format
 * (LDIF).
 *
 * @author Keith Barlow
 *
 */
public class LdifParserTests {

	private static Logger log = LoggerFactory.getLogger(LdifParserTests.class);

	private LdifParser parser;

	/**
	 * Default constructor: loads a preselected resource with sample LDIF entries. Each
	 * entry is parsed and checked for a DN and objectclass. Output is printed for visual
	 * verification of LDIF correctness.
	 */
	public LdifParserTests() {
		this.parser = new LdifParser(new ClassPathResource("test.ldif"));
		this.parser.setRecordSpecification(new BasicSchemaSpecification());
	}

	/**
	 * Setup: opens file.
	 */
	@BeforeEach
	public void openLdif() {
		try {
			this.parser.open();
		}
		catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Executes test: reads all records from LDIF file and validates an LdapAttributes
	 * object is successfully created.
	 */
	@Test
	public void parseLdif() {
		int count = 0;

		try {
			LdapAttributes attributes;

			while (this.parser.hasMoreRecords()) {
				try {
					attributes = this.parser.getRecord();
					log.info("attributes:\n" + attributes);
					if (attributes != null) {
						assertThat(attributes.getDN() != null).isTrue();
						assertThat(attributes.get("objectclass") != null).isTrue();
						count++;
					}
				}
				catch (InvalidAttributeFormatException ex) {
					log.error("Invalid attribute", ex);
					if (count != 6) {
						fail(ex.getMessage());
					}
				}

				log.debug("hasMoreRecords: " + this.parser.hasMoreRecords());
			}

			log.info("record count: " + count);
			// assertThat(count == 8).as("An incorrect number of records were
			// parsed.").isTrue();

			log.info("Done!");

		}
		catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	/**
	 * Cleanup: closes file.
	 */
	@AfterEach
	public void closeLdif() {
		try {
			this.parser.close();
		}
		catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

}
