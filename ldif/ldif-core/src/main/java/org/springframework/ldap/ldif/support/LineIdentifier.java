/*
 * Copyright 2006-present the original author or authors.
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

/**
 * Enumeration declaring possible event types when parsing LDIF files.
 *
 * @author Keith Barlow
 */

public enum LineIdentifier {

	/**
	 * Every LDIF file may optionally start with a version identifier of the form
	 * 'version: 1'.
	 */
	VersionIdentifier,

	/**
	 * Signifies the start of a new record in the file has been encountered: a DN
	 * declaration.
	 */
	NewRecord,

	/**
	 * Signals the end of record has been reached.
	 */
	EndOfRecord,

	/**
	 * Signifies the event when a new attribute is encountered.
	 */
	Attribute,

	/**
	 * Indicates the current line parsed is a continuation of the previous line.
	 */
	Continuation,

	/**
	 * The current line is a comment and should be ignored.
	 */
	Comment,

	/**
	 * An LDAP changetype control was encountered.
	 */
	Control,

	/**
	 * Record being parsed is a 'changetype' record.
	 */
	ChangeType,

	/**
	 * Parsed line should be ignored - used to skip remaining lines in a 'changetype'
	 * record.
	 */
	Void

}
