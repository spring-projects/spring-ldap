/*
 * Copyright 2005-2008 the original author or authors.
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
package org.springframework.ldap.support;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.support.AbstractContextMapper;

/**
 * <code>ContextMapper</code> implementation that maps the found entries to the
 * {@link LdapEntryIdentification} of each respective entry.
 * 
 * @author Mattias Hellborg Arthursson
 * @since 1.3
 */
public class LdapEntryIdentificationContextMapper extends AbstractContextMapper {

	protected Object doMapFromContext(DirContextOperations ctx) {
		return new LdapEntryIdentification(new DistinguishedName(ctx.getNameInNamespace()), new DistinguishedName(ctx
				.getDn()));
	}

}
