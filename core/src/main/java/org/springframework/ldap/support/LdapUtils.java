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

package org.springframework.ldap.support;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.NamingException;
import org.springframework.ldap.NoSuchAttributeException;
import org.springframework.util.Assert;

/**
 * Generic utility methods for working with LDAP. Mainly for internal use within the
 * framework, but also useful for custom code.
 *
 * @author Ulrik Sandberg
 * @author Mattias Hellborg Arthursson
 * @since 1.2
 */
public final class LdapUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtils.class);

	private static final int HEX = 16;

	/**
	 * Not to be instantiated.
	 */
	private LdapUtils() {

	}

	/**
	 * Close the given JNDI Context and ignore any thrown exception. This is useful for
	 * typical <code>finally</code> blocks in JNDI code.
	 * @param context the JNDI Context to close (may be <code>null</code>)
	 */
	public static void closeContext(DirContext context) {
		if (context != null) {
			try {
				context.close();
			}
			catch (NamingException ex) {
				LOGGER.debug("Could not close JNDI DirContext", ex);
			}
			catch (Throwable ex) {
				// We don't trust the JNDI provider: It might throw
				// RuntimeException or Error.
				LOGGER.debug("Unexpected exception on closing JNDI DirContext", ex);
			}
		}
	}

	/**
	 * Convert the specified checked {@link javax.naming.NamingException NamingException}
	 * to a Spring LDAP runtime {@link org.springframework.ldap.NamingException
	 * NamingException} equivalent.
	 * @param ex the original checked NamingException to convert
	 * @return the Spring LDAP runtime NamingException wrapping the given exception
	 */
	public static NamingException convertLdapException(javax.naming.NamingException ex) {
		Assert.notNull(ex, "NamingException must not be null");

		if (javax.naming.directory.AttributeInUseException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.AttributeInUseException(
					(javax.naming.directory.AttributeInUseException) ex);
		}
		if (javax.naming.directory.AttributeModificationException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.AttributeModificationException(
					(javax.naming.directory.AttributeModificationException) ex);
		}
		if (javax.naming.CannotProceedException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.CannotProceedException((javax.naming.CannotProceedException) ex);
		}
		if (javax.naming.CommunicationException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.CommunicationException((javax.naming.CommunicationException) ex);
		}
		if (javax.naming.ConfigurationException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.ConfigurationException((javax.naming.ConfigurationException) ex);
		}
		if (javax.naming.ContextNotEmptyException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.ContextNotEmptyException((javax.naming.ContextNotEmptyException) ex);
		}
		if (javax.naming.InsufficientResourcesException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.InsufficientResourcesException(
					(javax.naming.InsufficientResourcesException) ex);
		}
		if (javax.naming.InterruptedNamingException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.InterruptedNamingException(
					(javax.naming.InterruptedNamingException) ex);
		}
		if (javax.naming.directory.InvalidAttributeIdentifierException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.InvalidAttributeIdentifierException(
					(javax.naming.directory.InvalidAttributeIdentifierException) ex);
		}
		if (javax.naming.directory.InvalidAttributesException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.InvalidAttributesException(
					(javax.naming.directory.InvalidAttributesException) ex);
		}
		if (javax.naming.directory.InvalidAttributeValueException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.InvalidAttributeValueException(
					(javax.naming.directory.InvalidAttributeValueException) ex);
		}
		if (javax.naming.InvalidNameException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.InvalidNameException((javax.naming.InvalidNameException) ex);
		}
		if (javax.naming.directory.InvalidSearchControlsException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.InvalidSearchControlsException(
					(javax.naming.directory.InvalidSearchControlsException) ex);
		}
		if (javax.naming.directory.InvalidSearchFilterException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.InvalidSearchFilterException(
					(javax.naming.directory.InvalidSearchFilterException) ex);
		}

		if (javax.naming.ldap.LdapReferralException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.LdapReferralException((javax.naming.ldap.LdapReferralException) ex);
		}

		if (javax.naming.ReferralException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.ReferralException((javax.naming.ReferralException) ex);
		}

		// LimitExceededException hierarchy
		if (javax.naming.SizeLimitExceededException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.SizeLimitExceededException(
					(javax.naming.SizeLimitExceededException) ex);
		}
		if (javax.naming.TimeLimitExceededException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.TimeLimitExceededException(
					(javax.naming.TimeLimitExceededException) ex);
		}
		// this class is the superclass of the two above
		if (javax.naming.LimitExceededException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.LimitExceededException((javax.naming.LimitExceededException) ex);
		}

		// LinkException hierarchy
		if (javax.naming.LinkLoopException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.LinkLoopException((javax.naming.LinkLoopException) ex);
		}
		if (javax.naming.MalformedLinkException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.MalformedLinkException((javax.naming.MalformedLinkException) ex);
		}
		// this class is the superclass of the two above
		if (javax.naming.LinkException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.LinkException((javax.naming.LinkException) ex);
		}

		if (javax.naming.NameAlreadyBoundException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.NameAlreadyBoundException((javax.naming.NameAlreadyBoundException) ex);
		}
		if (javax.naming.NameNotFoundException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.NameNotFoundException((javax.naming.NameNotFoundException) ex);
		}

		// NamingSecurityException hierarchy
		if (javax.naming.NoPermissionException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.NoPermissionException((javax.naming.NoPermissionException) ex);
		}
		if (javax.naming.AuthenticationException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.AuthenticationException((javax.naming.AuthenticationException) ex);
		}
		if (javax.naming.AuthenticationNotSupportedException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.AuthenticationNotSupportedException(
					(javax.naming.AuthenticationNotSupportedException) ex);
		}
		if (javax.naming.NamingSecurityException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.NamingSecurityException((javax.naming.NamingSecurityException) ex);
		}

		if (javax.naming.NoInitialContextException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.NoInitialContextException((javax.naming.NoInitialContextException) ex);
		}
		if (javax.naming.directory.NoSuchAttributeException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.NoSuchAttributeException(
					(javax.naming.directory.NoSuchAttributeException) ex);
		}
		if (javax.naming.NotContextException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.NotContextException((javax.naming.NotContextException) ex);
		}
		if (javax.naming.OperationNotSupportedException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.OperationNotSupportedException(
					(javax.naming.OperationNotSupportedException) ex);
		}
		if (javax.naming.PartialResultException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.PartialResultException((javax.naming.PartialResultException) ex);
		}
		if (javax.naming.directory.SchemaViolationException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.SchemaViolationException(
					(javax.naming.directory.SchemaViolationException) ex);
		}
		if (javax.naming.ServiceUnavailableException.class.isAssignableFrom(ex.getClass())) {
			return new org.springframework.ldap.ServiceUnavailableException(
					(javax.naming.ServiceUnavailableException) ex);
		}

		// fallback
		return new org.springframework.ldap.UncategorizedLdapException(ex);
	}

	/**
	 * Get the actual class of the supplied DirContext instance; LdapContext or
	 * DirContext.
	 * @param context the DirContext instance to check.
	 * @return LdapContext.class if context is an LdapContext, DirContext.class otherwise.
	 */
	public static Class getActualTargetClass(DirContext context) {
		if (context instanceof LdapContext) {
			return LdapContext.class;
		}

		return DirContext.class;
	}

	/**
	 * Collect all the values of a the specified attribute from the supplied Attributes.
	 * @param attributes The Attributes; not <code>null</code>.
	 * @param name The name of the Attribute to get values for.
	 * @param collection the collection to collect the values in.
	 * @throws NoSuchAttributeException if no attribute with the specified name exists.
	 * @since 1.3
	 */
	public static void collectAttributeValues(Attributes attributes, String name, Collection<Object> collection) {
		collectAttributeValues(attributes, name, collection, Object.class);
	}

	/**
	 * Collect all the values of a the specified attribute from the supplied Attributes as
	 * the specified class.
	 * @param attributes The Attributes; not <code>null</code>.
	 * @param name The name of the Attribute to get values for.
	 * @param collection the collection to collect the values in.
	 * @param clazz the class of the collected attribute values
	 * @throws NoSuchAttributeException if no attribute with the specified name exists.
	 * @throws IllegalArgumentException if an attribute value cannot be cast to the
	 * specified class.
	 * @since 2.0
	 */
	public static <T> void collectAttributeValues(Attributes attributes, String name, Collection<T> collection,
			Class<T> clazz) {

		Assert.notNull(attributes, "Attributes must not be null");
		Assert.hasText(name, "Name must not be empty");
		Assert.notNull(collection, "Collection must not be null");

		Attribute attribute = attributes.get(name);
		if (attribute == null) {
			throw new NoSuchAttributeException("No attribute with name '" + name + "'");
		}

		iterateAttributeValues(attribute, new CollectingAttributeValueCallbackHandler<T>(collection, clazz));

	}

	/**
	 * Iterate through all the values of the specified Attribute calling back to the
	 * specified callbackHandler.
	 * @param attribute the Attribute to work with; not <code>null</code>.
	 * @param callbackHandler the callbackHandler; not <code>null</code>.
	 * @since 1.3
	 */
	public static void iterateAttributeValues(Attribute attribute, AttributeValueCallbackHandler callbackHandler) {
		Assert.notNull(attribute, "Attribute must not be null");
		Assert.notNull(callbackHandler, "callbackHandler must not be null");

		if (attribute instanceof Iterable) {
			int i = 0;
			for (Object obj : (Iterable) attribute) {
				handleAttributeValue(attribute.getID(), obj, i, callbackHandler);
				i++;
			}
		}
		else {
			for (int i = 0; i < attribute.size(); i++) {
				try {
					handleAttributeValue(attribute.getID(), attribute.get(i), i, callbackHandler);
				}
				catch (javax.naming.NamingException ex) {
					throw convertLdapException(ex);
				}
			}
		}
	}

	private static void handleAttributeValue(String attributeID, Object value, int i,
			AttributeValueCallbackHandler callbackHandler) {
		callbackHandler.handleAttributeValue(attributeID, value, i);
	}

	/**
	 * Converts a CompositeName to a String in a way that avoids escaping problems, such
	 * as the dreaded "triple backslash" problem.
	 * @param compositeName The CompositeName to convert
	 * @return String containing the String representation of <code>name</code>
	 */
	public static String convertCompositeNameToString(CompositeName compositeName) {
		if (compositeName.size() > 0) {
			// A lookup with an empty String seems to produce an empty
			// compositeName here; need to take this into account.
			return compositeName.get(0);
		}
		else {
			return "";
		}
	}

	/**
	 * Construct a new LdapName instance from the supplied Name instance. LdapName
	 * instances will be cloned, CompositeName tweaks will be managed using
	 * {@link #convertCompositeNameToString(javax.naming.CompositeName)}; for all other
	 * Name implementations, new LdapName instances are constructed using
	 * {@link LdapName#addAll(int, javax.naming.Name)}.
	 * @param name the Name instance to convert to LdapName, not <code>null</code>.
	 * @return a new LdapName representing the same Distinguished Name as the supplied
	 * instance.
	 * @throws org.springframework.ldap.InvalidNameException to wrap any
	 * InvalidNameExceptions thrown by LdapName.
	 * @since 2.0
	 */
	public static LdapName newLdapName(Name name) {
		Assert.notNull(name, "name must not be null");
		if (name instanceof LdapName) {
			return (LdapName) name.clone();
		}
		else if (name instanceof CompositeName) {
			CompositeName compositeName = (CompositeName) name;

			try {
				return new LdapName(convertCompositeNameToString(compositeName));
			}
			catch (InvalidNameException ex) {
				throw convertLdapException(ex);
			}
		}
		else {
			LdapName result = emptyLdapName();
			try {
				result.addAll(0, name);
			}
			catch (InvalidNameException ex) {
				throw convertLdapException(ex);
			}

			return result;
		}
	}

	/**
	 * Construct a new LdapName instance from the supplied distinguished name string.
	 * @param distinguishedName the string to parse for constructing an LdapName instance.
	 * @return a new LdapName instance.
	 * @throws org.springframework.ldap.InvalidNameException to wrap any
	 * InvalidNameExceptions thrown by LdapName.
	 * @since 2.0
	 */
	public static LdapName newLdapName(String distinguishedName) {
		Assert.notNull(distinguishedName, "distinguishedName must not be null");

		try {
			return new LdapName(distinguishedName);
		}
		catch (InvalidNameException ex) {
			throw convertLdapException(ex);
		}
	}

	private static LdapName returnOrConstructLdapNameFromName(Name name) {
		if (name instanceof LdapName) {
			return (LdapName) name;
		}
		else {
			return newLdapName(name);
		}
	}

	/**
	 * Remove the supplied path from the beginning the specified <code>Name</code> if the
	 * name instance starts with <code>path</code>. Useful for stripping base path suffix
	 * from a <code>Name</code>. The original Name will not be affected.
	 * @param dn the dn to strip from.
	 * @param pathToRemove the path to remove from the beginning the dn instance.
	 * @return an LdapName instance that is a copy of the original name with the specified
	 * path stripped from its beginning.
	 * @since 2.0
	 */
	public static LdapName removeFirst(Name dn, Name pathToRemove) {
		Assert.notNull(dn, "dn must not be null");
		Assert.notNull(pathToRemove, "pathToRemove must not be null");

		LdapName result = newLdapName(dn);
		LdapName path = returnOrConstructLdapNameFromName(pathToRemove);

		if (path.size() == 0 || !dn.startsWith(path)) {
			return result;
		}

		for (int i = 0; i < path.size(); i++) {
			try {
				result.remove(0);
			}
			catch (InvalidNameException ex) {
				throw convertLdapException(ex);
			}
		}

		return result;
	}

	/**
	 * Prepend the supplied path in the beginning the specified <code>Name</code> if the
	 * name instance starts with <code>path</code>. The original Name will not be
	 * affected.
	 * @param dn the dn to strip from.
	 * @param pathToPrepend the path to prepend in the beginning of the dn.
	 * @return an LdapName instance that is a copy of the original name with the specified
	 * path inserted at its beginning.
	 * @since 2.0
	 */
	public static LdapName prepend(Name dn, Name pathToPrepend) {
		Assert.notNull(dn, "dn must not be null");
		Assert.notNull(pathToPrepend, "pathToRemove must not be null");

		LdapName result = newLdapName(dn);
		try {
			result.addAll(0, pathToPrepend);
		}
		catch (InvalidNameException ex) {
			throw convertLdapException(ex);
		}

		return result;
	}

	/**
	 * Construct a new, empty LdapName instance.
	 * @return a new LdapName instance representing the empty path ("").
	 * @since 2.0
	 */
	public static LdapName emptyLdapName() {
		return newLdapName("");
	}

	/**
	 * Find the Rdn with the requested key in the supplied Name.
	 * @param name the Name in which to search for the key.
	 * @param key the attribute key to search for.
	 * @return the rdn corresponding to the <b>first</b> occurrence of the requested key.
	 * @throws NoSuchElementException if no corresponding entry is found.
	 * @since 2.0
	 */
	public static Rdn getRdn(Name name, String key) {
		Assert.notNull(name, "name must not be null");
		Assert.hasText(key, "key must not be blank");

		LdapName ldapName = returnOrConstructLdapNameFromName(name);

		List<Rdn> rdns = ldapName.getRdns();
		for (Rdn rdn : rdns) {
			NamingEnumeration<String> ids = rdn.toAttributes().getIDs();
			while (ids.hasMoreElements()) {
				String id = ids.nextElement();
				if (key.equalsIgnoreCase(id)) {
					return rdn;
				}
			}
		}

		throw new NoSuchElementException("No Rdn with the requested key: '" + key + "'");
	}

	/**
	 * Get the value of the Rdn with the requested key in the supplied Name.
	 * @param name the Name in which to search for the key.
	 * @param key the attribute key to search for.
	 * @return the value of the rdn corresponding to the <b>first</b> occurrence of the
	 * requested key.
	 * @throws NoSuchElementException if no corresponding entry is found.
	 * @since 2.0
	 */
	public static Object getValue(Name name, String key) {
		NamingEnumeration<? extends Attribute> allAttributes = getRdn(name, key).toAttributes().getAll();
		while (allAttributes.hasMoreElements()) {
			Attribute oneAttribute = allAttributes.nextElement();
			if (key.equalsIgnoreCase(oneAttribute.getID())) {
				try {
					return oneAttribute.get();
				}
				catch (javax.naming.NamingException ex) {
					throw convertLdapException(ex);
				}
			}
		}

		// This really shouldn't happen
		throw new NoSuchElementException("No Rdn with the requested key: '" + key + "'");
	}

	/**
	 * Get the value of the Rdn at the requested index in the supplied Name.
	 * @param name the Name to work on.
	 * @param index The 0-based index of the rdn value to retrieve. Must be in the range
	 * [0,size()).
	 * @return the value of the rdn at the requested index.
	 * @throws IndexOutOfBoundsException if index is outside the specified range.
	 * @since 2.0
	 */
	public static Object getValue(Name name, int index) {
		Assert.notNull(name, "name must not be null");

		LdapName ldapName = returnOrConstructLdapNameFromName(name);
		Rdn rdn = ldapName.getRdn(index);
		if (rdn.size() > 1) {
			LOGGER.warn("Rdn at position " + index + " of dn '" + name
					+ "' is multi-value - returned value is not to be trusted. "
					+ "Consider using name-based getValue method instead");
		}
		return rdn.getValue();
	}

	/**
	 * Get the value of the Rdn at the requested index in the supplied Name as a String.
	 * @param name the Name to work on.
	 * @param index The 0-based index of the rdn value to retrieve. Must be in the range
	 * [0,size()).
	 * @return the value of the rdn at the requested index as a String.
	 * @throws IndexOutOfBoundsException if index is outside the specified range.
	 * @throws ClassCastException if the value of the requested component is not a String.
	 * @since 2.0
	 */
	public static String getStringValue(Name name, int index) {
		return (String) getValue(name, index);
	}

	/**
	 * Get the value of the Rdn with the requested key in the supplied Name as a String.
	 * @param name the Name in which to search for the key.
	 * @param key the attribute key to search for.
	 * @return the String value of the rdn corresponding to the <b>first</b> occurrence of
	 * the requested key.
	 * @throws NoSuchElementException if no corresponding entry is found.
	 * @throws ClassCastException if the value of the requested component is not a String.
	 * @since 2.0
	 */
	public static String getStringValue(Name name, String key) {
		return (String) getValue(name, key);
	}

	/**
	 * Converts a binary SID to its String representation, according to the algorithm
	 * described
	 * <a href="https://blogs.msdn.com/oldnewthing/archive/2004/03/15/89753.aspx"
	 * >here</a>. Thanks to
	 * <a href= "https://www.jroller.com/eyallupu/entry/java_jndi_how_to_convert">Eyal
	 * Lupu</a> for algorithmic inspiration.
	 *
	 * <pre>
	 * If you have a SID like S-a-b-c-d-e-f-g-...
	 *
	 * Then the bytes are
	 * a	(revision)
	 * N	(number of dashes minus two)
	 * bbbbbb	(six bytes of &quot;b&quot; treated as a 48-bit number in big-endian format)
	 * cccc	(four bytes of &quot;c&quot; treated as a 32-bit number in little-endian format)
	 * dddd	(four bytes of &quot;d&quot; treated as a 32-bit number in little-endian format)
	 * eeee	(four bytes of &quot;e&quot; treated as a 32-bit number in little-endian format)
	 * ffff	(four bytes of &quot;f&quot; treated as a 32-bit number in little-endian format)
	 * etc.
	 *
	 * So for example, if your SID is S-1-5-21-2127521184-1604012920-1887927527-72713, then your raw hex SID is
	 *
	 * 010500000000000515000000A065CF7E784B9B5FE77C8770091C0100
	 *
	 * This breaks down as follows:
	 * 01	S-1
	 * 05	(seven dashes, seven minus two = 5)
	 * 000000000005	(5 = 0x000000000005, big-endian)
	 * 15000000	(21 = 0x00000015, little-endian)
	 * A065CF7E	(2127521184 = 0x7ECF65A0, little-endian)
	 * 784B9B5F	(1604012920 = 0x5F9B4B78, little-endian)
	 * E77C8770	(1887927527 = 0X70877CE7, little-endian)
	 * 091C0100	(72713 = 0x00011c09, little-endian)
	 *
	 * S-1-	version number (SID_REVISION)
	 * -5-	SECURITY_NT_AUTHORITY
	 * -21-	SECURITY_NT_NON_UNIQUE
	 * -...-...-...-	these identify the machine that issued the SID
	 * 72713	unique user id on the machine
	 * </pre>
	 * @param sid binary SID in byte array format
	 * @return String version of the given sid
	 * @since 1.3.1
	 */
	public static String convertBinarySidToString(byte[] sid) {
		// Add the 'S' prefix
		StringBuffer sidAsString = new StringBuffer("S-");

		// bytes[0] : in the array is the version (must be 1 but might
		// change in the future)
		sidAsString.append(sid[0]).append('-');

		// bytes[2..7] : the Authority
		StringBuffer sb = new StringBuffer();
		for (int t = 2; t <= 7; t++) {
			String hexString = Integer.toHexString(sid[t] & 0xFF);
			sb.append(hexString);
		}
		sidAsString.append(Long.parseLong(sb.toString(), HEX));

		// bytes[1] : the sub authorities count
		int count = sid[1];

		// bytes[8..end] : the sub authorities (these are Integers - notice
		// the endian)
		for (int i = 0; i < count; i++) {
			int currSubAuthOffset = i * 4;
			sb.setLength(0);
			sb.append(toHexString((byte) (sid[11 + currSubAuthOffset] & 0xFF)));
			sb.append(toHexString((byte) (sid[10 + currSubAuthOffset] & 0xFF)));
			sb.append(toHexString((byte) (sid[9 + currSubAuthOffset] & 0xFF)));
			sb.append(toHexString((byte) (sid[8 + currSubAuthOffset] & 0xFF)));

			sidAsString.append('-').append(Long.parseLong(sb.toString(), HEX));
		}

		// That's it - we have the SID
		return sidAsString.toString();
	}

	/**
	 * Converts a String SID to its binary representation, according to the algorithm
	 * described
	 * <a href="https://blogs.msdn.com/oldnewthing/archive/2004/03/15/89753.aspx"
	 * >here</a>.
	 * @param string SID in readable format
	 * @return Binary version of the given sid
	 * @since 1.3.1
	 * @see LdapUtils#convertBinarySidToString(byte[])
	 */
	public static byte[] convertStringSidToBinary(String string) {
		String[] parts = string.split("-");
		byte sidRevision = (byte) Integer.parseInt(parts[1]);
		int subAuthCount = parts.length - 3;

		byte[] sid = new byte[] { sidRevision, (byte) subAuthCount };
		sid = addAll(sid, numberToBytes(parts[2], 6, true));
		for (int i = 0; i < subAuthCount; i++) {
			sid = addAll(sid, numberToBytes(parts[3 + i], 4, false));
		}
		return sid;
	}

	private static byte[] addAll(byte[] array1, byte[] array2) {
		byte[] joinedArray = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		return joinedArray;
	}

	/**
	 * Converts the given number to a binary representation of the specified length and
	 * "endian-ness".
	 * @param number String with number to convert
	 * @param length How long the resulting binary array should be
	 * @param bigEndian <code>true</code> if big endian (5=0005), or <code>false</code> if
	 * little endian (5=5000)
	 * @return byte array containing the binary result in the given order
	 */
	static byte[] numberToBytes(String number, int length, boolean bigEndian) {
		BigInteger bi = new BigInteger(number);
		byte[] bytes = bi.toByteArray();
		int remaining = length - bytes.length;
		if (remaining < 0) {
			bytes = Arrays.copyOfRange(bytes, -remaining, bytes.length);
		}
		else {
			byte[] fill = new byte[remaining];
			bytes = addAll(fill, bytes);
		}
		if (!bigEndian) {
			reverse(bytes);
		}
		return bytes;
	}

	private static void reverse(byte[] array) {
		int i = 0;
		int j = array.length - 1;
		byte tmp;
		while (j > i) {
			tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	/**
	 * Converts a byte into its hexadecimal representation, padding with a leading zero to
	 * get an even number of characters.
	 * @param b value to convert
	 * @return hex string, possibly padded with a zero
	 */
	static String toHexString(final byte b) {
		String hexString = Integer.toHexString(b & 0xFF);
		if (hexString.length() % 2 != 0) {
			// Pad with 0
			hexString = "0" + hexString;
		}
		return hexString;
	}

	/**
	 * Converts a byte array into its hexadecimal representation, padding each with a
	 * leading zero to get an even number of characters.
	 * @param b values to convert
	 * @return hex string, possibly with elements padded with a zero
	 */
	static String toHexString(final byte[] b) {
		StringBuffer sb = new StringBuffer("{");
		for (int i = 0; i < b.length; i++) {
			sb.append(toHexString(b[i]));
			if (i < b.length - 1) {
				sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * An {@link AttributeValueCallbackHandler} to collect values in a supplied
	 * collection.
	 *
	 * @author Mattias Hellborg Arthursson
	 */
	private static final class CollectingAttributeValueCallbackHandler<T> implements AttributeValueCallbackHandler {

		private final Collection<T> collection;

		private final Class<T> clazz;

		CollectingAttributeValueCallbackHandler(Collection<T> collection, Class<T> clazz) {
			Assert.notNull(collection, "Collection must not be null");
			Assert.notNull(clazz, "Clazz parameter must not be null");

			this.collection = collection;
			this.clazz = clazz;
		}

		@Override
		public void handleAttributeValue(String attributeName, Object attributeValue, int index) {
			Assert.isTrue(attributeName == null || this.clazz.isAssignableFrom(attributeValue.getClass()),
					() -> "either attributeName must be null or the attribute value must be of type " + this.clazz);
			this.collection.add(this.clazz.cast(attributeValue));
		}

	}

}
