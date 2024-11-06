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

package org.springframework.ldap.odm.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tool creates a Java class representation of a set of LDAP object classes for use
 * with {@link org.springframework.ldap.odm.core.OdmManager}.
 * <p>
 * The schema of a named list of object classes is read from an LDAP directory and used to
 * generate a representative Java class. The Java class is automatically annotated with
 * {@link org.springframework.ldap.odm.annotations} for use with
 * {@link org.springframework.ldap.odm.core.OdmManager}.
 * <p>
 * The mapping of LDAP attributes to their Java representations may be configured by
 * supplying the <code>-s</code> flag or the equivalent <code>--syntaxmap</code> flag
 * whose argument is the name of a file with the following structure: <pre>
 * # List of attribute syntax to java class mappings
 *
 * # Syntax					   Java class
 * # ------					   ----------
 *
 * 1.3.6.1.4.1.1466.115.121.1.50, java.lang.Integer
 * 1.3.6.1.4.1.1466.115.121.1.40, some.other.Class
 * </pre>
 * <p>
 * Syntaxes not included in this map will be represented as {@link java.lang.String} if
 * they are returned as Strings by the JNDI LDAP provider and will be represented as
 * <code>byte[]</code> if they are returned by the provider as <code>byte[]</code>.
 * <p>
 * Command line flags are as follows:
 * <p>
 * <ul>
 * <li><code>-c,--class &lt;class name&gt;</code> Name of the Java class to create.
 * Mandatory.</li>
 * <li><code>-s,--syntaxmap &lt;map file&gt;</code> Configuration file of LDAP syntaxes to
 * Java classes mappings. Optional.</li>
 * <li><code>-h,--help</code> Print this help message then exit.</li>
 * <li><code>-k,--package &lt;package name&gt;</code> Package to create the Java class in.
 * Mandatory.</li>
 * <li><code>-l,--url &lt;ldap url&gt;</code> Ldap url of the directory service to bind
 * to. Defaults to <code>ldap://127.0.0.1:389</code>. Optional.</li>
 * <li><code>-o,--objectclasses &lt;LDAP object class lists&gt;</code> Comma separated
 * list of LDAP object classes. Mandatory.</li>
 * <li><code>-u,--username &lt;dn&gt;</code> DN to bind with. Defaults to "".
 * Optional.</li>
 * <li><code>-p,--password &lt;password&gt;</code> Password to bind with. Defaults to "".
 * Optional.</li>
 * <li><code>-t,--outputdir &lt;output directory&gt;</code> Base output directory,
 * defaults to ".". Optional.</li>
 * </ul>
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 */
public final class SchemaToJava {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaToJava.class);

	// Name of the FreeMarker template used to generate the Java code.
	private static final String TEMPLATE_FILE = "oc-to-java.ftl";

	// Name of file containing the list of attributes syntaxes to
	// returned as byte[] by the JNDI LDAP provider.
	private static final String BINARY_FILE = "binary-attributes.txt";

	// Class to use a base for loading resources
	private static final Class<?> DEFAULT_LOADER_CLASS = SchemaToJava.class;

	// Default LDAP Url to bind with
	private static final String DEFAULT_URL = "ldap://127.0.0.1:389";

	// Command line flags
	private enum Flag {

		URL("l", "url"), USERNAME("u", "username"), PASSWORD("p", "password"), OBJECTCLASS("o", "objectclasses"),
		CLASS("c", "class"), PACKAGE("k", "package"), SYNTAX_MAP("s", "syntaxmap"), OUTPUT_DIR("t", "outputdir"),
		HELP("h", "help");

		private String shortName;

		private String longName;

		Flag(String shortName, String longName) {
			this.shortName = shortName;
			this.longName = longName;
		}

		public String getShort() {
			return this.shortName;
		}

		public String getLong() {
			return this.longName;
		}

		@Override
		public String toString() {
			return String.format("short=%1$s, long=%2$s", this.shortName, this.longName);
		}

	}

	private static final Options DEFAULT_OPTIONS = new Options();
	static {
		DEFAULT_OPTIONS.addOption(Flag.URL.getShort(), Flag.URL.getLong(), true,
				"Ldap url (defaults to " + DEFAULT_URL + ")");
		DEFAULT_OPTIONS.addOption(Flag.USERNAME.getShort(), Flag.USERNAME.getLong(), true,
				"DN to bind with (defaults to \"\"");
		DEFAULT_OPTIONS.addOption(Flag.PASSWORD.getShort(), Flag.PASSWORD.getLong(), true,
				"Password to bind with (defaults to \"\"");
		DEFAULT_OPTIONS.addOption(Flag.OBJECTCLASS.getShort(), Flag.OBJECTCLASS.getLong(), true,
				"Comma separated list of object classes");
		DEFAULT_OPTIONS.addOption(Flag.CLASS.getShort(), Flag.CLASS.getLong(), true,
				"Name of the Java class to create");
		DEFAULT_OPTIONS.addOption(Flag.PACKAGE.getShort(), Flag.PACKAGE.getLong(), true,
				"Package to create the Java class in");
		DEFAULT_OPTIONS.addOption(Flag.SYNTAX_MAP.getShort(), Flag.SYNTAX_MAP.getLong(), true,
				"Syntax map file (optional)");
		DEFAULT_OPTIONS.addOption(Flag.OUTPUT_DIR.getShort(), Flag.OUTPUT_DIR.getLong(), true,
				"Base output directory (defaults to .)");
		DEFAULT_OPTIONS.addOption(Flag.HELP.getShort(), Flag.HELP.getLong(), false, "Print this help message");
	}

	/**
	 * Not to be instantiated.
	 */
	private SchemaToJava() {

	}

	// Read list of LDAP syntaxes that are returned as byte[]
	private static Set<String> readBinarySet(File binarySetFile) throws IOException {

		Set<String> result = new HashSet<String>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(binarySetFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.length() > 0) {
					if (trimmed.charAt(0) != '#') {
						String[] parts = trimmed.split("\\s");
						if (parts.length > 0) {
							result.add(parts[0]);
						}
					}
				}
			}
		}
		finally {
			if (reader != null) {
				reader.close();
			}
		}

		return result;
	}

	// Read mappings of LDAP syntaxes to Java classes.
	private static Map<String, String> readSyntaxMap(File syntaxMapFile) throws IOException {

		Map<String, String> result = new HashMap<String, String>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(syntaxMapFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.length() > 0) {
					if (trimmed.charAt(0) != '#') {
						String[] parts = trimmed.split(",");
						if (parts.length != 2) {
							throw new IOException(String.format("Failed to parse line \"%1$s\"", trimmed));
						}
						String partOne = parts[0].trim();
						String partTwo = parts[1].trim();
						if (partOne.length() == 0 || partTwo.length() == 0) {
							throw new IOException(String.format("Failed to parse line \"%1$s\"", trimmed));
						}
						result.put(partOne, partTwo);
					}
				}
			}
		}
		finally {
			if (reader != null) {
				reader.close();
			}
		}

		return result;
	}

	// Bind to the directory, read and process the schema
	private static ObjectSchema readSchema(String url, String user, String pass, SyntaxToJavaClass syntaxToJavaClass,
			Set<String> binarySet, Set<String> objectClasses) throws NamingException, ClassNotFoundException {

		// Set up environment
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		if (user != null) {
			env.put(Context.SECURITY_PRINCIPAL, user);
		}
		if (pass != null) {
			env.put(Context.SECURITY_CREDENTIALS, pass);
		}

		DirContext context = new InitialDirContext(env);
		DirContext schemaContext = context.getSchema("");
		SchemaReader reader = new SchemaReader(schemaContext, syntaxToJavaClass, binarySet);
		ObjectSchema schema = reader.getObjectSchema(objectClasses);

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Schema - %1$s", schema.toString()));
		}

		return schema;
	}

	// Create the Java
	private static void createCode(String packageName, String className, ObjectSchema schema,
			Set<SyntaxToJavaClass.ClassInfo> imports, File outputFile) throws IOException, TemplateException {

		Configuration freeMarkerConfiguration = new Configuration();

		freeMarkerConfiguration.setClassForTemplateLoading(DEFAULT_LOADER_CLASS, "");
		freeMarkerConfiguration.setObjectWrapper(new DefaultObjectWrapper());

		// Build the model for FreeMarker
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("package", packageName);
		model.put("class", className);
		model.put("schema", schema);
		model.put("imports", imports);

		// Have FreeMarker process the model with the template
		Template template = freeMarkerConfiguration.getTemplate(TEMPLATE_FILE);

		if (LOG.isDebugEnabled()) {
			Writer out = new OutputStreamWriter(System.out);
			template.process(model, out);
			out.flush();
		}

		LOG.debug(String.format("Writing java to: %1$s", outputFile.getAbsolutePath()));

		FileOutputStream outputStream = new FileOutputStream(outputFile);
		Writer out = new OutputStreamWriter(outputStream);
		template.process(model, out);
		out.flush();
		out.close();
	}

	// Create the output file for the generated code along with all intervening
	// directories
	private static File makeOutputFile(String outputDir, String packageName, String className) throws IOException {

		// Convert the package name to a path
		Pattern pattern = Pattern.compile("\\.");
		Matcher matcher = pattern.matcher(packageName);
		String sepToUse = File.separator;
		if (sepToUse.equals("\\")) {
			sepToUse = "\\\\";
		}

		// Try to create the necessary directories
		String directoryPath = outputDir + File.separator + matcher.replaceAll(sepToUse);
		File directory = new File(directoryPath);
		File outputFile = new File(directory, className + ".java");

		LOG.debug(String.format("Attempting to create output file at %1$s", outputFile.getAbsolutePath()));

		try {
			directory.mkdirs();
			outputFile.createNewFile();
		}
		catch (SecurityException se) {
			throw new IOException(String.format("Can't write to output file %1$s", outputFile.getAbsoluteFile()), se);
		}
		catch (IOException ioe) {
			throw new IOException(String.format("Can't write to output file %1$s", outputFile.getAbsoluteFile()), ioe);
		}

		return outputFile;
	}

	private static Set<String> parseObjectClassesFlag(String objectClassesFlag) {
		Set<String> objectClasses = new HashSet<String>();

		for (String objectClassFlag : objectClassesFlag.split(",")) {
			if (objectClassFlag.length() > 0) {
				objectClasses.add(objectClassFlag.toLowerCase(Locale.ROOT).trim());
			}
		}

		return objectClasses;
	}

	private static void error(String message) {
		System.err.println(String.format("%1$s: %2$s", SchemaToJava.class.getSimpleName(), message));
		System.exit(1);
	}

	public static void main(String[] argv) {
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;

		// Parse out the command line options
		try {
			cmd = parser.parse(DEFAULT_OPTIONS, argv);
		}
		catch (ParseException ex) {
			error(ex.toString());
		}

		// If the help flag is specified ignore other flags, print a usage message and
		// exit
		if (cmd.hasOption(Flag.HELP.getShort())) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(120, SchemaToJava.class.getSimpleName(), null, DEFAULT_OPTIONS, null, true);
			System.exit(0);
		}

		// Class name flag
		String className = cmd.getOptionValue(Flag.CLASS.getShort());
		if (className == null) {
			error("You must specify the name of a Java class to create");
		}

		// Package name flag
		String packageName = cmd.getOptionValue(Flag.PACKAGE.getShort());
		if (packageName == null) {
			error("You must specifiy a package name");
		}

		// Output base directory
		String outputDir = cmd.getOptionValue(Flag.OUTPUT_DIR.getShort(), ".");
		File outputFile = null;
		try {
			outputFile = makeOutputFile(outputDir, packageName, className);
		}
		catch (IOException ex) {
			error(ex.toString());
		}

		// Get the flags we need to bind to the directory
		String url = cmd.getOptionValue(Flag.URL.getShort(), DEFAULT_URL);
		String user = cmd.getOptionValue(Flag.USERNAME.getShort());
		String pass = cmd.getOptionValue(Flag.PASSWORD.getShort());

		// Parse out object classes
		String objectClassesFlag = cmd.getOptionValue(Flag.OBJECTCLASS.getShort());
		if (objectClassesFlag == null) {
			error("You must specificy a package name");
		}
		Set<String> objectClasses = parseObjectClassesFlag(objectClassesFlag);
		if (objectClasses.size() == 0) {
			error("You must specificy a package name");
		}

		// Look for the optional syntax to Java class mapping file
		String syntaxMapFileName = cmd.getOptionValue(Flag.SYNTAX_MAP.getShort(), null);
		SyntaxToJavaClass syntaxToJavaClass = new SyntaxToJavaClass(new HashMap<String, String>());
		if (syntaxMapFileName != null) {
			File syntaxMapFile = new File(syntaxMapFileName);
			if (syntaxMapFile.canRead()) {
				try {
					syntaxToJavaClass = new SyntaxToJavaClass(readSyntaxMap(syntaxMapFile));
				}
				catch (IOException ex) {
					error(String.format("Error reading syntax map file %1$s - %2$s", syntaxMapFile.getAbsolutePath(),
							ex.toString()));
				}
			}
			else {
				error(String.format("Cannot read syntax map file %s$1", syntaxMapFile.getAbsolutePath()));
			}
		}

		// Read binary mapping file
		URL binarySetUrl = DEFAULT_LOADER_CLASS.getResource(BINARY_FILE);
		if (binarySetUrl == null) {
			error(String.format("Can't locatate binary mappings file %1$s", BINARY_FILE));
		}
		File binarySetFile = new File(binarySetUrl.getFile());
		if (!binarySetFile.canRead()) {
			error(String.format("Can't read from binary mappings file %1$s", BINARY_FILE));
		}
		Set<String> binarySet = null;
		try {
			binarySet = readBinarySet(binarySetFile);
		}
		catch (IOException ex) {
			error(String.format("Error reading binary set file %1$s - %2$s", binarySetFile.getAbsolutePath(), ex));
		}

		// Read schema from the directory
		ObjectSchema schema = null;
		try {
			schema = readSchema(url, user, pass, syntaxToJavaClass, binarySet, objectClasses);
		}
		catch (NamingException ne) {
			error(String.format("Error processing schema - %1$s", ne));
		}
		catch (ClassNotFoundException cnfe) {
			error(String.format("Error processing schema - %1$s", cnfe));
		}

		// Work out what imports we need
		Set<SyntaxToJavaClass.ClassInfo> imports = new HashSet<SyntaxToJavaClass.ClassInfo>();
		for (AttributeSchema attributeSchema : schema.getMay()) {
			SyntaxToJavaClass.ClassInfo classInfo = syntaxToJavaClass.getClassInfo(attributeSchema.getSyntax());
			if (classInfo != null) {
				String classPackageName = classInfo.getPackageName();
				if (classPackageName != null && classPackageName.length() > 0) {
					imports.add(classInfo);
				}
			}
		}

		// Create the Java code
		try {
			createCode(packageName, className, schema, imports, outputFile);
		}
		catch (TemplateException te) {
			error(String.format("Error generating code - %1$s", te.toString()));
		}
		catch (IOException ioe) {
			error(String.format("Error generatign code - %1$s", ioe.toString()));
		}
	}

}
