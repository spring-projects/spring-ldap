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
package org.springframework.ldap.ldif.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Policy object for enforcing LDIF record separation rules. Designed explicitly
 * for use in LdifParser. This default separator policy should really not be
 * required to be replaced but it is modular just in case.
 * <p>
 * This class applies the separation policy prescribed in RFC2849 for LDIF files
 * and identifies the line type from the input.
 * 
 * @author Keith Barlow
 *
 */
public class SeparatorPolicy {

	private static Logger log = LoggerFactory.getLogger(SeparatorPolicy.class);
	
	/* 
	 * Line Identification Patterns.
	 */

	private static final String VERSION_IDENTIFIER = "^version: [0-9]+(\\.[0-9]*){0,1}$";
	
	private static final String CONTROL = "control:";
	
	private static final String CHANGE_TYPE = "changetype:";
	
	private static final String CONTINUATION = " ";
	
	private static final String COMMENT = "#";
	
	private static final String NEW_RECORD = "^dn:.*$";

	private boolean record = false;
	
	private boolean skip = false;
	
	public SeparatorPolicy() {
		
	}

	/**
	 * Assess a read line.
	 * <p>
	 * In LDIF, lines must adhere to a particular format. A line can only contain one attribute
	 * and its value.  The value may span multiple lines.  Continuation lines are marked by the presence
	 * of a single space in the 1st position.  Non-continuation lines must start in the first position.
	 * 
	 */
	public LineIdentifier assess(String line) {
		log.trace("Assessing --> [" + line + "]");
		
		if (record) {
			if (!StringUtils.hasLength(line)) {
				record = false;
				skip = false;
				return LineIdentifier.EndOfRecord;
				
			} else if (skip) {
				return LineIdentifier.Void;
			
			} else {
				if (line.startsWith(CONTROL)) {
					skip = true;
					return LineIdentifier.Control;
					
				} else if (line.startsWith(CHANGE_TYPE)) {
					skip = true;
					return LineIdentifier.ChangeType;
					
				} else if (line.startsWith(COMMENT)) {
					return LineIdentifier.Comment;
					
				} else if (line.startsWith(CONTINUATION)) {
					return LineIdentifier.Continuation;
					
				} else {
					return LineIdentifier.Attribute;
				
				}
			}
		} else {
			if (StringUtils.hasLength(line) && line.matches(VERSION_IDENTIFIER) && !skip) {
				//Version Identifiers are ignored by parser.
				return LineIdentifier.VersionIdentifier;		
				
			} else if (StringUtils.hasLength(line) && line.matches(NEW_RECORD)) {
				record = true;
				skip = false;
				return LineIdentifier.NewRecord;
				
			} else {
				return LineIdentifier.Void;
			}
		}
	}
}
