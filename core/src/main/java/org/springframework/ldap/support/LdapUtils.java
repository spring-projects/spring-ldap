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

import java.math.BigInteger;
import java.util.Collection;

import javax.naming.CompositeName;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.ArrayUtils;
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

	/**
	 * Converts a CompositeName to a String in a way that avoids escaping
	 * problems, such as the dreaded "triple backslash" problem.
	 * 
	 * @param compositeName The CompositeName to convert
	 * @return String containing the String representation of <code>name</code>
	 */
	public static String convertCompositeNameToString(
			CompositeName compositeName) {
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
	 * Converts a binary SID to its String representation, according to the
	 * algorithm described <a
	 * href="http://blogs.msdn.com/oldnewthing/archive/2004/03/15/89753.aspx"
	 * >here</a>. Thanks to <a href=
	 * "http://www.jroller.com/eyallupu/entry/java_jndi_how_to_convert">Eyal
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
	 * 
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
		sidAsString.append(Long.parseLong(sb.toString(), 16));

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

			sidAsString.append('-').append(Long.parseLong(sb.toString(), 16));
		}

		// That's it - we have the SID
		return sidAsString.toString();
	}
	
	/**
	 * Converts a String SID to its binary representation, according to the
	 * algorithm described <a
	 * href="http://blogs.msdn.com/oldnewthing/archive/2004/03/15/89753.aspx"
	 * >here</a>. 
	 * 
	 * @param string SID in readable format
	 * @return Binary version of the given sid
	 * @see LdapUtils#convertBinarySidToString(byte[])
	 * @since 1.3.1
	 */
	public static byte[] convertStringSidToBinary(String string) {
		String[] parts = string.split("-");
		byte sidRevision = (byte) Integer.parseInt(parts[1]);
		int subAuthCount = parts.length - 3;
		
		byte[] sid = new byte[] {sidRevision, (byte) subAuthCount};
		sid = ArrayUtils.addAll(sid, numberToBytes(parts[2], 6, true));
		for (int i = 0; i < subAuthCount; i++) {
			sid = ArrayUtils.addAll(sid, numberToBytes(parts[3 + i], 4, false));
		}
		return sid;
	}

	/**
	 * Converts the given number to a binary representation of the specified
	 * length and "endian-ness".
	 * 
	 * @param number String with number to convert
	 * @param length How long the resulting binary array should be
	 * @param bigEndian <code>true</code> if big endian (5=0005), or
	 * <code>false</code> if little endian (5=5000)
	 * @return byte array containing the binary result in the given order
	 */
	static byte[] numberToBytes(String number, int length, boolean bigEndian) {
		BigInteger bi = new BigInteger(number);
		byte[] bytes = bi.toByteArray();
		int remaining = length - bytes.length;
		if (remaining < 0) {
			bytes = ArrayUtils.subarray(bytes, -remaining, bytes.length);
		} else {
			byte[] fill = new byte[remaining];
			bytes = ArrayUtils.addAll(fill, bytes);
		}
		if (!bigEndian) {
			ArrayUtils.reverse(bytes);
		}
		return bytes;
	}
	
	/**
	 * Converts a byte into its hexadecimal representation, padding with a
	 * leading zero to get an even number of characters.
	 * 
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
	 * Converts a byte array into its hexadecimal representation, padding each
	 * with a leading zero to get an even number of characters.
	 * 
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
}
