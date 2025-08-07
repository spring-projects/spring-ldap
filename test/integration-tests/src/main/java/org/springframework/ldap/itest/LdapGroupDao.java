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

package org.springframework.ldap.itest;

import javax.naming.Name;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.BaseLdapPathAware;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapGroupDao implements BaseLdapPathAware {

	private Name basePath;

	public LdapGroupDao() {
		super();
	}

	public void setBaseLdapPath(DistinguishedName baseLdapPath) {
		this.basePath = baseLdapPath;
	}

	public Name getBasePath() {
		return this.basePath;
	}

}
