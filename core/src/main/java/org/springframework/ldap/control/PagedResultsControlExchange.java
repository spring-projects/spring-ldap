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

import java.io.IOException;

import javax.naming.ldap.Control;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ControlExchange} implementation for managing LDAP paged results request and
 * response controls.
 * <p>
 * This class pairs a {@link PagedResultsControl} request with its corresponding
 * {@link PagedResultsResponseControl} response. The paged results control allows clients
 * to retrieve search results in pages of a specified size, with the response control
 * providing a cookie for retrieving subsequent pages.
 * <p>
 * Instances of this class are immutable. The
 * {@link #withResponse(PagedResultsResponseControl)} method returns a new instance with
 * an updated request control that includes the cookie from the response, ready for the
 * next page request.
 *
 * @author Josh Cummings
 * @since 4.1
 * @see PagedResultsControl
 * @see PagedResultsResponseControl
 * @see ControlExchange
 * @see <a href="https://www.rfc-editor.org/rfc/rfc2696.html">RFC 2696 - LDAP Control
 * Extension for Simple Paged Results Manipulation</a>
 */
public class PagedResultsControlExchange implements ControlExchange<PagedResultsControl, PagedResultsResponseControl> {

	private final SpringLdapPagedResultsControl request;

	private final @Nullable PagedResultsResponseControl response;

	/**
	 * Constructs a new paged results control exchange with the specified page size
	 * @param pageSize the number of entries to return in each page
	 */
	public PagedResultsControlExchange(int pageSize) {
		this.request = new SpringLdapPagedResultsControl(pageSize);
		this.response = null;
	}

	/**
	 * Constructs a new paged results control exchange with the specified request and
	 * response controls.
	 * @param request the paged results request control
	 * @param response the paged results response control
	 */
	PagedResultsControlExchange(SpringLdapPagedResultsControl request, PagedResultsResponseControl response) {
		this.request = request;
		this.response = response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PagedResultsControl getRequest() {
		return this.request.delegate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable PagedResultsResponseControl getResponse() {
		return this.response;
	}

	/**
	 * Creates a new exchange with an updated request control that includes the cookie
	 * from the response control.
	 * <p>
	 * This method creates a new {@link SpringLdapPagedResultsControl} with the same page
	 * size and criticality as the current request, but with the cookie from the provided
	 * response. This prepares the control for requesting the next page of results.
	 * @param response the response control containing the cookie for the next page
	 * @return a new {@link PagedResultsControlExchange} with the updated request and the
	 * provided response
	 */
	@Override
	public PagedResultsControlExchange withResponse(PagedResultsResponseControl response) {
		SpringLdapPagedResultsControl updated = new SpringLdapPagedResultsControl(this.request.getPageSize(),
				response.getCookie(), this.request.isCritical());
		return new PagedResultsControlExchange(updated, response);
	}

	static class SpringLdapPagedResultsControl implements Control {

		final PagedResultsControl delegate;

		private final int pageSize;

		private final byte @Nullable [] cookie;

		/**
		 * Constructs a critical paged results control with the specified page size and no
		 * cookie.
		 * @param pageSize the number of entries to return in each page
		 */
		SpringLdapPagedResultsControl(int pageSize) {
			this(pageSize, null, true);
		}

		/**
		 * Constructs a paged results control with the specified page size, cookie, and
		 * criticality.
		 * @param pageSize the number of entries to return in each page
		 * @param cookie the cookie returned by the server from a previous paged results
		 * operation, or {@code null} if this is the first request
		 * @param criticality whether the control is critical to the operation
		 */
		SpringLdapPagedResultsControl(int pageSize, byte @Nullable [] cookie, boolean criticality) {
			try {
				this.delegate = new PagedResultsControl(pageSize, cookie, criticality);
			}
			catch (IOException ex) {
				throw new IllegalArgumentException(ex);
			}
			this.pageSize = pageSize;
			this.cookie = cookie;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getID() {
			return this.delegate.getID();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public byte[] getEncodedValue() {
			return this.delegate.getEncodedValue();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isCritical() {
			return this.delegate.isCritical();
		}

		int getPageSize() {
			return this.pageSize;
		}

		byte @Nullable [] getCookie() {
			return this.cookie;
		}

		@Override
		public String toString() {
			return "PagedResultsRequest [pageSize=" + this.pageSize + ", cookie=" + (this.cookie != null)
					+ ", critical=" + this.delegate.isCritical() + "]";
		}

	}

}
