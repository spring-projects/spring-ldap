package org.springframework.ldap.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.ContextNotEmptyException;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.io.IOUtils;
import org.apache.directory.server.configuration.MutableServerStartupConfiguration;
import org.apache.directory.server.core.configuration.ShutdownConfiguration;
import org.apache.directory.server.core.partition.impl.btree.MutableBTreePartitionConfiguration;
import org.apache.directory.server.jndi.ServerContextFactory;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;

public class LdapTestUtils {

	/**
	 * Not to be instantiated.
	 */
	private LdapTestUtils() {
	}

	public static DirContext startApacheDirectoryServer(int port,
			String defaultPartitionSuffix, String defaultPartitionName)
			throws NamingException {

		MutableServerStartupConfiguration cfg = new MutableServerStartupConfiguration();

		// Determine an appropriate working directory
		String tempDir = System.getProperty("java.io.tmpdir");
		cfg.setWorkingDirectory(new File(tempDir));

		cfg.setLdapPort(port);

		MutableBTreePartitionConfiguration partitionConfiguration = new MutableBTreePartitionConfiguration();
		partitionConfiguration.setSuffix(defaultPartitionSuffix);
		partitionConfiguration
				.setContextEntry(getRootPartitionAttributes(defaultPartitionName));
		partitionConfiguration.setName(defaultPartitionName);

		cfg.setPartitionConfigurations(Collections
				.singleton(partitionConfiguration));
		// Start the Server

		Hashtable env = createEnv();
		env.putAll(cfg.toJndiEnvironment());
		return new InitialDirContext(env);
	}

	public static void destroyApacheDirectoryServer(String principal,
			String credentials) throws Exception {
		Properties env = new Properties();
		env.setProperty(Context.INITIAL_CONTEXT_FACTORY,
				ServerContextFactory.class.getName());
		env.setProperty(Context.SECURITY_AUTHENTICATION, "simple");
		env.setProperty(Context.SECURITY_PRINCIPAL, principal);
		env.setProperty(Context.SECURITY_CREDENTIALS, credentials);

		ShutdownConfiguration configuration = new ShutdownConfiguration();
		env.putAll(configuration.toJndiEnvironment());

		new InitialContext(env);
	}

	//
	// public void cleanAndSetup(ContextSource contextSource, Resource ldifFile,
	// DistinguishedName baseLdapPath)
	// throws Exception {
	// DirContext ctx = contextSource.getReadWriteContext();
	//
	// // First of all, make sure the database is empty.
	// Name startingPoint = null;
	//
	// // Different test cases have different base paths. This means that the
	// // starting point will be different.
	// if (baseLdapPath.size() != 0) {
	// startingPoint = DistinguishedName.EMPTY_PATH;
	// } else {
	// startingPoint = new DistinguishedName(baseLdapPath);
	// }
	//
	// try {
	// clearSubContexts(ctx, startingPoint);
	// // Load the ldif to the recently started server
	// LdifFileLoader loader = new LdifFileLoader(ctx, ldifFile);
	// loader.execute();
	// } finally {
	// ctx.close();
	// }
	// }
	//

	public static void clearSubContexts(ContextSource contextSource, Name name)
			throws NamingException {
		DirContext ctx = null;
		try {
			ctx = contextSource.getReadWriteContext();
			clearSubContexts(ctx, name);
		} finally {
			ctx.close();
		}
	}

	public static void clearSubContexts(DirContext ctx, Name name)
			throws NamingException {

		NamingEnumeration enumeration = null;
		try {
			enumeration = ctx.listBindings(name);
			while (enumeration.hasMore()) {
				Binding element = (Binding) enumeration.next();
				DistinguishedName childName = new DistinguishedName(element
						.getName());
				childName.prepend((DistinguishedName) name);

				try {
					ctx.destroySubcontext(childName);
				} catch (ContextNotEmptyException e) {
					clearSubContexts(ctx, childName);
					ctx.destroySubcontext(childName);
				}
			}
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			try {
				enumeration.close();
			} catch (Exception e) {
				// Never mind this
			}
		}
	}

	public static void loadLdif(ContextSource contextSource, Resource ldifFile)
			throws IOException {
		DirContext context = contextSource.getReadWriteContext();
		try {
			loadLdif(context, ldifFile);
		} finally {
			try {
				context.close();
			} catch (Exception e) {
				// This is not the exception we are interested in.
			}
		}
	}

	private static void loadLdif(DirContext context, Resource ldifFile)
			throws IOException {
		File tempFile = File.createTempFile("spring_ldap_test", ".ldif");
		InputStream inputStream = ldifFile.getInputStream();

		IOUtils.copy(inputStream, new FileOutputStream(tempFile));
		LdifFileLoader fileLoader = new LdifFileLoader(context, tempFile
				.getAbsolutePath());
		fileLoader.execute();
	}

	private static Hashtable createEnv() {
		Hashtable env = new Properties();

		env.put(Context.PROVIDER_URL, "");
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.apache.directory.server.jndi.ServerContextFactory");

		env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
		env.put(Context.SECURITY_CREDENTIALS, "secret");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");

		return env;
	}

	private static Attributes getRootPartitionAttributes(
			String defaultPartitionName) {
		BasicAttributes attributes = new BasicAttributes();
		BasicAttribute objectClassAttribute = new BasicAttribute("objectClass");
		objectClassAttribute.add("top");
		objectClassAttribute.add("domain");
		objectClassAttribute.add("extensibleObject");
		attributes.put(objectClassAttribute);
		attributes.put("dc", defaultPartitionName);

		return attributes;
	}

}
