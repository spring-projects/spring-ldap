/*
 * Copyright 2005-2015 the original author or authors.
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

import org.apache.commons.io.FileUtils;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.extractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.loader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.api.util.exception.Exceptions;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Helper class for embedded Apache Directory Server V2.
 *
 * @author Eddu Melendez
 * @since 2.1.0
 */
public final class EmbeddedLdapServer {

	private final static Logger LOGGER = LoggerFactory.getLogger(EmbeddedLdapServer.class);

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

		DirectoryService directoryService = new DefaultDirectoryService();
		InstanceLayout instanceLayout = new InstanceLayout(workingDirectory);
		directoryService.setInstanceLayout(instanceLayout);

		directoryService.setShutdownHookEnabled(true);
		directoryService.setAllowAnonymousAccess(true);

		CacheService cacheService = new CacheService();
		cacheService.initialize(directoryService.getInstanceLayout());

		directoryService.setCacheService(cacheService);

		File schemaPartitionDirectory = new File(instanceLayout.getPartitionsDirectory(), "schema");

		if (schemaPartitionDirectory.exists()) {
			LOGGER.info("schema partition already exists, skipping schema extraction");
		} else {
			SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor( instanceLayout.getPartitionsDirectory() );
			extractor.extractOrCopy();
		}

		SchemaLoader loader = new LdifSchemaLoader(schemaPartitionDirectory);
		SchemaManager schemaManager = new DefaultSchemaManager(loader);

		schemaManager.loadAllEnabled();

		List<Throwable> errors = schemaManager.getErrors();

		if (errors.size() != 0) {
			throw new Exception(I18n.err(I18n.ERR_317, Exceptions.printErrors(errors)));
		}

		directoryService.setSchemaManager(schemaManager);

		LdifPartition schemaLdifPartition = new LdifPartition(schemaManager, directoryService.getDnFactory());
		schemaLdifPartition.setPartitionPath(schemaPartitionDirectory.toURI());

		SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
		schemaPartition.setWrappedPartition(schemaLdifPartition);
		directoryService.setSchemaPartition(schemaPartition);

		JdbmPartition partition = new JdbmPartition(directoryService.getSchemaManager(), directoryService.getDnFactory());
		partition.setId("system");
		partition.setPartitionPath(new File(directoryService.getInstanceLayout().getPartitionsDirectory(), partition.getId() ).toURI());
		partition.setSuffixDn(new Dn(ServerDNConstants.SYSTEM_DN));
		partition.setSchemaManager(directoryService.getSchemaManager());

		directoryService.setSystemPartition(partition);

		directoryService.getChangeLog().setEnabled(false);
		directoryService.setDenormalizeOpAttrsEnabled(true);

		Partition customPartition = addPartition(defaultPartitionName, defaultPartitionSuffix, directoryService);

		directoryService.startup();

		// Inject the apache root entry if it does not already exist
		if (!directoryService.getAdminSession().exists(customPartition.getSuffixDn())) {
			Entry entry = directoryService.newEntry(new Dn(defaultPartitionSuffix));
			entry.add("objectClass", "top", "domain", "extensibleObject");
			entry.add("dc", defaultPartitionName);
			directoryService.getAdminSession().add(entry);
		}

		LdapServer ldapServer = new LdapServer();
		ldapServer.setDirectoryService(directoryService);

		ldapServer.setTransports(new TcpTransport(port));
		ldapServer.start();

		return new EmbeddedLdapServer(directoryService, ldapServer);
	}

	private static Partition addPartition(String partitionId, String partitionDn, DirectoryService directoryService) throws Exception {
		JdbmPartition partition = new JdbmPartition(directoryService.getSchemaManager(), directoryService.getDnFactory());
		partition.setId(partitionId);
		partition.setPartitionPath(new File(directoryService.getInstanceLayout().getPartitionsDirectory(), partitionId ).toURI());
		partition.setSuffixDn(new Dn( partitionDn ));
		directoryService.addPartition(partition);

		return partition;
	}

	public void shutdown() throws Exception {
		this.ldapServer.stop();
		this.directoryService.shutdown();

		FileUtils.deleteDirectory(workingDirectory);
	}
}
