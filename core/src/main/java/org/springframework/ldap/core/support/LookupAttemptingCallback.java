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

import org.springframework.ldap.core.AuthenticatedLdapEntryContextCallback;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * Attempts to perform an LDAP operation in the authenticated context, because
 * Active Directory might allow bind with incorrect password (specifically empty
 * password), and later refuse operations. We want to fail fast when
 * authenticating. {@link #mapWithContext(javax.naming.directory.DirContext, org.springframework.ldap.core.LdapEntryIdentification)}
 * returns the {@link DirContextOperations} instance that results from the lookup operation. This instance
 * can be used to obtain information regarding the authenticated user.
 * 
 * @author Hugo Josefson
 * @author Mattias Hellborg Arthursson
 * @since 1.3.1
 */
public class LookupAttemptingCallback implements
        AuthenticatedLdapEntryContextCallback, AuthenticatedLdapEntryContextMapper<DirContextOperations> {
    @Override
	public void executeWithContext(DirContext ctx, LdapEntryIdentification ldapEntryIdentification) {
        mapWithContext(ctx, ldapEntryIdentification);
	}

    @Override
    public DirContextOperations mapWithContext(DirContext ctx, LdapEntryIdentification ldapEntryIdentification) {
        try {
            return (DirContextOperations) ctx.lookup(ldapEntryIdentification.getRelativeName());
        }
        catch (NamingException e) {
            // rethrow, because we aren't allowed to throw checked exceptions.
            throw LdapUtils.convertLdapException(e);
        }
    }
}
