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

package org.springframework.ldap.test.unboundid;

import java.util.List;
import java.util.function.Consumer;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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
	 * Creates a new {@link Builder} with a given partition suffix.
	 *
	 * @since 3.3
	 */
	public static Builder withPartitionSuffix(String partitionSuffix) {
		return new Builder(partitionSuffix);
	}

	/**
	 * Creates and starts new embedded LDAP server.
	 * @deprecated Use the builder pattern exposed via
	 * {@link #withPartitionSuffix(String)} instead.
	 */
	@Deprecated(since = "3.3")
	public static EmbeddedLdapServer newEmbeddedServer(String defaultPartitionName, String defaultPartitionSuffix,
			int port) throws Exception {
		EmbeddedLdapServer server = EmbeddedLdapServer.withPartitionSuffix(defaultPartitionSuffix)
			.partitionName(defaultPartitionName)
			.port(port)
			.build();

		server.start();
		return server;
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

	/**
	 * Helper class for embedded Unboundid ldap server.
	 *
	 * @author Emanuel Trandafir
	 * @since 3.3
	 */
	public static final class Builder {

		private final String partitionSuffix;

		private String partitionName;

		private int port = 0;

		private Consumer<InMemoryDirectoryServerConfig> configurationCustomizer = (__) -> {
		};

		private Builder(String partitionSuffix) {
			this.partitionSuffix = partitionSuffix;
			this.partitionName = leftMostElement(partitionSuffix);
		}

		/**
		 * Sets the port for the embedded LDAP server.
		 * @param port the port for the embedded LDAP server. Defaults to 0 in which case
		 * the server should automatically choose an available port.
		 * @return this {@link Builder} instance.
		 */
		public Builder port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Sets a customizer for the {@link InMemoryDirectoryServerConfig}.
		 * @param configurationCustomizer a {@link Consumer} function that will be applied
		 * to the {@link InMemoryDirectoryServerConfig} before creating the
		 * {@link InMemoryDirectoryServer}. The default values, it a Consumer function
		 * that does nothing: (config) -> {}
		 * @return this {@link Builder} instance.
		 */
		public Builder configurationCustomizer(Consumer<InMemoryDirectoryServerConfig> configurationCustomizer) {
			this.configurationCustomizer = configurationCustomizer;
			return this;
		}

		/**
		 * Sets the partition name for the embedded LDAP server.
		 * @param partitionName the partition name for the embedded LDAP server. Defaults
		 * to the left most element of the partition suffix.
		 * @return this {@link Builder} instance.
		 */
		public Builder partitionName(String partitionName) {
			this.partitionName = partitionName;
			return this;
		}

		/**
		 * Builds and returns a {@link EmbeddedLdapServer}.
		 * <p>
		 * In order to start the server, you should call
		 * {@link EmbeddedLdapServer#start()}.
		 * @return a new {@link EmbeddedLdapServer}.
		 */
		public EmbeddedLdapServer build() {
			try {
				InMemoryDirectoryServerConfig config = inMemoryDirectoryServerConfig(this.partitionSuffix, this.port);
				this.configurationCustomizer.accept(config);

				Entry entry = ldapEntry(this.partitionName, this.partitionSuffix);
				InMemoryDirectoryServer directoryServer = inMemoryDirectoryServer(config, entry);
				return new EmbeddedLdapServer(directoryServer);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		static String leftMostElement(String partitionSuffix) {
			try {
				List<Rdn> rdns = new LdapName(partitionSuffix).getRdns();
				Rdn leftMost = CollectionUtils.lastElement(rdns);
				Assert.notNull(leftMost, "partitionSuffix [" + partitionSuffix + "] produced an empty list of rdns");
				return leftMost.getValue().toString();
			}
			catch (InvalidNameException ex) {
				throw new RuntimeException(ex);
			}
		}

		private static InMemoryDirectoryServerConfig inMemoryDirectoryServerConfig(String partitionSuffix, int port)
				throws LDAPException {
			InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(partitionSuffix);
			config.addAdditionalBindCredentials("uid=admin,ou=system", "secret");
			config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", port));
			config.setEnforceSingleStructuralObjectClass(false);
			config.setEnforceAttributeSyntaxCompliance(true);
			return config;
		}

		private static Entry ldapEntry(String defaultPartitionName, String defaultPartitionSuffix)
				throws LDAPException {
			Entry entry = new Entry(new DN(defaultPartitionSuffix));
			entry.addAttribute("objectClass", "top", "domain", "extensibleObject");
			entry.addAttribute("dc", defaultPartitionName);
			return entry;
		}

		private static InMemoryDirectoryServer inMemoryDirectoryServer(InMemoryDirectoryServerConfig config,
				Entry entry) throws LDAPException {
			InMemoryDirectoryServer directoryServer = new InMemoryDirectoryServer(config);
			directoryServer.add(entry);
			return directoryServer;
		}

	}

}
