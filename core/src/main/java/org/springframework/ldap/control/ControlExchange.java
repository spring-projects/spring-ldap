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

import javax.naming.ldap.Control;

import org.jspecify.annotations.Nullable;

/**
 * Represents an exchange of LDAP request and response controls.
 * <p>
 * This interface defines the contract for managing the lifecycle of LDAP controls,
 * pairing a request {@link Control} with its corresponding response {@link Control}.
 * Implementations are typically immutable, with the {@link #withResponse(Control)} method
 * returning a new instance that includes the response control.
 * <p>
 * Control exchanges are used by {@link ControlExchangeDirContextProcessor} to manage
 * controls across LDAP operations, particularly for stateful controls that require
 * maintaining information between successive requests (such as paged results or
 * server-side sorting).
 *
 * @param <S> the type of the request {@link Control}
 * @param <T> the type of the response {@link Control}
 * @author Josh Cummings
 * @since 4.1
 * @see ControlExchangeDirContextProcessor
 * @see PagedResultsControlExchange
 * @see SortControlExchange
 */
public interface ControlExchange<S extends Control, T extends Control> {

	/**
	 * Returns the request control.
	 * @return the request control
	 */
	S getRequest();

	/**
	 * Returns the response control, if one has been received.
	 * @return the response control, or {@code null} if no response has been received
	 */
	@Nullable T getResponse();

	/**
	 * Creates a new exchange with the specified response control.
	 * <p>
	 * Implementations typically create a new instance with the updated response,
	 * maintaining immutability. Some implementations may also update the request control
	 * based on information from the response (such as copying a cookie for paged
	 * results).
	 * @param response the response control to include in the new exchange
	 * @return a new {@link ControlExchange} with the response
	 */
	ControlExchange<S, T> withResponse(T response);

}
