/*
 * Copyright 2005-2023 the original author or authors.
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

/**
 * Callback interface to be used in the authentication methods in {@link LdapOperations}
 * for performing operations when there are authentication errors. Can be useful when the
 * cause of the authentication failure needs to be retrieved.
 *
 * @author Ulrik Sandberg
 * @since 1.3.1
 */
public interface AuthenticationErrorCallback {

	/**
	 * This method will be called with the authentication exception in case there is a
	 * problem with the authentication.
	 * @param e the exception that was caught in the authentication method
	 */
	void execute(Exception e);

}
