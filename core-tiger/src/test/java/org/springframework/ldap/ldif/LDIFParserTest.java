package org.springframework.ldap.ldif;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.ldif.LdapAttributes;
import org.springframework.ldap.ldif.parser.LDIFParser;

/**
 * Unit test for LDIFParser.
 * 
 * Test results in complete end to end test of all LDIFParser functionality:
 * 	1.) Open a file
 * 	2.) Read lines and compose an attribute.
 * 	3.) Parse the attribute and create a LdapAttribute object.
 * 	4.) Repeat until end of record (Identify end of record).
 * 	5.) Return a valid LdapAttributes object.
 * 	6.) Close file upon completion.
 * 
 * Provided test file is comprised of sample LDIFs from RFC2849 and exhausts the full range of
 * the functionality prescribed by RFC2849 for the LDAP Data Interchange Format (LDIF).
 * 
 * @author Keith Barlow
 * 
 */
public class LDIFParserTest {

	private static Log log = LogFactory.getLog(LDIFParserTest.class);
	
	private LDIFParser parser;

	/**
	 * Default constructor: loads a preselected resource with sample LDIF entries.
	 * Each entry is parsed and checked for a DN and objectclass.  Output is printed for visual verification
	 * of LDIF correctness.
	 */
	public LDIFParserTest() {
		parser = new LDIFParser(new ClassPathResource("test.ldif"));
	}
	
	/**
	 * Setup: opens file.
	 */
	@Before
	public void openLdif() {
		try {
			parser.open();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Executes test: reads all records from LDIF file and validates an LdapAttributes object is successfully created.
	 */
	@Test
	public void parseLdif() {
		try {
			LdapAttributes attributes;
			int count = 0;
			
			while (parser.hasMoreRecords()) {
				attributes = parser.getRecord();
				log.info("attributes:\n" + attributes);
				if (attributes != null) {
					assertTrue("A dn is required.", attributes.getDN() != null);
					assertTrue("Object class is required.", attributes.get("objectclass") != null);
					count++;
				}
				
				log.debug("hasMoreRecords: " + parser.hasMoreRecords());
			}
			
			log.info("record count: " + count);
			log.info("Done!");
			
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Cleanup: closes file.
	 */
	@After
	public void closeLdif() {
		try {
			parser.close();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
