/*
 * Copyright 2002-2005 the original author or authors.
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

import javax.naming.CommunicationException;
import javax.naming.ContextNotEmptyException;
import javax.naming.LimitExceededException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.InvalidAttributesException;
import javax.naming.directory.InvalidSearchControlsException;
import javax.naming.directory.InvalidSearchFilterException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.ldap.BadLdapGrammarException;

/**
 * The default implementation of NamingExceptionTranslator.
 * 
 * @author Mattias Arthursson
 */
public class DefaultNamingExceptionTranslator implements
        NamingExceptionTranslator {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.NamingExceptionTranslator#translate(java.lang.String,
     *      java.lang.String, java.lang.String, javax.naming.NamingException)
     */
    public DataAccessException translate(NamingException namingException) {

        if (namingException instanceof NameNotFoundException) {
            return new EntryNotFoundException("Entry not found",
                    namingException);
        }

        if (namingException instanceof InvalidSearchFilterException) {
            return new BadLdapGrammarException("Invalid search filter",
                    namingException);
        }

        if (namingException instanceof InvalidSearchControlsException) {
            return new InvalidDataAccessApiUsageException(
                    "Invalid search controls supplied by internal API",
                    namingException);
        }

        if (namingException instanceof NameAlreadyBoundException) {
            return new DataIntegrityViolationException("Name already bound",
                    namingException);
        }

        if (namingException instanceof ContextNotEmptyException) {
            return new DataIntegrityViolationException(
                    "The context needs to be empty in order to be removed",
                    namingException);
        }

        if (namingException instanceof InvalidAttributesException) {
            return new AttributesIntegrityViolationException(
                    "Invalid attributes", namingException);
        }

        if (namingException instanceof LimitExceededException) {
            return new SearchLimitExceededException("Too many objects found",
                    namingException);
        }

        if (namingException instanceof CommunicationException) {
            throw new DataRetrievalFailureException(
                    "Unable to communicate with LDAP server", namingException);
        }

        // Fallback - other type of NamingException encountered.
        return new UncategorizedLdapException("Operation failed",
                namingException);
    }
}
