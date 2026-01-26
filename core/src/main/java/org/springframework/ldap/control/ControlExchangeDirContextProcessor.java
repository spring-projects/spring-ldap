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

package org.springframework.ldap.control;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.ldap.core.DirContextProcessor;

/**
 * A generic {@link DirContextProcessor} implementation for managing LDAP request and
 * response controls through a {@link ControlExchange}.
 * <p>
 * This processor handles the lifecycle of LDAP controls by:
 * <ul>
 * <li>Adding the request control from the exchange to the {@link LdapContext} before
 * operations</li>
 * <li>Extracting matching response controls after operations</li>
 * <li>Updating the exchange with the response control for subsequent operations</li>
 * </ul>
 * <p>
 * For stateful controls that require maintaining connection state across multiple
 * operations (such as paged results or server-side sorting), the same LDAP connection
 * must be reused. Spring LDAP's
 * {@link org.springframework.ldap.core.support.SingleContextSource} and
 * {@link org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy}
 * provide this capability when wired into
 * {@link org.springframework.ldap.core.LdapTemplate} and
 * {@link org.springframework.ldap.core.LdapClient} instances.
 *
 * @param <S> the type of the request {@link Control}
 * @param <T> the type of the response {@link Control}
 * @author Josh Cummings
 * @since 4.1
 * @see ControlExchange
 * @see org.springframework.ldap.core.support.SingleContextSource
 * @see org.springframework.ldap.transaction.compensating.manager.ContextSourceTransactionManager
 * @see org.springframework.ldap.transaction.compensating.manager.TransactionAwareContextSourceProxy
 */
public class ControlExchangeDirContextProcessor<S extends Control, T extends Control> implements DirContextProcessor {

	private final Log log = LogFactory.getLog(getClass());

	private ControlExchange<S, T> exchange;

	private final Consumer<DirContext> noResultResponseControlHandler = (ctx) -> this.log
		.debug("Failed to find " + this.exchange.getRequest().getID() + " response control");

	/**
	 * Construct this {@link DirContextProcessor}, providing the {@link ControlExchange}
	 * to use
	 * @param exchange the {@link ControlExchange} to use for managing request and
	 * response controls
	 */
	public ControlExchangeDirContextProcessor(ControlExchange<S, T> exchange) {
		this.exchange = exchange;
	}

	/**
	 * Adds the request control from the exchange to the {@link LdapContext}, replacing
	 * any existing control with the same ID.
	 * @param ctx the {@link DirContext} to pre-process; must be an instance of
	 * {@link LdapContext}
	 * @throws NamingException if an error occurs while processing
	 * @throws IllegalArgumentException if {@code ctx} is not an {@link LdapContext}
	 */
	@Override
	public void preProcess(DirContext ctx) throws NamingException {
		if (!(ctx instanceof LdapContext ldap)) {
			throw new IllegalArgumentException("ctx must be of type LdapContext");
		}
		Control[] controls = ldap.getRequestControls();
		if (controls == null) {
			ldap.setRequestControls(new Control[] { this.exchange.getRequest() });
			return;
		}
		List<Control> updated = new ArrayList<>();
		for (Control control : controls) {
			if (!this.exchange.getRequest().getID().equals(control.getID())) {
				updated.add(control);
			}
			else {
				if (this.log.isTraceEnabled()) {
					this.log.trace("Replacing pre-existing paged results control with " + this.exchange.getRequest());
				}
			}
		}
		updated.add(this.exchange.getRequest());
		ldap.setRequestControls(updated.toArray(Control[]::new));
	}

	/**
	 * Extracts the response control matching the request control's ID from the
	 * {@link LdapContext} and updates the exchange.
	 * @param ctx the {@link DirContext} to post-process; must be an instance of
	 * {@link LdapContext}
	 * @throws NamingException if an error occurs while processing
	 * @throws IllegalArgumentException if {@code ctx} is not an {@link LdapContext}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void postProcess(DirContext ctx) throws NamingException {
		if (!(ctx instanceof LdapContext ldap)) {
			throw new IllegalArgumentException("ctx must be of type LdapContext");
		}
		Control[] responseControls = ldap.getResponseControls();
		if (responseControls == null || responseControls.length == 0) {
			this.noResultResponseControlHandler.accept(ctx);
			return;
		}
		for (Control responseControl : responseControls) {
			if (this.exchange.getRequest().getID().equals(responseControl.getID())) {
				this.exchange = this.exchange.withResponse((T) responseControl);
				if (this.log.isTraceEnabled()) {
					this.log.trace("Replacing paged results request with " + this.exchange.getRequest());
				}
				return;
			}
		}
		this.noResultResponseControlHandler.accept(ctx);
	}

	/**
	 * Returns the current control exchange containing the request and response controls.
	 * <p>
	 * The exchange is updated after each {@link #postProcess(DirContext)} call when a
	 * matching response control is found.
	 * @return the current {@link ControlExchange}
	 */
	public ControlExchange<S, T> getExchange() {
		return this.exchange;
	}

}
