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

import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.ContextSource;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.Hashtable;

/**
 * A strategy to use when authenticating LDAP connections on creation. When authenticating
 * LDAP connections different strategies are needed depending on the authentication
 * mechanism used. Furthermore, depending on the mechanism the work to be done needs to be
 * applied at different stages of the <code>DirContext</code> creation process. A
 * DirContextAuthenticationStrategy contains the logic to perform a particular type of
 * authentication mechanism and will be called by its {@link ContextSource} at appropriate
 * stages of the process.
 *
 * @author Mattias Hellborg Arthursson
 */
public interface DirContextAuthenticationStrategy {

	/**
	 * This method is responsible for preparing the environment to be used when creating
	 * the <code>DirContext</code> instance. The base environment (including URL,
	 * <code>ContextFactory</code> etc. will already be set, and this method is called
	 * just before the actual Context is to be created.
	 * @param env The <code>Hashtable</code> to be sent to the <code>DirContext</code>
	 * instance on initialization. Pre-configured with the basic settings; the
	 * implementation of this method is responsible for manipulating the environment as
	 * appropriate for the particular authentication mechanism.
	 * @param userDn the user DN to authenticate, as received from the
	 * {@link AuthenticationSource} of the {@link ContextSource}.
	 * @param password the password to authenticate with, as received from the
	 * {@link AuthenticationSource} of the {@link ContextSource}.
	 * @throws NamingException if anything goes wrong. This will cause the
	 * <code>DirContext</code> creation to be aborted and the exception to be translated
	 * and rethrown.
	 */
	void setupEnvironment(Hashtable<String, Object> env, String userDn, String password) throws NamingException;

	/**
	 * This method is responsible for post-processing the <code>DirContext</code> instance
	 * after it has been created. It will be called immediately after the instance has
	 * been created. Some authentication mechanisms, e.g. TLS, require particular stuff to
	 * happen before the actual target Context is closed. This method provides the
	 * possibility to replace or wrap the actual DirContext with a proxy so that any calls
	 * on it may be intercepted.
	 * @param ctx the freshly created <code>DirContext</code> instance. The actual
	 * implementation class (e.g. <code>InitialLdapContext</code>) depends on the
	 * {@link ContextSource} implementation.
	 * @param userDn the user DN to authenticate, as received from the
	 * {@link AuthenticationSource} of the {@link ContextSource}.
	 * @param password the password to authenticate with, as received from the
	 * {@link AuthenticationSource} of the {@link ContextSource}.
	 * @return the DirContext, possibly modified, replaced or wrapped.
	 * @throws NamingException if anything goes wrong. This will cause the
	 * <code>DirContext</code> creation to be aborted and the exception to be translated
	 * and rethrown.
	 */
	DirContext processContextAfterCreation(DirContext ctx, String userDn, String password) throws NamingException;

}
