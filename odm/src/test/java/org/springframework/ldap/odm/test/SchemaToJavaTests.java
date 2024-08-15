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

package org.springframework.ldap.odm.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.ldap.LdapName;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.odm.core.impl.OdmManagerImpl;
import org.springframework.ldap.odm.test.utils.CompilerInterface;
import org.springframework.ldap.odm.test.utils.GetFreePort;
import org.springframework.ldap.odm.tools.SchemaToJava;
import org.springframework.ldap.odm.typeconversion.impl.Converter;
import org.springframework.ldap.odm.typeconversion.impl.ConverterManagerImpl;
import org.springframework.ldap.odm.typeconversion.impl.converters.FromStringConverter;
import org.springframework.ldap.odm.typeconversion.impl.converters.ToStringConverter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

// Tests the generation of entry Java classes from LDAP schema
public final class SchemaToJavaTests {

	private static final Logger LOG = LoggerFactory.getLogger(LdapTests.class);

	private static final LdapName baseName = LdapUtils.newLdapName("o=Whoniverse");

	private static final String tempDir = System.getProperty("java.io.tmpdir");

	// These unit tests require this port to free on localhost
	private static int port;

	private ConverterManagerImpl converterManager;

	private LdapContextSource contextSource;

	@BeforeClass
	public static void setUpClass() throws Exception {
		// Added because the close down of Apache DS on Linux does
		// not seem to free up its port.
		port = GetFreePort.getFreePort();

		// Start an in process LDAP server
		LdapTestUtils.startEmbeddedServer(port, baseName.toString(), "odm-test");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		// Stop the in process LDAP server
		LdapTestUtils.shutdownEmbeddedServer();
	}

	@Before
	public void setUp() throws Exception {
		// Create some basic converters and a converter manager
		this.converterManager = new ConverterManagerImpl();

		Converter ptc = new FromStringConverter();
		this.converterManager.addConverter(String.class, "", Byte.class, ptc);
		this.converterManager.addConverter(String.class, "", Short.class, ptc);
		this.converterManager.addConverter(String.class, "", Integer.class, ptc);
		this.converterManager.addConverter(String.class, "", Long.class, ptc);
		this.converterManager.addConverter(String.class, "", Double.class, ptc);
		this.converterManager.addConverter(String.class, "", Float.class, ptc);
		this.converterManager.addConverter(String.class, "", Boolean.class, ptc);

		Converter tsc = new ToStringConverter();
		this.converterManager.addConverter(Byte.class, "", String.class, tsc);
		this.converterManager.addConverter(Short.class, "", String.class, tsc);
		this.converterManager.addConverter(Integer.class, "", String.class, tsc);
		this.converterManager.addConverter(Long.class, "", String.class, tsc);
		this.converterManager.addConverter(Double.class, "", String.class, tsc);
		this.converterManager.addConverter(Float.class, "", String.class, tsc);
		this.converterManager.addConverter(Boolean.class, "", String.class, tsc);

		// Bind to the directory
		this.contextSource = new LdapContextSource();
		this.contextSource.setUrl("ldap://127.0.0.1:" + port);
		this.contextSource.setUserDn("");
		this.contextSource.setPassword("");
		this.contextSource.setPooled(false);
		this.contextSource.afterPropertiesSet();

		// Clear out any old data - and load the test data
		LdapTestUtils.cleanAndSetup(this.contextSource, baseName, new ClassPathResource("testdata.ldif"));
	}

	@After
	public void tearDown() throws Exception {
		LdapTestUtils.shutdownEmbeddedServer();

		this.contextSource = null;
		this.converterManager = null;
	}

	// Figure out the path of the created Java file
	private static String calculateOutputDirectory(String outputDir, String packageName) {
		// Convert the package name to a path
		Pattern pattern = Pattern.compile("\\.");
		Matcher matcher = pattern.matcher(packageName);
		String sepToUse = File.separator;
		if (sepToUse.equals("\\")) {
			sepToUse = "\\\\";
		}

		return outputDir + File.separator + matcher.replaceAll(sepToUse);
	}

	// Due of the nature of the code under test this unit test is a little unusual:
	//
	// 1) Generate an entry class corresponding to objects classes
	// "inetorgperson, organizationalperson, person, top"
	// using the SchemaToJavaTool
	// 2) Compile the generated code
	// 3) Create an OdmManager to managing the newly created
	// entry class.
	// 4) Use this OdmManager to read an entry from LDAP and check the results.
	//
	@Test
	public void generate() throws Exception {
		final String className = "Person";
		final String packageName = "org.springframework.ldap.odm.testclasses";

		File tempFile = File.createTempFile("test-odm-syntax-to-class-map", ".txt");
		Path from = new ClassPathResource("/syntax-to-class-map.txt").getFile().toPath();
		try (OutputStream to = new FileOutputStream(tempFile)) {
			Files.copy(from, to);
		}

		// Add classes dir to class path - needed for compilation
		System.setProperty("java.class.path",
				System.getProperty("java.class.path") + File.pathSeparator + "target/classes");

		String[] flags = new String[] { "--url", "ldap://127.0.0.1:" + port, "--objectclasses", "organizationalperson",
				"--syntaxmap", tempFile.getAbsolutePath(), "--class", className, "--package", packageName,
				"--outputdir", tempDir };

		// Generate the code using SchemaToJava
		SchemaToJava.main(flags);

		tempFile.delete();

		// Java 5 - we'll use the Java 6 Compiler API once we can drop support for Java 5.
		String javaDir = calculateOutputDirectory(tempDir, packageName);

		CompilerInterface.compile(javaDir, className + ".java");
		// Java 5

		// OK it compiles so lets load our new class
		URL[] urls = new URL[] { new File(tempDir).toURI().toURL() };
		URLClassLoader ucl = new URLClassLoader(urls, getClass().getClassLoader());
		Class<?> clazz = ucl.loadClass(packageName + "." + className);

		// Create our OdmManager using our new class
		OdmManagerImpl odmManager = new OdmManagerImpl(this.converterManager, this.contextSource);
		odmManager.addManagedClass(clazz);

		// And try reading from the directory using it
		LdapName testDn = LdapUtils.newLdapName(baseName);
		testDn.addAll(LdapUtils.newLdapName("cn=William Hartnell,ou=Doctors"));
		Object fromDirectory = odmManager.read(clazz, testDn);

		LOG.debug(String.format("Read - %1$s", fromDirectory));

		// Check some returned values
		Method getDnMethod = clazz.getMethod("getDn");
		Object dn = getDnMethod.invoke(fromDirectory);
		assertThat(testDn).isEqualTo(dn);

		Method getCnIteratorMethod = clazz.getMethod("getCnIterator");
		@SuppressWarnings("unchecked")
		Iterator<String> cnIterator = (Iterator<String>) getCnIteratorMethod.invoke(fromDirectory);
		int cnCount = 0;
		while (cnIterator.hasNext()) {
			cnCount++;
			assertThat("William Hartnell").isEqualTo(cnIterator.next());
		}
		assertThat(1).isEqualTo(cnCount);

		Method telephoneNumberIteratorMethod = clazz.getMethod("getTelephoneNumberIterator");
		@SuppressWarnings("unchecked")
		Iterator<Integer> telephoneNumberIterator = (Iterator<Integer>) telephoneNumberIteratorMethod
			.invoke(fromDirectory);
		int telephoneNumberCount = 0;
		while (telephoneNumberIterator.hasNext()) {
			telephoneNumberCount++;
			assertThat(Integer.valueOf(1)).isEqualTo(telephoneNumberIterator.next());
		}
		assertThat(1).isEqualTo(telephoneNumberCount);

		// Reread and check whether equals and hashCode are at least sane
		Object fromDirectory2 = odmManager.read(clazz, testDn);
		assertThat(fromDirectory).isEqualTo(fromDirectory2);
		assertThat(fromDirectory.hashCode()).isEqualTo(fromDirectory2.hashCode());
	}

}
