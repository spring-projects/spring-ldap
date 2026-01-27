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

package org.springframework.ldap.test;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.name.LdapDN;

/**
 * Helper class for embedded Apache Directory Server.
 *
 * @author Mattias Hellborg Arthursson
 * @since 1.3.2
 */
public final class EmbeddedLdapServer {

	private final DirectoryService directoryService;

	private final LdapServer ldapServer;

	private static File workingDirectory = new File(System.getProperty("java.io.tmpdir") + "/apacheds-test1");

	private EmbeddedLdapServer(DirectoryService directoryService, LdapServer ldapServer) {
		this.directoryService = directoryService;
		this.ldapServer = ldapServer;
	}

	public static EmbeddedLdapServer newEmbeddedServer(String defaultPartitionName, String defaultPartitionSuffix,
			int port) throws Exception {
		FileUtils.deleteDirectory(workingDirectory);

		DefaultDirectoryService directoryService = new DefaultDirectoryService();
		directoryService.setShutdownHookEnabled(true);
		directoryService.setAllowAnonymousAccess(true);

		directoryService.setWorkingDirectory(workingDirectory);
		directoryService.getChangeLog().setEnabled(false);

		JdbmPartition partition = new JdbmPartition();
		partition.setId(defaultPartitionName);
		partition.setSuffix(defaultPartitionSuffix);
		directoryService.addPartition(partition);

		directoryService.startup();

		// Inject the apache root entry if it does not already exist
		if (!directoryService.getAdminSession().exists(partition.getSuffixDn())) {
			ServerEntry entry = directoryService.newEntry(new LdapDN(defaultPartitionSuffix));
			entry.add("objectClass", "top", "domain", "extensibleObject");
			entry.add("dc", defaultPartitionName);
			directoryService.getAdminSession().add(entry);
		}

		LdapServer ldapServer = new LdapServer();
		ldapServer.setDirectoryService(directoryService);

		TcpTransport ldapTransport = new TcpTransport(port);
		ldapServer.setTransports(ldapTransport);
		ldapServer.start();

		return new EmbeddedLdapServer(directoryService, ldapServer);
	}

	public void shutdown() throws Exception {
		this.ldapServer.stop();
		this.directoryService.shutdown();

		FileUtils.deleteDirectory(workingDirectory);
	}

}
