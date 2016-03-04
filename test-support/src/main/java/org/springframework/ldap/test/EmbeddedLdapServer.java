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

package org.springframework.ldap.test;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.core.schema.SchemaPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.name.DN;
import org.apache.directory.shared.ldap.schema.SchemaManager;
import org.apache.directory.shared.ldap.schema.ldif.extractor.SchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.ldif.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.loader.ldif.LdifSchemaLoader;
import org.apache.directory.shared.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.shared.ldap.schema.registries.SchemaLoader;

import java.io.File;
import java.util.List;

/**
 * Helper class for embedded Apache Directory Server.
 *
 * @author Mattias Hellborg Arthursson
 * @author Eddu Melendez
 * @since 1.3.2
 */
public final class EmbeddedLdapServer {
    private final DirectoryService directoryService;
    private final LdapServer ldapServer;
    private static File workingDirectory;

    private EmbeddedLdapServer(DirectoryService directoryService,
                               LdapServer ldapServer) {
        this.directoryService = directoryService;
        this.ldapServer = ldapServer;
    }

    public static EmbeddedLdapServer newEmbeddedServer(String defaultPartitionName, String defaultPartitionSuffix, int port)
            throws Exception{
        workingDirectory = new File(System.getProperty("java.io.tmpdir") + "/apacheds-test1");
        FileUtils.deleteDirectory(workingDirectory);

        DefaultDirectoryService directoryService = new DefaultDirectoryService();
        directoryService.setShutdownHookEnabled(true);
        directoryService.setAllowAnonymousAccess(true);

        directoryService.setWorkingDirectory(workingDirectory);

	    initSchemaPartition(directoryService);

        directoryService.getChangeLog().setEnabled(false);
	    directoryService.setDenormalizeOpAttrsEnabled(true);

	    Partition systemPartition = createPartition("system", ServerDNConstants.SYSTEM_DN);
	    directoryService.addPartition(systemPartition);
	    directoryService.setSystemPartition(systemPartition);

	    Partition partition = createPartition(defaultPartitionName, defaultPartitionSuffix);
        directoryService.addPartition(partition);

        directoryService.startup();

        // Inject the apache root entry if it does not already exist
        if ( !directoryService.getAdminSession().exists( partition.getSuffixDn() ) )
        {
            ServerEntry entry = directoryService.newEntry(new DN(defaultPartitionSuffix));
            entry.add("objectClass", "top", "domain", "extensibleObject");
            entry.add("dc", defaultPartitionName);
            directoryService.getAdminSession().add( entry );
        }

        LdapServer ldapServer = new LdapServer();
        ldapServer.setDirectoryService(directoryService);

        TcpTransport ldapTransport = new TcpTransport(port);
        ldapServer.setTransports( ldapTransport );
        ldapServer.start();

        return new EmbeddedLdapServer(directoryService, ldapServer);
    }

    public void shutdown() throws Exception {
        ldapServer.stop();
        directoryService.shutdown();
    }

	private static void initSchemaPartition(DefaultDirectoryService directoryService)
			throws Exception {
		SchemaPartition schemaPartition = directoryService.getSchemaService()
				.getSchemaPartition();

		// Init the LdifPartition
		LdifPartition ldifPartition = new LdifPartition();
		String workingDirectory = directoryService.getWorkingDirectory().getPath();
		ldifPartition.setWorkingDirectory(workingDirectory + "/schema");

		// Extract the schema on disk (a brand new one) and load the registries
		File schemaRepository = new File(workingDirectory, "schema");
		SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(
				new File(workingDirectory));
		extractor.extractOrCopy(true);

		schemaPartition.setWrappedPartition(ldifPartition);

		SchemaLoader loader = new LdifSchemaLoader(schemaRepository);
		SchemaManager schemaManager = new DefaultSchemaManager(loader);
		directoryService.setSchemaManager(schemaManager);

		// We have to load the schema now, otherwise we won't be able
		// to initialize the Partitions, as we won't be able to parse
		// and normalize their suffix DN
		schemaManager.loadAllEnabled();

		schemaPartition.setSchemaManager(schemaManager);

		List<Throwable> errors = schemaManager.getErrors();

		if (errors.size() != 0) {
			throw new Exception("Schema load failed : " + errors);
		}
	}

	private static Partition createPartition(String partitionId, String partitionSuffix)
			throws LdapInvalidDnException {
		JdbmPartition partition = new JdbmPartition();
		partition.setId(partitionId);
		partition.setPartitionDir(new File(workingDirectory, partitionId));
		partition.setSuffix(partitionSuffix);
		return partition;
	}
}
