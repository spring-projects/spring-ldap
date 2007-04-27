/*
 * Copyright 2005-2007 the original author or authors.
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

package org.springframework.ldap.core.support;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;


/**
 * ContextSource implementation which creates InitialDirContext instances, for
 * LDAPv2 compatibility. For configuration information, see
 * {@link org.springframework.ldap.core.support.AbstractContextSource AbstractContextSource}.
 * 
 * @see org.springframework.ldap.core.support.AbstractContextSource
 * 
 * @author Mattias Arthursson
 */
public class DirContextSource extends AbstractContextSource {

    /**
     * Create a new InitialDirContext instance.
     * 
     * @param environment
     *            the environment to use when creating the context.
     * @return a new InitialDirContext implementation.
     */
    protected DirContext getDirContextInstance(Hashtable environment)
            throws NamingException {
        return new InitialDirContext(environment);
    }
}
