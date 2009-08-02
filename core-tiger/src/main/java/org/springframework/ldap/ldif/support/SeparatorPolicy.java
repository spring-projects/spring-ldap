/**
 * Policy object for enforcing LDIF record separation rules. Designed explicitly for use in LDIFParser.
 */
package org.springframework.ldap.ldif.support;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This default separator policy should really not be required to be
 * replaced but it is modular just in case.
 * <p>
 * This class applies the separation policy prescribed in RFC2849 
 * for LDIF files and identifies the line type from the input.
 * 
 * @author Keith Barlow
 *
 */
public class SeparatorPolicy {

	private static Log log = LogFactory.getLog(SeparatorPolicy.class);
	
	/* 
	 * Line Identification Patterns.
	 */

	private static final String VERSION_IDENTIFIER = "^version: [0-9]+(\\.[0-9]*){0,1}$";
	
	private static final String CONTROL = "control:";
	
	private static final String CHANGE_TYPE = "changetype:";
	
	private static final String CONTINUATION = " ";
	
	private static final String COMMENT = "#";
	
	private static final String NewRecord = "^dn:.*$";

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
			if (StringUtils.isEmpty(line)) {
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
			if (StringUtils.isNotEmpty(line) && line.matches(VERSION_IDENTIFIER) && !skip) {
				//Version Identifiers are ignored by parser.
				return LineIdentifier.VersionIdentifier;		
				
			} else if (StringUtils.isNotEmpty(line) && line.matches(NewRecord)) {
				record = true;
				skip = false;
				return LineIdentifier.NewRecord;
				
			} else {
				return LineIdentifier.Void;
			}
		}
	}
}
