/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.core;

/**
 * Convenience implementation of AuthenticationErrorCallback that stores the
 * given exception and provides a method for retrieving it. The caller of the
 * authenticate method can provide an instance of this class as an error
 * callback. If the authentication fails, the caller can ask the callback
 * instance for the actual authentication exception.
 * 
 * @author Ulrik Sandberg
 * @since 1.3.1
 */
public final class CollectingAuthenticationErrorCallback implements AuthenticationErrorCallback {
	private Exception error;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.ldap.core.AuthenticationErrorCallback#execute(java
	 * .lang.Exception)
	 */
	public void execute(Exception e) {
		this.error = e;
	}

	/**
	 * @return the collected exception
	 */
	public Exception getError() {
		return error;
	}

    /**
     * Check whether this callback has collected an error.
     *
     * @return <code>true</code> if an error has been collected, <code>false</code> otherwise.
     */
    public boolean hasError() {
        return error != null;
    }
}