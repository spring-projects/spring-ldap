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

import java.util.function.Consumer;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.sdk.Entry;

/**
 * Helper class for embedded Unboundid ldap server.
 *
 * @author Emanuel Trandafir
 * @since 3.3
 */
public class EmbeddedLdapServerBuilder {

	private int port = 0;

	private Consumer<InMemoryDirectoryServerConfig> configurationCustomizer = (__) -> {
	};

	private String partitionSuffix = "dc=jayway,dc=se";

	private String partitionName = "jayway";

	EmbeddedLdapServerBuilder() {
	}

	public EmbeddedLdapServerBuilder withPort(int port) {
		this.port = port;
		return this;
	}

	public EmbeddedLdapServerBuilder withConfigurationCustomizer(
			Consumer<InMemoryDirectoryServerConfig> configurationCustomizer) {
		this.configurationCustomizer = configurationCustomizer;
		return this;
	}

	public EmbeddedLdapServerBuilder withPartitionSuffix(String defaultPartitionSuffix) {
		this.partitionSuffix = defaultPartitionSuffix;
		return this;
	}

	public EmbeddedLdapServerBuilder withPartitionName(String defaultPartitionName) {
		this.partitionName = defaultPartitionName;
		return this;
	}

	/**
	 * Builds and returns a {@link EmbeddedLdapServer}.
	 * <p>
	 * In order to start the server, you should call {@link EmbeddedLdapServer#start()}.
	 * @return a new {@link EmbeddedLdapServer}.
	 */
	public EmbeddedLdapServer build() {
		try {
			InMemoryDirectoryServerConfig config = EmbeddedLdapServer.inMemoryDirectoryServerConfig(this.partitionSuffix, this.port);
			this.configurationCustomizer.accept(config);

			Entry entry = EmbeddedLdapServer.ldapEntry(this.partitionName, this.partitionSuffix);
			InMemoryDirectoryServer directoryServer = EmbeddedLdapServer.inMemoryDirectoryServer(config, entry);
			return new EmbeddedLdapServer(directoryServer);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
