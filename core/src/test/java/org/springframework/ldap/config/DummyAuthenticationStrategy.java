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

package org.springframework.ldap.config;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;

/**
 * @author Mattias Hellborg Arthursson
 */
public class DummyAuthenticationStrategy implements DirContextAuthenticationStrategy {

	@Override
	public void setupEnvironment(Hashtable<String, Object> env, String userDn, String password) throws NamingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DirContext processContextAfterCreation(DirContext ctx, String userDn, String password)
			throws NamingException {
		throw new UnsupportedOperationException();
	}

}
