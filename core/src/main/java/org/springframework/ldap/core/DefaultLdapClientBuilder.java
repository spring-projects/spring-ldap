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

package org.springframework.ldap.core;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.NamingException;

class DefaultLdapClientBuilder implements LdapClient.Builder {

	private ContextSource contextSource;

	private Supplier<SearchControls> searchControlsSupplier = () -> {
		SearchControls controls = new SearchControls();
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		controls.setCountLimit(0);
		controls.setTimeLimit(0);
		return controls;
	};

	private boolean ignorePartialResultException = false;

	private boolean ignoreNameNotFoundException = false;

	private boolean ignoreSizeLimitExceededException = true;

	@Deprecated
	DefaultLdapClientBuilder() {
		this(new NullContextSource());
	}

	DefaultLdapClientBuilder(ContextSource contextSource) {
		this.contextSource = contextSource;
	}

	DefaultLdapClientBuilder(ContextSource contextSource, Supplier<SearchControls> searchControlsSupplier) {
		this.contextSource = contextSource;
		this.searchControlsSupplier = searchControlsSupplier;
	}

	DefaultLdapClientBuilder(LdapTemplate ldap) {
		this.contextSource = ldap.getContextSource();
		this.searchControlsSupplier = () -> {
			SearchControls controls = new SearchControls();
			controls.setSearchScope(ldap.getDefaultSearchScope());
			controls.setCountLimit(ldap.getDefaultCountLimit());
			controls.setTimeLimit(ldap.getDefaultTimeLimit());
			return controls;
		};
		this.ignoreNameNotFoundException = ldap.isIgnoreNameNotFoundException();
		this.ignoreSizeLimitExceededException = ldap.isIgnoreSizeLimitExceededException();
		this.ignorePartialResultException = ldap.isIgnorePartialResultException();
	}

	@Override
	public DefaultLdapClientBuilder contextSource(ContextSource contextSource) {
		this.contextSource = contextSource;
		return this;
	}

	@Override
	public DefaultLdapClientBuilder defaultSearchControls(Supplier<SearchControls> searchControlsSupplier) {
		this.searchControlsSupplier = searchControlsSupplier;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefaultLdapClientBuilder ignorePartialResultException(boolean ignore) {
		this.ignorePartialResultException = ignore;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefaultLdapClientBuilder ignoreNameNotFoundException(boolean ignore) {
		this.ignoreNameNotFoundException = ignore;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefaultLdapClientBuilder ignoreSizeLimitExceededException(boolean ignore) {
		this.ignoreSizeLimitExceededException = ignore;
		return this;
	}

	@Override
	public DefaultLdapClientBuilder apply(Consumer<LdapClient.Builder> builderConsumer) {
		builderConsumer.accept(this);
		return this;
	}

	@Override
	public DefaultLdapClientBuilder clone() {
		return new DefaultLdapClientBuilder(this.contextSource, this.searchControlsSupplier);
	}

	@Override
	public LdapClient build() {
		DefaultLdapClient client = new DefaultLdapClient(this.contextSource, this.searchControlsSupplier, this);
		client.setIgnorePartialResultException(this.ignorePartialResultException);
		client.setIgnoreSizeLimitExceededException(this.ignoreSizeLimitExceededException);
		client.setIgnoreNameNotFoundException(this.ignoreNameNotFoundException);
		return client;
	}

	private static final class NullContextSource implements ContextSource {

		@Override
		public DirContext getReadOnlyContext() throws NamingException {
			throw new IllegalStateException("Property 'contextSource' must be set.");
		}

		@Override
		public DirContext getReadWriteContext() throws NamingException {
			throw new IllegalStateException("Property 'contextSource' must be set.");
		}

		@Override
		public DirContext getContext(String principal, String credentials) throws NamingException {
			throw new IllegalStateException("Property 'contextSource' must be set.");
		}

	}

}
