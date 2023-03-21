package org.springframework.ldap.core;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.naming.directory.SearchControls;

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

	DefaultLdapClientBuilder() {
	}

	DefaultLdapClientBuilder(ContextSource contextSource, Supplier<SearchControls> searchControlsSupplier) {
		this.contextSource = contextSource;
		this.searchControlsSupplier = searchControlsSupplier;
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
		DefaultLdapClient client = new DefaultLdapClient(this.contextSource, this.searchControlsSupplier);
		client.setIgnorePartialResultException(this.ignorePartialResultException);
		client.setIgnoreSizeLimitExceededException(this.ignoreSizeLimitExceededException);
		client.setIgnoreNameNotFoundException(this.ignoreNameNotFoundException);
		return client;
	}

}
