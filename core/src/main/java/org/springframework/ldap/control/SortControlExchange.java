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
import java.io.UncheckedIOException;

import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortResponseControl;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * A {@link ControlExchange} implementation for managing LDAP server-side sort request and
 * response controls.
 * <p>
 * This class pairs a {@link SortControl} request with its corresponding
 * {@link SortResponseControl} response. The sort control requests that search results be
 * sorted by the server, and the response control indicates whether the sort was
 * successful and provides result codes if there were errors.
 * <p>
 * Instances of this class are immutable. The {@link #withResponse(SortResponseControl)}
 * method returns a new instance with the updated response control.
 *
 * @author Josh Cummings
 * @since 4.1
 * @see SortControl
 * @see SortResponseControl
 * @see ControlExchange
 * @see <a href="https://www.rfc-editor.org/rfc/rfc2891.html">RFC 2891 - LDAP Control
 * Extension for Server Side Sorting of Search Results</a>
 */
public class SortControlExchange implements ControlExchange<SortControl, SortResponseControl> {

	private final SortControl request;

	private final @Nullable SortResponseControl response;

	/**
	 * Constructs a new sort control exchange with the specified attributes to sort by
	 * @param sortBy the attributes to sort by
	 */
	public SortControlExchange(String... sortBy) {
		Assert.notEmpty(sortBy, "sortBy cannot be empty");
		try {
			this.request = new SortControl(sortBy, true);
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		this.response = null;
	}

	/**
	 * Constructs a new sort control exchange with the specified request and response
	 * controls.
	 * @param request the sort request control
	 * @param response the sort response control
	 */
	SortControlExchange(SortControl request, SortResponseControl response) {
		this.request = request;
		this.response = response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SortControl getRequest() {
		return this.request;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nullable SortResponseControl getResponse() {
		return this.response;
	}

	/**
	 * Creates a new exchange with the specified response control.
	 * <p>
	 * If the provided control is not a {@link SortResponseControl}, this method returns
	 * the current exchange unchanged.
	 * @param response the response control to add to the exchange
	 * @return a new {@link SortControlExchange} with the response, or this exchange if
	 * the response is not a {@link SortResponseControl}
	 */
	@Override
	public SortControlExchange withResponse(SortResponseControl response) {
		return new SortControlExchange(this.request, response);
	}

}
