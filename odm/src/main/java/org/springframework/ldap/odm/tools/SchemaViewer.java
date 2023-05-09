/*
 * Copyright 2005-2023 the original author or authors.
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

import java.io.PrintStream;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * A simple utility to list LDAP directory schema.
 * <p>
 * <code>SchemaViewer</code> takes the following flags:
 * <ul>
 * <li><code>-h,--help&lt;</code> Print this help message</li>
 * <li><code>-l,--url &lt;arg&gt;</code> Ldap url of directory to bind to (defaults to
 * ldap://127.0.0.1:389)</li>
 * <li><code>-u,--username &lt;arg&gt;</code> DN to bind with (defaults to "")</li>
 * <li><code>-p,--password &lt;arg&gt;</code> Password to bind with (defaults to "")</li>
 * <li><code>-o,--objectclass &lt;arg&gt;</code> Object class name or ? for all. Print
 * object class schema</li>
 * <li><code>-a,--attribute &lt;arg&gt;</code> Attribute name or ? for all. Print
 * attribute schema</li>
 * <li><code>-s,--syntax &lt;arg&gt;</code> Syntax or ? for all. Print syntax</li>
 * </ul>
 *
 * Only one of <code>-a</code>, <code>-o</code> and <code>-s</code> should be specified.
 *
 * @author Paul Harvey &lt;paul.at.pauls-place.me.uk&gt;
 *
 */
public final class SchemaViewer {

	private static final String DEFAULT_URL = "ldap://127.0.0.1:389";

	private enum Flag {

		URL("l", "url"), USERNAME("u", "username"), PASSWORD("p", "password"), OBJECTCLASS("o",
				"objectclass"), ATTRIBUTE("a",
						"attribute"), SYNTAX("s", "syntax"), HELP("h", "help"), ERROR("e", "error");

		private String shortName;

		private String longName;

		private Flag(String shortName, String longName) {
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

	private enum SchemaContext {

		OBJECTCLASS("ClassDefinition"), ATTRIBUTE("AttributeDefinition"), SYNTAX("SyntaxDefinition");

		private String value;

		private SchemaContext(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			return String.format("value=%1$s", this.value);
		}

	}

	private static final Options DEFAULT_OPTIONS = new Options();
	static {
		DEFAULT_OPTIONS.addOption(Flag.URL.getShort(), Flag.URL.getLong(), true,
				"Ldap url (defaults to " + DEFAULT_URL + ")");
		DEFAULT_OPTIONS.addOption(Flag.USERNAME.getShort(), Flag.USERNAME.getLong(), true,
				"DN to bind with (defaults to \"\")");
		DEFAULT_OPTIONS.addOption(Flag.PASSWORD.getShort(), Flag.PASSWORD.getLong(), true,
				"Password to bind with defaults to \"\")");
		DEFAULT_OPTIONS.addOption(Flag.OBJECTCLASS.getShort(), Flag.OBJECTCLASS.getLong(), true,
				"Object class name or ? for all. Print object class schema");
		DEFAULT_OPTIONS.addOption(Flag.ATTRIBUTE.getShort(), Flag.ATTRIBUTE.getLong(), true,
				"Attribute name or ? for all. Print attribute schema");
		DEFAULT_OPTIONS.addOption(Flag.SYNTAX.getShort(), Flag.SYNTAX.getLong(), true,
				"Syntax OID or ? for all. Print attribute syntax");
		DEFAULT_OPTIONS.addOption(Flag.HELP.getShort(), Flag.HELP.getLong(), false, "Print this help message");
		DEFAULT_OPTIONS.addOption(Flag.ERROR.getShort(), Flag.ERROR.getLong(), false, "Send output to standard error");
	}

	/**
	 * Not to be instantiated.
	 */
	private SchemaViewer() {

	}

	private static void printAttrs(Attributes attrs) throws NamingException {
		NamingEnumeration<? extends Attribute> attrsEnum = attrs.getAll();
		while (attrsEnum.hasMore()) {
			Attribute currentAttr = attrsEnum.next();
			outstream.print(String.format("%1$s:", currentAttr.getID()));
			NamingEnumeration<?> valuesEnum = currentAttr.getAll();
			while (valuesEnum.hasMoreElements()) {
				outstream.print(String.format("%1$s ", valuesEnum.nextElement().toString()));
			}
			outstream.println();
		}
	}

	private static void printObject(String contextName, String schemaName, DirContext schemaContext)
			throws NameNotFoundException, NamingException {

		DirContext oContext = (DirContext) schemaContext.lookup(contextName + "/" + schemaName);

		outstream.println("NAME:" + schemaName);
		printAttrs(oContext.getAttributes(""));
	}

	private static void printSchema(String contextName, DirContext schemaContext)
			throws NameNotFoundException, NamingException {

		outstream.println();

		NamingEnumeration<NameClassPair> schemaList = schemaContext.list(contextName);

		while (schemaList.hasMore()) {
			NameClassPair ncp = schemaList.nextElement();

			printObject(contextName, ncp.getName(), schemaContext);
			outstream.println();
		}

		outstream.println();
	}

	private static void print(String optionValue, String contextName, DirContext schemaContext)
			throws NameNotFoundException, NamingException {

		if (optionValue.equals(WILDCARD)) {
			printSchema(contextName, schemaContext);
		}
		else {
			printObject(contextName, optionValue, schemaContext);
		}
	}

	private static PrintStream outstream = System.out;

	private final static String WILDCARD = "?";

	public static void main(String[] argv) {
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(DEFAULT_OPTIONS, argv);
		}
		catch (ParseException ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}

		if (cmd.hasOption(Flag.HELP.getShort())) {
			HelpFormatter formatter = new HelpFormatter();

			formatter.printHelp(120, SchemaViewer.class.getSimpleName(), null, DEFAULT_OPTIONS, null, true);
			System.exit(0);
		}

		if (cmd.hasOption(Flag.ERROR.getShort())) {
			outstream = System.err;
		}

		String url = cmd.getOptionValue(Flag.URL.getShort(), DEFAULT_URL);
		String user = cmd.getOptionValue(Flag.USERNAME.getShort(), "");
		String pass = cmd.getOptionValue(Flag.PASSWORD.getShort(), "");

		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		if (user != null) {
			env.put(Context.SECURITY_PRINCIPAL, user);
		}
		if (pass != null) {
			env.put(Context.SECURITY_CREDENTIALS, pass);
			if (user == null) {
				System.err.println("You must specify a user if you specify a password");
				System.exit(1);
			}
		}

		try {
			DirContext context = new InitialDirContext(env);
			DirContext schemaContext = context.getSchema("");

			if (cmd.hasOption(Flag.OBJECTCLASS.getShort())) {
				print(cmd.getOptionValue(Flag.OBJECTCLASS.getShort()), SchemaContext.OBJECTCLASS.getValue(),
						schemaContext);
			}

			if (cmd.hasOption(Flag.ATTRIBUTE.getShort())) {
				print(cmd.getOptionValue(Flag.ATTRIBUTE.getShort()), SchemaContext.ATTRIBUTE.getValue(), schemaContext);
			}

			if (cmd.hasOption(Flag.SYNTAX.getShort())) {
				print(cmd.getOptionValue(Flag.SYNTAX.getShort()), SchemaContext.SYNTAX.getValue(), schemaContext);
			}

		}
		catch (AuthenticationException ex) {
			System.err.println(String.format("Failed to bind to ldap server at %1$s", url));
		}
		catch (CommunicationException ex) {
			System.err.println(String.format("Failed to contact ldap server at %1$s", url));
		}
		catch (NameNotFoundException ex) {
			System.err.println(String.format("Can't find object %1$s", ex.getMessage()));
		}
		catch (NamingException ex) {
			System.err.println(ex.toString());
		}
	}

}
