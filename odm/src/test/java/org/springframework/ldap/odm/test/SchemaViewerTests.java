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

package org.springframework.ldap.odm.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.ldap.LdapName;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.ldap.odm.test.utils.ExecuteRunnable;
import org.springframework.ldap.odm.test.utils.GetFreePort;
import org.springframework.ldap.odm.test.utils.RunnableTests;
import org.springframework.ldap.odm.tools.SchemaViewer;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public final class SchemaViewerTests {

	// Base DN for test data
	private static final LdapName baseName = LdapUtils.newLdapName("o=Whoniverse");

	private static final String lineSeparator = System.getProperty("line.separator");

	private static int port;

	private static String[] commonFlags;

	@BeforeClass
	public static void setUpClass() throws Exception {
		// Added because the close down of Apache DS on Linux does
		// not seem to free up its port.
		port = GetFreePort.getFreePort();

		commonFlags = new String[] { "--url", "ldap://127.0.0.1:" + port, "--username", "", "--password", "",
				"--error" };

		// Start an in process LDAP server
		LdapTestUtils.startEmbeddedServer(port, baseName.toString(), "odm-test");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		LdapTestUtils.shutdownEmbeddedServer();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private static String runSchemaViewer(String... flags) {
		String result = null;
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output));

			List<String> commandFlags = new ArrayList<>(Arrays.asList(commonFlags));
			commandFlags.addAll(Arrays.asList(flags));

			SchemaViewer.main(commandFlags.toArray(new String[0]));

			// Turn end of lines into | for portability
			result = output.toString().trim().replace(lineSeparator, "|");

		}
		finally {
			System.setErr(originalOut);
		}

		return result;
	}

	// This makes the test dependent on the order in which the data is returned - it is
	// invalid to assume that this will not change
	private static TestData[] viewerTestData = new TestData[] { new TestData("-o", "top",
			"NAME:top|MUST:objectClass |X-SCHEMA:system |NAME:top |NUMERICOID:2.5.6.0 |DESC:top of the superclass chain |ABSTRACT:true"),
			new TestData("-o", "country",
					"NAME:country|MUST:c |X-SCHEMA:core |SUP:top |NAME:country |STRUCTURAL:true |NUMERICOID:2.5.6.2 |DESC:RFC2256: a country |MAY:searchGuide description"),
			new TestData("-a", "sn",
					"NAME:sn|NAME:sn surname |SUBSTR:caseIgnoreSubstringsMatch |X-SCHEMA:core |SYNTAX:1.3.6.1.4.1.1466.115.121.1.15 |NUMERICOID:2.5.4.4 |SUP:name |DESC:RFC2256: last (family) name(s) for which the entity is known by |USAGE:userApplications |EQUALITY:caseIgnoreMatch"),
			new TestData("-a", "jpegPhoto",
					"NAME:jpegPhoto|X-SCHEMA:inetorgperson |SYNTAX:1.3.6.1.4.1.1466.115.121.1.28 |NAME:jpegPhoto |USAGE:userApplications |NUMERICOID:0.9.2342.19200300.100.1.60 |DESC:RFC2798: a JPEG image"), };

	// Very simple test - mainly just to exercise the code and to
	// ensure we get representative test coverage
	@Test
	public void testSchemaViewer() throws Exception {
		new ExecuteRunnable<TestData>().runTests(new RunnableTests<>() {
			public void runTest(TestData testData) {
				String result = runSchemaViewer(testData.flag, testData.value);
				assertThat(testData.result).isEqualTo(result);
			}
		}, viewerTestData);
	}

	private static class TestData {

		private final String flag;

		private final String value;

		private final String result;

		TestData(String flag, String value, String result) {
			this.flag = flag;
			this.value = value;
			this.result = result;
		}

	}

}
