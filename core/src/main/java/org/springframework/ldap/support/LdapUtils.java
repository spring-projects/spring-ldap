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

import java.util.Collection;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.NoSuchAttributeException;
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
	 * Convert the specified checked {@link javax.naming.NamingException
	 * NamingException} to a Spring LDAP runtime
	 * {@link org.springframework.ldap.NamingException NamingException}
	 * equivalent.
	 * 
	 * @param ex the original checked NamingException to convert
	 * @return the Spring LDAP runtime NamingException wrapping the given
	 * exception
	 */
	public static NamingException convertLdapException(javax.naming.NamingException ex) {
		Assert.notNull(ex, "NamingException must not be null");

		if (ex.getClass().equals(javax.naming.directory.AttributeInUseException.class)) {
			return new org.springframework.ldap.AttributeInUseException(
					(javax.naming.directory.AttributeInUseException) ex);
		}
		if (ex.getClass().equals(javax.naming.directory.AttributeModificationException.class)) {
			return new org.springframework.ldap.AttributeModificationException(
					(javax.naming.directory.AttributeModificationException) ex);
		}
		if (ex.getClass().equals(javax.naming.CannotProceedException.class)) {
			return new org.springframework.ldap.CannotProceedException((javax.naming.CannotProceedException) ex);
		}
		if (ex.getClass().equals(javax.naming.CommunicationException.class)) {
			return new org.springframework.ldap.CommunicationException((javax.naming.CommunicationException) ex);
		}
		if (ex.getClass().equals(javax.naming.ConfigurationException.class)) {
			return new org.springframework.ldap.ConfigurationException((javax.naming.ConfigurationException) ex);
		}
		if (ex.getClass().equals(javax.naming.ContextNotEmptyException.class)) {
			return new org.springframework.ldap.ContextNotEmptyException((javax.naming.ContextNotEmptyException) ex);
		}
		if (ex.getClass().equals(javax.naming.InsufficientResourcesException.class)) {
			return new org.springframework.ldap.InsufficientResourcesException(
					(javax.naming.InsufficientResourcesException) ex);
		}
		if (ex.getClass().equals(javax.naming.InterruptedNamingException.class)) {
			return new org.springframework.ldap.InterruptedNamingException((javax.naming.InterruptedNamingException) ex);
		}
		if (ex.getClass().equals(javax.naming.directory.InvalidAttributeIdentifierException.class)) {
			return new org.springframework.ldap.InvalidAttributeIdentifierException(
					(javax.naming.directory.InvalidAttributeIdentifierException) ex);
		}
		if (ex.getClass().equals(javax.naming.directory.InvalidAttributesException.class)) {
			return new org.springframework.ldap.InvalidAttributesException(
					(javax.naming.directory.InvalidAttributesException) ex);
		}
		if (ex.getClass().equals(javax.naming.directory.InvalidAttributeValueException.class)) {
			return new org.springframework.ldap.InvalidAttributeValueException(
					(javax.naming.directory.InvalidAttributeValueException) ex);
		}
		if (ex.getClass().equals(javax.naming.InvalidNameException.class)) {
			return new org.springframework.ldap.InvalidNameException((javax.naming.InvalidNameException) ex);
		}
		if (ex.getClass().equals(javax.naming.directory.InvalidSearchControlsException.class)) {
			return new org.springframework.ldap.InvalidSearchControlsException(
					(javax.naming.directory.InvalidSearchControlsException) ex);
		}
		if (ex.getClass().equals(javax.naming.directory.InvalidSearchFilterException.class)) {
			return new org.springframework.ldap.InvalidSearchFilterException(
					(javax.naming.directory.InvalidSearchFilterException) ex);
		}

		// this class is abstract, so it can never be of exactly this class;
		// using instanceof
		if (ex instanceof javax.naming.ldap.LdapReferralException) {
			return new org.springframework.ldap.LdapReferralException((javax.naming.ldap.LdapReferralException) ex);
		}
		// Skipping the abstract superclass javax.naming.ReferralException

		// LimitExceededException hierarchy
		if (ex.getClass().equals(javax.naming.SizeLimitExceededException.class)) {
			return new org.springframework.ldap.SizeLimitExceededException((javax.naming.SizeLimitExceededException) ex);
		}
		if (ex.getClass().equals(javax.naming.TimeLimitExceededException.class)) {
			return new org.springframework.ldap.TimeLimitExceededException((javax.naming.TimeLimitExceededException) ex);
		}
		// this class is the superclass of the two above
		if (ex.getClass().equals(javax.naming.LimitExceededException.class)) {
			return new org.springframework.ldap.LimitExceededException((javax.naming.LimitExceededException) ex);
		}

		// LinkException hierarchy
		if (ex.getClass().equals(javax.naming.LinkLoopException.class)) {
			return new org.springframework.ldap.LinkLoopException((javax.naming.LinkLoopException) ex);
		}
		if (ex.getClass().equals(javax.naming.MalformedLinkException.class)) {
			return new org.springframework.ldap.MalformedLinkException((javax.naming.MalformedLinkException) ex);
		}
		// this class is the superclass of the two above
		if (ex.getClass().equals(javax.naming.LinkException.class)) {
			return new org.springframework.ldap.LinkException((javax.naming.LinkException) ex);
		}

		if (ex.getClass().equals(javax.naming.NameAlreadyBoundException.class)) {
			return new org.springframework.ldap.NameAlreadyBoundException((javax.naming.NameAlreadyBoundException) ex);
		}
		if (ex.getClass().equals(javax.naming.NameNotFoundException.class)) {
			return new org.springframework.ldap.NameNotFoundException((javax.naming.NameNotFoundException) ex);
		}

		// NamingSecurityException hierarchy
		if (ex.getClass().equals(javax.naming.NoPermissionException.class)) {
			return new org.springframework.ldap.NoPermissionException((javax.naming.NoPermissionException) ex);
		}
		if (ex.getClass().equals(javax.naming.AuthenticationException.class)) {
			return new org.springframework.ldap.AuthenticationException((javax.naming.AuthenticationException) ex);
		}
		if (ex.getClass().equals(javax.naming.AuthenticationNotSupportedException.class)) {
			return new org.springframework.ldap.AuthenticationNotSupportedException(
					(javax.naming.AuthenticationNotSupportedException) ex);
		}
		// Skipping the abstract superclass javax.naming.NamingSecurityException

		if (ex.getClass().equals(javax.naming.NoInitialContextException.class)) {
			return new org.springframework.ldap.NoInitialContextException((javax.naming.NoInitialContextException) ex);
		}
		if (ex.getClass().equals(javax.naming.directory.NoSuchAttributeException.class)) {
			return new org.springframework.ldap.NoSuchAttributeException(
					(javax.naming.directory.NoSuchAttributeException) ex);
		}
		if (ex.getClass().equals(javax.naming.NotContextException.class)) {
			return new org.springframework.ldap.NotContextException((javax.naming.NotContextException) ex);
		}
		if (ex.getClass().equals(javax.naming.OperationNotSupportedException.class)) {
			return new org.springframework.ldap.OperationNotSupportedException(
					(javax.naming.OperationNotSupportedException) ex);
		}
		if (ex.getClass().equals(javax.naming.PartialResultException.class)) {
			return new org.springframework.ldap.PartialResultException((javax.naming.PartialResultException) ex);
		}
		if (ex.getClass().equals(javax.naming.directory.SchemaViolationException.class)) {
			return new org.springframework.ldap.SchemaViolationException(
					(javax.naming.directory.SchemaViolationException) ex);
		}
		if (ex.getClass().equals(javax.naming.ServiceUnavailableException.class)) {
			return new org.springframework.ldap.ServiceUnavailableException(
					(javax.naming.ServiceUnavailableException) ex);
		}

		// fallback
		return new org.springframework.ldap.UncategorizedLdapException(ex);
	}

	/**
	 * Get the actual class of the supplied DirContext instance; LdapContext or
	 * DirContext.
	 * 
	 * @param context the DirContext instance to check.
	 * @return LdapContext.class if context is an LdapContext, DirContext.class
	 * otherwise.
	 */
	public static Class getActualTargetClass(DirContext context) {
		if (context instanceof LdapContext) {
			return LdapContext.class;
		}

		return DirContext.class;
	}

	/**
	 * Collect all the values of a the specified attribute from the supplied
	 * Attributes.
	 * 
	 * @param attributes The Attributes; not <code>null</code>.
	 * @param name The name of the Attribute to get values for.
	 * @param collection the collection to collect the values in.
	 * @throws NoSuchAttributeException if no attribute with the specified name
	 * exists.
	 * @since 1.3
	 */
	public static void collectAttributeValues(Attributes attributes, String name, Collection collection) {
		Assert.notNull(attributes, "Attributes must not be null");
		Attribute attribute = attributes.get(name);
		if (attribute == null) {
			throw new NoSuchAttributeException("No attribute with name '" + name + "'");
		}

		iterateAttributeValues(attribute, new CollectingAttributeValueCallbackHandler(collection));
	}

	/**
	 * Iterate through all the values of the specified Attribute calling back to
	 * the specified callbackHandler.
	 * @param attribute the Attribute to work with; not <code>null</code>.
	 * @param callbackHandler the callbackHandler; not <code>null</code>.
	 * @since 1.3
	 */
	public static void iterateAttributeValues(Attribute attribute, AttributeValueCallbackHandler callbackHandler) {
		Assert.notNull(attribute, "Attribute must not be null");
		Assert.notNull(callbackHandler, "callbackHandler must not be null");

		for (int i = 0; i < attribute.size(); i++) {
			try {
				callbackHandler.handleAttributeValue(attribute.getID(), attribute.get(i), i);
			}
			catch (javax.naming.NamingException e) {
				throw LdapUtils.convertLdapException(e);
			}
		}
	}

	/**
	 * An {@link AttributeValueCallbackHandler} to collect values in a supplied
	 * collection.
	 * 
	 * @author Mattias Hellborg Arthursson
	 */
	private final static class CollectingAttributeValueCallbackHandler implements AttributeValueCallbackHandler {
		private final Collection collection;

		public CollectingAttributeValueCallbackHandler(Collection collection) {
			Assert.notNull(collection, "Collection must not be null");
			this.collection = collection;
		}

		public final void handleAttributeValue(String attributeName, Object attributeValue, int index) {
			collection.add(attributeValue);
		}
	}
}
