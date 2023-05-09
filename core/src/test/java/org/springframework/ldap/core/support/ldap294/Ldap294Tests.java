/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.ldap.core.support.ldap294;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.Test;

import org.springframework.ldap.core.support.AbstractContextSource;

/**
 * These tests just ensure that the subclass compiles
 *
 * @author Rob Winch
 *
 */
public class Ldap294Tests {

	@Test
	public void concerteContextSourceCanAccessPasswordAndUserDn() {
	}

	static class ConcerteContextSource extends AbstractContextSource {

		@Override
		protected DirContext getDirContextInstance(Hashtable<String, Object> environment) throws NamingException {
			// Verify a subclass outside of package scope can access password
			// and userDn since Spring Security needs to be able to access these
			// properties.
			String pass = super.getPassword();
			String userDn = super.getUserDn();
			return null;
		}

	}

}
