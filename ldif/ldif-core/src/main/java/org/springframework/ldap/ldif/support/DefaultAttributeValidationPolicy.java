/*
 * Copyright 2005-2021 the original author or authors.
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
package org.springframework.ldap.ldif.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.directory.Attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ldap.core.LdapAttribute;
import org.springframework.ldap.ldif.InvalidAttributeFormatException;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.util.StringUtils;

/**
 * Ensures the buffer represents a valid attribute as defined by RFC2849.
 *
 * Meets the standards imposed by RFC 2849 for the "LDAP Data Interchange Format (LDIF) -
 * Technical Specification".
 *
 * Special attention is called to URL support: RFC 2849 requires that LDIFs support URLs
 * as defined in 1738; however, RFC 1738 has been updated by several RFCs including RFC
 * 1808, RFC 2396, and RFC 3986 (which obsoleted the formers). Unsupported features of
 * this implementation of URL identification include query strings and fragments in HTTP
 * URLs.
 *
 * @author Keith Barlow
 *
 */
@SuppressWarnings("unused")
public class DefaultAttributeValidationPolicy implements AttributeValidationPolicy {

	private static Logger log = LoggerFactory.getLogger(DefaultAttributeValidationPolicy.class);

	/**
	 * Pattern Declarations.
	 */

	// General Definitions
	private static final String DIGIT = "\\p{Digit}";

	private static final String LOW_ALPHA = "\\p{Lower}";

	private static final String HIGH_ALPHA = "\\p{Upper}";

	private static final String ALPHA = "\\p{Alpha}";

	private static final String ALPHANUM = "\\p{Alnum}";

	private static final String HEX = "\\p{XDigit}";

	private static final String SAFE = "[\\x24\\x2D\\x5F\\x2E\\x2B]"; // $|-|_|.|+

	private static final String EXTRA = "[\\x21\\x2A\\x27\\x7B\\x7D\\x2C]"; // !|*|'|(|)|,

	private static final String PUNCTUATION = "[\\x3C\\x3E\\x23\\x25\\x22]"; // <|>|#|%|"

	private static final String ESCAPE = "%" + HEX + "{2}";

	private static final String RESERVED = "[\\x3B\\x2F\\x3F\\x3A\\x40\\x26\\x3D]"; // ;|/|?|:|@|&|=

	private static final String UNRESERVED = "[" + ALPHA + DIGIT + SAFE + EXTRA + "]";

	private static final String UCHAR = "(?:" + UNRESERVED + "|" + ESCAPE + ")";

	private static final String XCHAR = "(?:" + UNRESERVED + "|" + RESERVED + "|" + ESCAPE + ")";

	private static final String DIGITS = DIGIT + "+";

	// Standard LDAP Attribute Definitions
	private static final String ATTRIBUTE_SEPARATOR = ":";

	private static final String OPTION_SEPARATOR = ";";

	private static final String BASE64_INDICATOR = ":";

	private static final String URL_INDICATOR = "<";

	private static final String ATTRIBUTE_TYPE_CHARS = ALPHA + DIGIT + "-";

	private static final String LDAP_OID = "[[0-9]|[1-9][0-9]+][\\.(?:[0-9]|[1-9][0-9]+)]+";

	private static final String OPTION = "[" + ATTRIBUTE_TYPE_CHARS + "]+";

	private static final String OPTIONS = "[" + OPTION_SEPARATOR + OPTION + "]*";

	private static final String ATTRIBUTE_TYPE = LDAP_OID + "|" + ALPHANUM + "[" + ATTRIBUTE_TYPE_CHARS + "]*";

	private static final String ATTRIBUTE_DESCRIPTION = "(" + ATTRIBUTE_TYPE + ")(" + OPTIONS + ")";

	private static final String SAFE_CHAR = "[\\p{ASCII}&&[^\\x00\\x0A\\x0D]]"; // Any
																				// ASCII
																				// except
																				// NUL,
																				// LF, and
																				// CR

	private static final String SAFE_INIT_CHAR = "[\\p{ASCII}&&[^ \\x00\\x0A\\x0D\\x3A\\x3C]]"; // Any
																								// ASCII
																								// except
																								// NUL,
																								// LF,
																								// CR,
																								// SPACE,
																								// colon,
																								// and
																								// less-than

	private static final String SAFE_STRING = "(" + SAFE_INIT_CHAR + SAFE_CHAR + "*)";

	private static final String FILL = "[ ]*"; // Any number of spaces

	// BASE64 Definitions
	private static final String BASE64_CHAR = "[\\x2B\\x2F\\x30-\\x39\\x3D\\x41-\\x5A\\x61-\\x7A]"; // +,
																									// /,
																									// 0-9,
																									// -,
																									// A-Z,
																									// a-z

	private static final String BASE64_STRING = "(" + BASE64_CHAR + "*)";

	// UTF8 Definitions
	private static final String UTF8_CHAR = "[\\p{L}|[0-9]|\\s]"; // \p{L} for any kind of
																	// letter from any
																	// language, including
																	// spaces and digits

	private static final String UTF8_STRING = "(" + UTF8_CHAR + "+)";

	// URL Components
	private static final String USER = "[" + UCHAR + "\\x3B\\x3F\\x26\\x3D]*"; // UCHAR|;|?|&|=

	private static final String PASSWORD = "[" + UCHAR + "\\x3B\\x3F\\x26\\x3D]*"; // UCHAR|;|?|&|=

	private static final String DOMAINLABEL = ALPHANUM + "|" + ALPHANUM + "[" + ALPHANUM + "-]*" + ALPHANUM;

	private static final String TOPLABEL = ALPHA + "|" + ALPHA + "[" + ALPHANUM + "-]*" + ALPHANUM;

	private static final String HOSTNAME = "(?:" + DOMAINLABEL + "\\.)*" + TOPLABEL;

	private static final String IPADDRESS = "(?:" + DIGIT + "{1,3}\\.){3}" + DIGIT + "{1,3}";

	private static final String HOST = "(?:" + HOSTNAME + "|" + IPADDRESS + ")";

	private static final String PORT = DIGITS;

	private static final String HOSTPORT = HOST + "(?::" + PORT + ")?";

	private static final String URLPATH = XCHAR + "*";

	private static final String LOGIN = "(?:" + USER + "(?::" + PASSWORD + ")?@)?" + HOSTPORT;

	// URL Definitions
	private static final String SCHEME = "[" + LOW_ALPHA + DIGIT + "\\x2B\\x2D\\x2E]+";

	private static final String IP_SCHEMEPART = "//" + LOGIN + "(?:/" + URLPATH + ")?";

	private static final String SCHEMEPART = "(?:" + XCHAR + "*|" + IP_SCHEMEPART + ")";

	private static final String GENERIC_URL = SCHEME + ":" + SCHEMEPART;

	// HTTP Definition
	private static final String HSEGMENT = "[" + UCHAR + "\\x3A\\x3B\\x26\\x3D\\x40]*"; // UCHAR|:|;|&|=|@

	private static final String HPATH = HSEGMENT + "[/" + HSEGMENT + "]*";

	private static final String SEARCH = HSEGMENT;

	private static final String HTTP_URL = "http://" + HOSTPORT + "(?:/" + HPATH + "(?:\\x3F" + SEARCH + ")?)?";

	// FTP
	private static final String FSEGMENT = "[" + UCHAR + "\\x3F\\x3A\\x26\\x3D\\x40]*"; // UCHAR|?|:|&|=|@

	private static final String FPATH = FSEGMENT + "[/" + FSEGMENT + "]*";

	private static final String FTPTYPE = "[AIDaid]";

	private static final String FTP_URL = "ftp://" + LOGIN + "(?:/" + FPATH + "(?:;type=" + FTPTYPE + ")?)?";

	// NEWS
	private static final String GROUP = ALPHA + "[" + ALPHA + DIGIT + "\\x2D\\x2E\\x2B\\x5F]*"; // ALPHA
																								// [ALPHA|DIGIT|-|.|+|_]*

	private static final String ARTICLE = "[" + UCHAR + "\\x3A\\x3B\\x2F\\x3F\\x26\\x3D]@" + HOST; // [UCHAR|;|/|?|:|&|=]@HOST

	private static final String GROUPPART = "(?:\\x2A|" + GROUP + "|" + ARTICLE + ")";

	private static final String NEWS_URL = "news:" + GROUPPART;

	// NNTP
	private static final String NNTP_URL = "nntp://" + HOSTPORT + "/" + GROUP + "/" + DIGITS;

	// TELNET
	private static final String TELNET_URL = "telnet://" + LOGIN + "[/]?";

	// GOPHER
	private static final String GTYPE = XCHAR;

	private static final String SELECTOR = XCHAR + "*";

	private static final String GOPHER_STRING = XCHAR + "*";

	private static final String GOPHER_URL = "gopher://" + HOSTPORT + "(?:/(?:" + GTYPE + "(?:" + SELECTOR + "(?:%09"
			+ SEARCH + "(?:%09" + GOPHER_STRING + ")?)?)?)?)?";

	// WAIS
	private static final String WPATH = UCHAR + "*";

	private static final String WTYPE = UCHAR + "*";

	private static final String DATABASE = UCHAR + "*";

	private static final String WAIS_DOC = "wais://" + HOSTPORT + "/" + DATABASE + "/" + WTYPE + "/" + WPATH;

	private static final String WAIS_INDEX = "wais://" + HOSTPORT + "/" + DATABASE + "\\?" + SEARCH;

	private static final String WAIS_DATABASE = "wais://" + HOSTPORT + "/" + DATABASE;

	private static final String WAIS_URL = WAIS_DATABASE + "|" + WAIS_INDEX + "|" + WAIS_DOC;

	// MAILTO
	private static final String ENCODED_822_ADDR = XCHAR + "+";

	private static final String MAILTO_URL = "mailto:" + ENCODED_822_ADDR;

	// FILE
	private static final String FILE_URL = "file://(?:" + HOST + "|localhost)?/" + FPATH;

	// PROPERO
	private static final String FIELD_VALUE = "[" + UCHAR + "\\x3F\\x3A\\x40\\x26]*"; // [UCHAR|?|:|@|&]*

	private static final String FIELD_NAME = "[" + UCHAR + "\\x3F\\x3A\\x40\\x26]*"; // [UCHAR|?|:|@|&]*

	private static final String FIELD_SPEC = ";" + FIELD_NAME + "=" + FIELD_VALUE;

	private static final String PSEGMENT = "[" + UCHAR + "\\x3F\\x3A\\x40\\x26\\x3D]*"; // [UCHAR|?|:|@|&|=]*

	private static final String PPATH = PSEGMENT + "(?:/" + PSEGMENT + ")*";

	private static final String PROSPERO_URL = "prospero://" + HOSTPORT + "/" + PPATH + "(?:" + FIELD_SPEC + ")*";

	// GENERIC
	private static final String OTHER_URL = GENERIC_URL;

	private static final String URL = "((?:" + HTTP_URL + ")|(?:" + FTP_URL + ")|(?:" + NEWS_URL + ")|(?:" + NNTP_URL
			+ ")|(?:" + TELNET_URL + ")|(?:" + GOPHER_URL + ")|(?:" + WAIS_URL + ")|(?:" + MAILTO_URL + ")|(?:"
			+ FILE_URL + ")|(?:" + PROSPERO_URL + ")|(?:" + OTHER_URL + "))"; // URL
																				// Pattern

	// Expression Definitions
	private static final String ATTRIBUTE_EXPRESSION = "^" + ATTRIBUTE_DESCRIPTION + ATTRIBUTE_SEPARATOR + FILL
			+ SAFE_STRING + "{0,1}$"; // Regular Attribute

	private static final String BASE64_ATTRIBUTE_EXPRESSION = "^" + ATTRIBUTE_DESCRIPTION + ATTRIBUTE_SEPARATOR
			+ BASE64_INDICATOR + FILL + BASE64_STRING + "$"; // Base 64

	private static final String URL_ATTRIBUTE_EXPRESSION = "^" + ATTRIBUTE_DESCRIPTION + ATTRIBUTE_SEPARATOR
			+ URL_INDICATOR + FILL + URL + "$"; // URL

	private static final String UTF8_ATTRIBUTE_EXPRESSION = "^" + ATTRIBUTE_DESCRIPTION + ATTRIBUTE_SEPARATOR + FILL
			+ UTF8_STRING;

	// Pattern Declarations
	private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(ATTRIBUTE_EXPRESSION);

	private static final Pattern BASE64_ATTRIBUTE_PATTERN = Pattern.compile(BASE64_ATTRIBUTE_EXPRESSION);

	private static final Pattern URL_ATTRIBUTE_PATTERN = Pattern.compile(URL_ATTRIBUTE_EXPRESSION);

	private static final Pattern UTF8_ATTRIBUTE_PATTERN = Pattern.compile(UTF8_ATTRIBUTE_EXPRESSION);

	private boolean ordered = false;

	/**
	 * Default constructor.
	 */
	public DefaultAttributeValidationPolicy() {

	}

	/**
	 * Constructor for indicating whether or not attribute values should be ordered
	 * alphabetically.
	 * @param ordered value.
	 */
	public DefaultAttributeValidationPolicy(boolean ordered) {
		this.ordered = ordered;
	}

	/**
	 * Indicates whether or not the attribute values should be ordered alphabetically.
	 * @param ordered value.
	 */
	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	/**
	 * Validates attribute contained in the buffer and returns an LdapAttribute.
	 * <p>
	 * Ensures attributes meets one of four prescribed patterns for valid attributes:
	 * <ol>
	 * <li>A standard attribute pattern of the form: ATTR_ID[;options]: VALUE</li>
	 * <li>A Base64 attribute pattern of the form: ATTR_ID[;options]:: BASE64_VALUE</li>
	 * <li>A url attribute pattern of the form: ATTR_ID[;options]:&lt; URL_VALUE</li>
	 * <li>A UTF8 attribute pattern of the form: ATTR_ID[;options]: UTF8_VALUE</li>
	 * </ol>
	 * <p>
	 * Upon success an LdapAttribute object is returned.
	 * @param buffer {@inheritDoc}
	 * @return {@inheritDoc}
	 * @throws InvalidAttributeFormatException if the attribute does not meet one of the
	 * three patterns above or the attribute cannot be parsed.
	 */
	public Attribute parse(String buffer) {
		log.trace("Parsing --> [" + buffer + "]");

		Matcher matcher = ATTRIBUTE_PATTERN.matcher(buffer);
		if (matcher.matches()) {
			// Is a regular attribute...
			return parseStringAttribute(matcher);
		}

		matcher = BASE64_ATTRIBUTE_PATTERN.matcher(buffer);
		if (matcher.matches()) {
			// Is a base64 attribute...
			return parseBase64Attribute(matcher);
		}

		matcher = URL_ATTRIBUTE_PATTERN.matcher(buffer);
		if (matcher.matches()) {
			// Is a URL attribute...
			return parseUrlAttribute(matcher);
		}

		matcher = UTF8_ATTRIBUTE_PATTERN.matcher(buffer);
		if (matcher.matches()) {
			// Is a UTF8 attribute...
			return parseUtf8Attribute(matcher);
		}

		// default: no match.
		throw new InvalidAttributeFormatException("Not a valid attribute: [" + buffer + "]");
	}

	private LdapAttribute parseStringAttribute(Matcher matcher) {
		String id = matcher.group(1);
		String value = matcher.group(3);
		List<String> options = Arrays.asList((!StringUtils.hasLength(matcher.group(2)) ? new String[] {}
				: matcher.group(2).replaceFirst(";", "").split(OPTION_SEPARATOR)));

		if (options.isEmpty()) {
			return new LdapAttribute(id, value, this.ordered);
		}
		else {
			return new LdapAttribute(id, value, options, this.ordered);
		}
	}

	private LdapAttribute parseBase64Attribute(Matcher matcher) {
		try {
			String id = matcher.group(1);
			String value = matcher.group(3);
			List<String> options = Arrays.asList((StringUtils.isEmpty(matcher.group(2)) ? new String[] {}
					: matcher.group(2).replaceFirst(";", "").split(OPTION_SEPARATOR)));

			if (options.isEmpty()) {
				return new LdapAttribute(id, LdapEncoder.parseBase64Binary(value), this.ordered);
			}
			else {
				return new LdapAttribute(id, LdapEncoder.parseBase64Binary(value), options, this.ordered);
			}
		}
		catch (IllegalArgumentException e) {
			throw new InvalidAttributeFormatException(e);
		}
	}

	private LdapAttribute parseUrlAttribute(Matcher matcher) {
		try {
			String id = matcher.group(1);
			String value = matcher.group(3);
			List<String> options = Arrays.asList((StringUtils.isEmpty(matcher.group(2)) ? new String[] {}
					: matcher.group(2).replaceFirst(";", "").split(OPTION_SEPARATOR)));

			if (options.isEmpty()) {
				return new LdapAttribute(id, new URI(value), this.ordered);
			}
			else {
				return new LdapAttribute(id, new URI(value), options, this.ordered);
			}
		}
		catch (URISyntaxException e) {
			throw new InvalidAttributeFormatException(e);
		}
	}

	private LdapAttribute parseUtf8Attribute(Matcher matcher) {
		String id = matcher.group(1);
		String value = matcher.group(3);
		List<String> options = Arrays.asList((!StringUtils.hasLength(matcher.group(2)) ? new String[] {}
				: matcher.group(2).replaceFirst(";", "").split(OPTION_SEPARATOR)));

		if (options.isEmpty()) {
			return new LdapAttribute(id, value, this.ordered);
		}
		else {
			return new LdapAttribute(id, value, options, this.ordered);
		}
	}

}
