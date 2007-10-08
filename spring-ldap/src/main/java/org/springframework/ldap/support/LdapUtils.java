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

package org.springframework.ldap.support;

import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.NamingException;
import org.springframework.util.Assert;

/**
 * Generic utility methods for working with LDAP. Mainly for internal use within
 * the framework, but also useful for custom code.
 * 
 * @author Ulrik Sandberg
 * @since 1.2
 */
public final class LdapUtils {

	private static final Log logger = LogFactory.getLog(LdapUtils.class);

	/**
	 * Not to be instantiated.
	 */
	private LdapUtils() {

	}

	/**
	 * Close the given JNDI Context and ignore any thrown exception. This is
	 * useful for typical <code>finally</code> blocks in JNDI code.
	 * 
	 * @param context the JNDI Context to close (may be <code>null</code>)
	 */
	public static void closeContext(DirContext context) {
		if (context != null) {
			try {
				context.close();
			}
			catch (NamingException ex) {
				logger.debug("Could not close JNDI DirContext", ex);
			}
			catch (Throwable ex) {
				// We don't trust the JNDI provider: It might throw
				// RuntimeException or Error.
				logger.debug("Unexpected exception on closing JNDI DirContext", ex);
			}
		}
	}

	/**
	 * Convert the specified checked
	 * {@link javax.naming.NamingException NamingException} to a Spring LDAP
	 * runtime {@link org.springframework.ldap.NamingException NamingException}
	 * equivalent.
	 * 
	 * @param ex the original checked NamingException to convert
	 * @return the Spring LDAP runtime NamingException wrapping the given
	 * exception
	 */
	public static NamingException convertLdapException(javax.naming.NamingException ex) {
		Assert.notNull(ex, "NamingException must not be null");

		if (ex instanceof javax.naming.directory.AttributeInUseException) {
			return new org.springframework.ldap.AttributeInUseException(
					(javax.naming.directory.AttributeInUseException) ex);
		}
		if (ex instanceof javax.naming.directory.AttributeModificationException) {
			return new org.springframework.ldap.AttributeModificationException(
					(javax.naming.directory.AttributeModificationException) ex);
		}
		if (ex instanceof javax.naming.AuthenticationException) {
			return new org.springframework.ldap.AuthenticationException((javax.naming.AuthenticationException) ex);
		}
		if (ex instanceof javax.naming.AuthenticationNotSupportedException) {
			return new org.springframework.ldap.AuthenticationNotSupportedException(
					(javax.naming.AuthenticationNotSupportedException) ex);
		}
		if (ex instanceof javax.naming.CannotProceedException) {
			return new org.springframework.ldap.CannotProceedException((javax.naming.CannotProceedException) ex);
		}
		if (ex instanceof javax.naming.CommunicationException) {
			return new org.springframework.ldap.CommunicationException((javax.naming.CommunicationException) ex);
		}
		if (ex instanceof javax.naming.ConfigurationException) {
			return new org.springframework.ldap.ConfigurationException((javax.naming.ConfigurationException) ex);
		}
		if (ex instanceof javax.naming.ContextNotEmptyException) {
			return new org.springframework.ldap.ContextNotEmptyException((javax.naming.ContextNotEmptyException) ex);
		}
		if (ex instanceof javax.naming.InsufficientResourcesException) {
			return new org.springframework.ldap.InsufficientResourcesException(
					(javax.naming.InsufficientResourcesException) ex);
		}
		if (ex instanceof javax.naming.InterruptedNamingException) {
			return new org.springframework.ldap.InterruptedNamingException((javax.naming.InterruptedNamingException) ex);
		}
		if (ex instanceof javax.naming.directory.InvalidAttributeIdentifierException) {
			return new org.springframework.ldap.InvalidAttributeIdentifierException(
					(javax.naming.directory.InvalidAttributeIdentifierException) ex);
		}
		if (ex instanceof javax.naming.directory.InvalidAttributesException) {
			return new org.springframework.ldap.InvalidAttributesException(
					(javax.naming.directory.InvalidAttributesException) ex);
		}
		if (ex instanceof javax.naming.directory.InvalidAttributeValueException) {
			return new org.springframework.ldap.InvalidAttributeValueException(
					(javax.naming.directory.InvalidAttributeValueException) ex);
		}
		if (ex instanceof javax.naming.InvalidNameException) {
			return new org.springframework.ldap.InvalidNameException((javax.naming.InvalidNameException) ex);
		}
		if (ex instanceof javax.naming.directory.InvalidSearchControlsException) {
			return new org.springframework.ldap.InvalidSearchControlsException(
					(javax.naming.directory.InvalidSearchControlsException) ex);
		}
		if (ex instanceof javax.naming.directory.InvalidSearchFilterException) {
			return new org.springframework.ldap.InvalidSearchFilterException(
					(javax.naming.directory.InvalidSearchFilterException) ex);
		}
		if (ex instanceof javax.naming.ldap.LdapReferralException) {
			return new org.springframework.ldap.LdapReferralException((javax.naming.ldap.LdapReferralException) ex);
		}
		if (ex instanceof javax.naming.LimitExceededException) {
			return new org.springframework.ldap.LimitExceededException((javax.naming.LimitExceededException) ex);
		}
		if (ex instanceof javax.naming.LinkException) {
			return new org.springframework.ldap.LinkException((javax.naming.LinkException) ex);
		}
		if (ex instanceof javax.naming.LinkLoopException) {
			return new org.springframework.ldap.LinkLoopException((javax.naming.LinkLoopException) ex);
		}
		if (ex instanceof javax.naming.MalformedLinkException) {
			return new org.springframework.ldap.MalformedLinkException((javax.naming.MalformedLinkException) ex);
		}
		if (ex instanceof javax.naming.NameAlreadyBoundException) {
			return new org.springframework.ldap.NameAlreadyBoundException((javax.naming.NameAlreadyBoundException) ex);
		}
		if (ex instanceof javax.naming.NameNotFoundException) {
			return new org.springframework.ldap.NameNotFoundException((javax.naming.NameNotFoundException) ex);
		}
		if (ex instanceof javax.naming.NoInitialContextException) {
			return new org.springframework.ldap.NoInitialContextException((javax.naming.NoInitialContextException) ex);
		}
		if (ex instanceof javax.naming.NoPermissionException) {
			return new org.springframework.ldap.NoPermissionException((javax.naming.NoPermissionException) ex);
		}
		if (ex instanceof javax.naming.directory.NoSuchAttributeException) {
			return new org.springframework.ldap.NoSuchAttributeException(
					(javax.naming.directory.NoSuchAttributeException) ex);
		}
		if (ex instanceof javax.naming.NotContextException) {
			return new org.springframework.ldap.NotContextException((javax.naming.NotContextException) ex);
		}
		if (ex instanceof javax.naming.OperationNotSupportedException) {
			return new org.springframework.ldap.OperationNotSupportedException(
					(javax.naming.OperationNotSupportedException) ex);
		}
		if (ex instanceof javax.naming.PartialResultException) {
			return new org.springframework.ldap.PartialResultException((javax.naming.PartialResultException) ex);
		}
		if (ex instanceof javax.naming.directory.SchemaViolationException) {
			return new org.springframework.ldap.SchemaViolationException(
					(javax.naming.directory.SchemaViolationException) ex);
		}
		if (ex instanceof javax.naming.ServiceUnavailableException) {
			return new org.springframework.ldap.ServiceUnavailableException(
					(javax.naming.ServiceUnavailableException) ex);
		}
		if (ex instanceof javax.naming.SizeLimitExceededException) {
			return new org.springframework.ldap.SizeLimitExceededException((javax.naming.SizeLimitExceededException) ex);
		}
		if (ex instanceof javax.naming.TimeLimitExceededException) {
			return new org.springframework.ldap.TimeLimitExceededException((javax.naming.TimeLimitExceededException) ex);
		}

		// fallback
		return new org.springframework.ldap.UncategorizedLdapException(ex);
	}
}
