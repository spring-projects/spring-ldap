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

package org.springframework.ldap;

/**
 * Runtime exception mirroring the JNDI AttributeInUseException.
 *
 * @author Ulrik Sandberg
 * @since 1.2
 * @see javax.naming.directory.AttributeInUseException
 */
public class AttributeInUseException extends NamingException {

	public AttributeInUseException(javax.naming.directory.AttributeInUseException cause) {
		super(cause);
	}

}
