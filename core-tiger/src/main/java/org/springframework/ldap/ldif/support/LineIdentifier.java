package org.springframework.ldap.ldif.support;

/**
 * Enumeration declaring possible event types when parsing LDIF files.
 *  
 * @author Keith Barlow
 */

public enum LineIdentifier {
	/**
	 * Every LDIF file may optionally start with a version identifier of the form 'version: 1'.
	 */
	 VersionIdentifier, 
	 
	 /**
	  * Signifies the start of a new record in the file has been encountered: a DN declaration.
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
	   * Parsed line should be ignored - used to skip remaining lines in a 'changetype' record.
	   */
	  Void
}
