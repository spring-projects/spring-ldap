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

package org.springframework.ldap.itest.ad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.odm.core.impl.OdmManagerImpl;
import org.springframework.ldap.odm.tools.SchemaToJava;
import org.springframework.ldap.odm.typeconversion.impl.Converter;
import org.springframework.ldap.odm.typeconversion.impl.ConverterManagerImpl;
import org.springframework.ldap.odm.typeconversion.impl.converters.FromStringConverter;
import org.springframework.ldap.odm.typeconversion.impl.converters.ToStringConverter;

import static org.assertj.core.api.Assertions.assertThat;

// Tests the generation of entry Java classes from LDAP schema
public final class SchemaToJavaAdITests {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaToJavaAdITests.class);

	private static final DistinguishedName baseName = new DistinguishedName("dc=261consulting,dc=local");

	private static final String tempDir = System.getProperty("java.io.tmpdir");

	private static final String USER_DN = "CN=ldaptest,CN=Users,DC=261consulting,DC=local";

	private static final String PASSWORD = "Buc8xe6AZiewoh7";

	// These unit tests require this port to free on localhost
	private static int port = 13636;

	private ConverterManagerImpl converterManager;

	private LdapContextSource contextSource;

	private LdapTemplate ldapTemplate;

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
		this.contextSource.setUrl("ldaps://127.0.0.1:" + port);
		this.contextSource.setUserDn(USER_DN);
		this.contextSource.setPassword(PASSWORD);
		this.contextSource.setPooled(false);
		this.contextSource.setBase("dc=261consulting,dc=local");
		HashMap<String, Object> baseEnvironment = new HashMap<>() {
			{
				put("java.naming.ldap.attributes.binary",
						"thumbnailLogo replPropertyMetaData partialAttributeSet registeredAddress userPassword telexNumber partialAttributeDeletionList mS-DS-ConsistencyGuid attributeCertificateAttribute thumbnailPhoto teletexTerminalIdentifier replUpToDateVector dSASignature objectGUID");
			}
		};
		this.contextSource.setBaseEnvironmentProperties(baseEnvironment);
		this.contextSource.afterPropertiesSet();

		this.ldapTemplate = new LdapTemplate(this.contextSource);

		cleanup();

		DirContextAdapter ctx = new DirContextAdapter("cn=William Hartnell,cn=Users");
		ctx.setAttributeValues("objectclass",
				new String[] { "person", "inetorgperson", "organizationalperson", "top" });
		ctx.setAttributeValue("cn", "William Hartnell");
		ctx.addAttributeValue("description", "First Doctor");
		ctx.addAttributeValue("description", "Grumpy");
		ctx.addAttributeValue("sn", "Hartnell");
		ctx.addAttributeValue("telephonenumber", "1");

		this.ldapTemplate.bind(ctx);
	}

	@After
	public void cleanup() {
		this.ldapTemplate.unbind("cn=William Hartnell,cn=Users");
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
	public void verifySchemaToJavaOnAd() throws Exception {
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

		String[] flags = new String[] { "--url", "ldaps://127.0.0.1:" + port, "--objectclasses", "organizationalperson",
				"--syntaxmap", tempFile.getAbsolutePath(), "--class", className, "--package", packageName,
				"--outputdir", tempDir, "--username", USER_DN, "--password", PASSWORD };

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
		DistinguishedName testDn = new DistinguishedName("cn=William Hartnell,cn=Users");
		Object fromDirectory = odmManager.read(clazz, testDn);

		LOG.debug(String.format("Read - %1$s", fromDirectory));

		// Check some returned values
		Method getDnMethod = clazz.getMethod("getDn");
		Object dn = getDnMethod.invoke(fromDirectory);
		assertThat(dn).isEqualTo(testDn);

		Method getCnIteratorMethod = clazz.getMethod("getCn");
		@SuppressWarnings("unchecked")
		String cn = (String) getCnIteratorMethod.invoke(fromDirectory);
		assertThat(cn).isEqualTo("William Hartnell");

		Method telephoneNumberIteratorMethod = clazz.getMethod("getTelephoneNumber");
		@SuppressWarnings("unchecked")
		String telephoneNumber = (String) telephoneNumberIteratorMethod.invoke(fromDirectory);
		assertThat(telephoneNumber).isEqualTo("1");

		// Reread and check whether equals and hashCode are at least sane
		Object fromDirectory2 = odmManager.read(clazz, testDn);
		assertThat(fromDirectory2).isEqualTo(fromDirectory);
		assertThat(fromDirectory2.hashCode()).isEqualTo(fromDirectory.hashCode());
	}

}
