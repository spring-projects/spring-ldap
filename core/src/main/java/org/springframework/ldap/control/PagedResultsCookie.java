/*
 * Copyright 2005-2013 the original author or authors.
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

import javax.naming.ldap.PagedResultsControl;
import java.util.Arrays;

/**
 * Wrapper class for the cookie returned when using the {@link PagedResultsControl}.
 *
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class PagedResultsCookie {

	private byte[] cookie;

	/**
	 * Constructor.
	 * @param cookie the cookie returned by a PagedResultsResponseControl.
	 */
	public PagedResultsCookie(byte[] cookie) {
		if (cookie != null) {
			this.cookie = Arrays.copyOf(cookie, cookie.length);
		}
		else {
			this.cookie = null;
		}
	}

	/**
	 * Get the cookie.
	 * @return the cookie. This value may be <code>null</code>, indicating that there are
	 * no more requests, or that the control wasn't supported by the server.
	 */
	public byte[] getCookie() {
		if (this.cookie != null) {
			return Arrays.copyOf(this.cookie, this.cookie.length);
		}
		else {
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PagedResultsCookie that = (PagedResultsCookie) o;

		if (!Arrays.equals(this.cookie, that.cookie))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return this.cookie != null ? Arrays.hashCode(this.cookie) : 0;
	}

}
