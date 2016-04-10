/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.test.apacheds2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.ContextNotEmptyException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;

import org.apache.commons.io.IOUtils;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.ldif.parser.LdifParser;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.DummyDirContext;

/**
 * Utilities for starting, stopping and populating an in-process Apache
 * Directory Server to use for integration testing purposes.
 *
 * @author Mattias Hellborg Arthursson
 */
public final class LdapTestUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(LdapTestUtils.class);

    private static EmbeddedLdapServer embeddedServer;

    /**
     * Not to be instantiated.
     */
    private LdapTestUtils() {
    }


    /**
     * Start an in-process Apache Directory Server.
     *
     * @param port                   the port on which the server will be listening.
     * @param defaultPartitionSuffix The default base suffix that will be used
     *                               for the LDAP server.
     * @param defaultPartitionName   The name to use in the directory server
     *                               configuration for the default base suffix.
     * @param principal              The principal to use when starting the directory server.
     * @param credentials            The credentials to use when starting the directory
     *                               server.
     * @param extraSchemas           Set of extra schemas to add to the bootstrap schemas
     *                               of ApacheDS. May be <code>null</code>.
     * @return An unusable DirContext instance.
     * @throws NamingException If anything goes wrong when starting the server.
     * @deprecated use {@link #startEmbeddedServer(int, String, String)} instead.
     */
    public static DirContext startApacheDirectoryServer(int port, String defaultPartitionSuffix,
                                                        String defaultPartitionName, String principal, String credentials, Set extraSchemas) throws NamingException {

        startEmbeddedServer(port, defaultPartitionSuffix, defaultPartitionName);
        return new DummyDirContext();
    }

    /**
     * Start an embedded Apache Directory Server. Only one embedded server will be permitted in the same JVM.
     *
     * @param port                   the port on which the server will be listening.
     * @param defaultPartitionSuffix The default base suffix that will be used
     *                               for the LDAP server.
     * @param defaultPartitionName   The name to use in the directory server
     *                               configuration for the default base suffix.
     *
     * @throws IllegalStateException if an embedded server is already started.
     * @since 1.3.2
     */
    public static void startEmbeddedServer(int port, String defaultPartitionSuffix, String defaultPartitionName) {
        if(embeddedServer != null) {
            throw new IllegalStateException("An embedded server is already started");
        }

        try {
            embeddedServer = EmbeddedLdapServer.newEmbeddedServer(defaultPartitionName, defaultPartitionSuffix, port);
        } catch (Exception e) {
            throw new UncategorizedLdapException("Failed to start embedded server", e);
        }
    }

    /**
     * Shuts down the embedded server, if there is one. If no server was previously started in this JVM
     * this is silently ignored.
     *
     * @throws Exception
     * @since 1.3.2
     */
    public static void shutdownEmbeddedServer() throws Exception {
        if(embeddedServer != null) {
            embeddedServer.shutdown();
            embeddedServer = null;
        }
    }

    /**
     * Shut down the in-process Apache Directory Server.
     *
     * @param principal   the principal to be used for authentication.
     * @param credentials the credentials to be used for authentication.
     * @throws Exception If anything goes wrong when shutting down the server.
     * @deprecated use {@link #shutdownEmbeddedServer()} instead.
     */
    public static void destroyApacheDirectoryServer(String principal, String credentials) throws Exception {
        shutdownEmbeddedServer();
    }

    /**
     * Clear the directory sub-tree starting with the node represented by the
     * supplied distinguished name.
     *
     * @param contextSource the ContextSource to use for getting a DirContext.
     * @param name          the distinguished name of the root node.
     * @throws NamingException if anything goes wrong removing the sub-tree.
     */
    public static void clearSubContexts(ContextSource contextSource, Name name) throws NamingException {
        DirContext ctx = null;
        try {
            ctx = contextSource.getReadWriteContext();
            clearSubContexts(ctx, name);
        } finally {
            try {
                ctx.close();
            } catch (Exception e) {
                // Never mind this
            }
        }
    }

    /**
     * Clear the directory sub-tree starting with the node represented by the
     * supplied distinguished name.
     *
     * @param ctx  The DirContext to use for cleaning the tree.
     * @param name the distinguished name of the root node.
     * @throws NamingException if anything goes wrong removing the sub-tree.
     */
    public static void clearSubContexts(DirContext ctx, Name name) throws NamingException {

        NamingEnumeration enumeration = null;
        try {
            enumeration = ctx.listBindings(name);
            while (enumeration.hasMore()) {
                Binding element = (Binding) enumeration.next();
                Name childName = LdapUtils.newLdapName(element.getName());
                childName = LdapUtils.prepend(childName, name);

                try {
                    ctx.unbind(childName);
                } catch (ContextNotEmptyException e) {
                    clearSubContexts(ctx, childName);
                    ctx.unbind(childName);
                }
            }
        } catch (NamingException e) {
            LOGGER.debug("Error cleaning sub-contexts", e);
        } finally {
            try {
                enumeration.close();
            } catch (Exception e) {
                // Never mind this
            }
        }
    }

    /**
     * Load an Ldif file into an LDAP server.
     *
     * @param contextSource ContextSource to use for getting a DirContext to
     *                      interact with the LDAP server.
     * @param ldifFile      a Resource representing a valid LDIF file.
     * @throws IOException if the Resource cannot be read.
     */
    public static void loadLdif(ContextSource contextSource, Resource ldifFile) throws IOException {
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

    public static void cleanAndSetup(ContextSource contextSource, Name rootNode, Resource ldifFile)
            throws NamingException, IOException {

        clearSubContexts(contextSource, rootNode);
        loadLdif(contextSource, ldifFile);
    }

    private static void loadLdif(DirContext context, Resource ldifFile) throws IOException {
        loadLdif(context, LdapUtils.emptyLdapName(), ldifFile);
    }

    private static void loadLdif(DirContext context, Name rootNode, Resource ldifFile) {
        try {
            LdapName baseDn = (LdapName)
                    context.getEnvironment().get(DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY);

            LdifParser parser = new LdifParser(ldifFile);
            parser.open();
            while (parser.hasMoreRecords()) {
                LdapAttributes record = parser.getRecord();

                LdapName dn = record.getName();

                if(baseDn != null) {
                    dn = LdapUtils.removeFirst(dn, baseDn);
                }

                if(!rootNode.isEmpty()) {
                    dn = LdapUtils.prepend(dn, rootNode);
                }
                context.bind(dn, null, record);
            }
        } catch (Exception e) {
            throw new UncategorizedLdapException("Failed to populate LDIF", e);
        }
    }

    public static void loadLdif(DefaultDirectoryService directoryService, Resource ldifFile) throws IOException {
        File tempFile = File.createTempFile("spring_ldap_test", ".ldif");
        try {
            InputStream inputStream = ldifFile.getInputStream();
            IOUtils.copy(inputStream, new FileOutputStream(tempFile));
            LdifFileLoader fileLoader = new LdifFileLoader(directoryService.getSession(), tempFile.getAbsolutePath());
            fileLoader.execute();
        } finally {
            try {
                tempFile.delete();
            } catch (Exception e) {
                // Ignore this
            }
        }
    }
}
