/*
 * Copyright 2005-2016 the original author or authors.
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

package org.springframework.ldap.test.unboundid;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;

import org.springframework.util.Assert;

/**
 * Helper class for embedded Unboundid ldap server.
 *
 * @author Eddu Melendez
 * @since 2.1.0
 */
public final class EmbeddedLdapServer implements AutoCloseable {

	private final InMemoryDirectoryServer directoryServer;

	/**
	 * Construct an {@link EmbeddedLdapServer} using the provided
	 * {@link InMemoryDirectoryServer}.
	 *
	 * @since 3.3
	 */
	public EmbeddedLdapServer(InMemoryDirectoryServer directoryServer) {
		this.directoryServer = directoryServer;
	}

	/**
	 * Creates and starts new embedded LDAP server.
	 */
	public static EmbeddedLdapServer newEmbeddedServer(String defaultPartitionName, String defaultPartitionSuffix,
			int port) throws Exception {
		InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(defaultPartitionSuffix);
		config.addAdditionalBindCredentials("uid=admin,ou=system", "secret");

		config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", port));

		config.setEnforceSingleStructuralObjectClass(false);
		config.setEnforceAttributeSyntaxCompliance(true);

		Entry entry = new Entry(new DN(defaultPartitionSuffix));
		entry.addAttribute("objectClass", "top", "domain", "extensibleObject");
		entry.addAttribute("dc", defaultPartitionName);

		InMemoryDirectoryServer directoryServer = new InMemoryDirectoryServer(config);
		directoryServer.add(entry);
		directoryServer.startListening();
		return new EmbeddedLdapServer(directoryServer);
	}

	/**
	 * Starts the embedded LDAP server.
	 *
	 * @since 3.3
	 */
	public void start() {
		Assert.isTrue(this.directoryServer.getListenPort() == -1, "The server has already been started.");
		try {
			this.directoryServer.startListening();
		}
		catch (LDAPException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Closes the embedded LDAP server and releases resource, closing existing
	 * connections.
	 *
	 * @since 3.3
	 */
	@Override
	public void close() {
		this.directoryServer.shutDown(true);
	}

	/**
	 * @deprecated Use {@link #close()} instead.
	 */
	@Deprecated(since = "3.3")
	public void shutdown() {
		this.directoryServer.shutDown(true);
	}

}
