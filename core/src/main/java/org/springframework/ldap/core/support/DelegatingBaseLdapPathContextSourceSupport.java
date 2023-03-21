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

package org.springframework.ldap.core.support;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;

import javax.naming.ldap.LdapName;

/**
 * Support class to provide {@link BaseLdapPathSource} functionality to ContextSource
 * instances that act as proxies.
 *
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public abstract class DelegatingBaseLdapPathContextSourceSupport implements BaseLdapPathSource {

	/**
	 * Get the target ContextSource.
	 * @return the target ContextSource.
	 */
	protected abstract ContextSource getTarget();

	private BaseLdapPathSource getTargetAsBaseLdapPathSource() {
		try {
			return (BaseLdapPathSource) getTarget();
		}
		catch (ClassCastException e) {
			throw new UnsupportedOperationException(
					"This operation is not supported on a target ContextSource that does not "
							+ " implement BaseLdapPathContextSource",
					e);
		}
	}

	@Override
	public final LdapName getBaseLdapName() {
		return getTargetAsBaseLdapPathSource().getBaseLdapName();
	}

	@Override
	public final DistinguishedName getBaseLdapPath() {
		return getTargetAsBaseLdapPathSource().getBaseLdapPath();
	}

	@Override
	public final String getBaseLdapPathAsString() {
		return getTargetAsBaseLdapPathSource().getBaseLdapPathAsString();
	}

}
