package org.springframework.ldap.ldif.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.ldif.InvalidAttributeFormatException;
import org.springframework.ldap.ldif.InvalidRecordFormatException;
import org.springframework.ldap.ldif.LdapAttributes;
import org.springframework.ldap.ldif.support.AttributeValidationPolicy;
import org.springframework.ldap.ldif.support.DefaultAttributeValidationPolicy;
import org.springframework.ldap.ldif.support.LineIdentifier;
import org.springframework.ldap.ldif.support.SeparatorPolicy;
import org.springframework.ldap.schema.DefaultSchemaSpecification;
import org.springframework.ldap.schema.Specification;
import org.springframework.util.Assert;

/**
 * The {@link LDIFParser LDIFParser} is the main class of the {@link org.springframework.ldap.ldif} package.  
 * This class reads lines from a resource and assembles them into an {@link LdapAttributes LdapAttributes} object.
 * The {@link LDIFParser LDIFParser} does ignores <i>changetype</i> LDIF entries as their usefulness in the 
 * context of an application has yet to be determined.
 * <p>
 * <b>Design</b><br/>
 * {@link LDIFPaser LDIFParser} provides the main interface for operation but requires three supporting classes to 
 * enable operation:
 * <ul>
 * 	<li>{@link SeparatorPolicy SeparatorPolicy} - establishes the mechanism by which lines are assembled into attributes.</li>
 * 	<li>{@link AttributeValidationPolicy AttributeValidationPolicy} - ensures that attributes are correctly structured prior to parsing.</li>
 * 	<li>{@link Specification Specification} - provides a mechanism by which object structure can be validated after assembly.</li>
 * </ul>
 * Together, these 4 classes read from the resource line by line and translate the data into objects for use.
 * <p>
 * <b>Usage</b><br/>
 * {@link #getRecord() getRecord()} reads the next available record from the resource.  Lines are read and 
 * passed to the {@link SeparatorPolicy SeparatorPolicy} for interpretation.  The parser continues to read
 * lines and appends them to the buffer until it encounters the start of a new attribute or an end of record
 * delimiter.  When the new attribute or end of record is encountered, the buffer is passed to the 
 * {@link AttributeValidationPolicy AttributeValidationPolicy} which ensures the buffer conforms to a valid 
 * attribute definition as defined in RFC2849 and returns an {@link org.springframework.ldap.ldif.LdapAttribute LdapAttribute} object 
 * which is then added to the record, an {@link LdapAttributes LdapAttributes} object.  Upon encountering the 
 * end of record, the record is validated by the {@link Specification Specification} policy and, 
 * if valid, returned to the requester.  
 * <p>
 * The parser requires the resource to be {@link open() open()} prior to an invocation of {@link #getRecord() getRecord()}.  
 * {@link #hasMoreRecords() hasMoreRecords()} can be used to loop over the resource until all records have been 
 * retrieved.  Likewise, the {@link #reset() reset()} method will reset the resource.
 * 
 * @author Keith Barlow
 *
 */
public class LDIFParser implements Parser {

	private static final Log log = LogFactory.getLog(LDIFParser.class);
	
	/**
	 * The resource to parse.
	 */
	private Resource resource;
	
	/**
	 * A BufferedReader to read the file.
	 */
	private BufferedReader reader;
	
	/**
	 * The SeparatorPolicy to use for interpreting attributes from the lines of the resource.
	 */
	private SeparatorPolicy separatorPolicy = new SeparatorPolicy();

	/**
	 * The AttributeValidationPolicy to use to interpret attributes.
	 */
	private AttributeValidationPolicy attributePolicy = new DefaultAttributeValidationPolicy();
	
	/**
	 * The RecordSpecification for validating records produced.
	 */
	private Specification<LdapAttributes> recordSpecification = new DefaultSchemaSpecification();
	
	/**
	 * Default constructor.
	 */
	public LDIFParser() {
		
	}
	
	/**
	 * Convenience constructor for resource specification.
	 * 
	 * @param resource The resource to parse.
	 */
	public LDIFParser(Resource resource) {
		this.resource = resource;
	}
	
	/**
	 * Convenience constructor: accepts a File object.
	 * 
	 * @param file The file to parse.
	 */
	public LDIFParser(File file) {
		this.resource = new FileSystemResource(file);
	}
	
	/**
	 * Set the separator policy.
	 * 
	 * The default separator policy should suffice for most needs.
	 * 
	 * @param separatorPolicy Separator policy.
	 */
	public void setSeparatorPolicy(SeparatorPolicy separatorPolicy) {
		this.separatorPolicy = separatorPolicy;
	}
	
	/**
	 * Policy object enforcing the rules for acceptable attributes.
	 * 
	 * @param avPolicy Attribute validation policy.
	 */
	public void setAttributeValidationPolicy(AttributeValidationPolicy avPolicy) {
		this.attributePolicy = avPolicy;
	}
	
	/**
	 * Policy object for enforcing rules to acceptable LDAP objects.
	 * 
	 * This policy may be used to enforce schema restrictions.
	 * @param specification
	 */
	public void setRecordSpecification(Specification<LdapAttributes> specification) {
		this.recordSpecification = specification;
	}

	public void setResource(Resource resource) {
		this.resource = resource;		
	}

	public void afterPropertiesSet() throws Exception {
				
	}

	public void open() throws IOException {
		Assert.notNull(resource, "Resource must be set.");
		Assert.isTrue(resource.exists(), resource.getDescription() + ": resource does not exist!");		
		Assert.isTrue(resource.isReadable(), "Resource is not readable.");

		reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		
	}

	public void close() throws IOException {
	
		if (resource.isOpen())
			reader.close();
	}

	public void reset() throws IOException {
		Assert.notNull(reader, "A reader has not been obtained.");
		reader.reset();
	}
	
	public boolean hasMoreRecords() throws IOException {
		return reader.ready();
	}
	
	public LdapAttributes getRecord() throws IOException {
		Assert.notNull(reader, "A reader must be obtained: parser not open.");
		
		if (!reader.ready()) return null;
		
		LdapAttributes record = new LdapAttributes();
		StringBuilder builder = new StringBuilder();
		
		String line = reader.readLine();
		
		while(true) {			
						
			LineIdentifier identifier = separatorPolicy.assess(line);
			
			switch(identifier) {
				case NewRecord:
					log.trace("Starting new record.");
					//Start new record.
					builder = new StringBuilder(line);
					
					break;
				
				case Control:
					log.trace("'control' encountered.");
					
					//Log WARN and discard record.
					log.warn("LDIF change records have no implementation: record will be ignored.");					
					builder = null;
					record = null;
					
					break;
					
				case ChangeType:
					log.trace("'changetype' encountered.");
					
					//Log WARN and discard record.
					log.warn("LDIF change records have no implementation: record will be ignored.");					
					builder = null;
					record = null;
					
					break;
					
				case Attribute:
					//flush buffer.
					addAttributeToRecord(builder.toString(), record);

					log.trace("Starting new attribute.");
					//Start new attribute.
					builder = new StringBuilder(line);	
					
					break;
					
				case Continuation:
					log.trace("...appending line to buffer.");
					//Append line to buffer.
					builder.append(line.replaceFirst(" ", ""));
					
					break;					
				
				case EndOfRecord:
					log.trace("...done parsing record. (EndOfRecord)");
					
					//Validate record and return.
					if (record == null) return null;
					else {
						try {
							//flush buffer.
							addAttributeToRecord(builder.toString(), record);
							
							log.debug("satisfied: " + recordSpecification.isSatisfiedBy(record));
							
							if (recordSpecification.isSatisfiedBy(record)) {
								log.trace("Returning record.");
								return record;
								
							} else {
								String dn;
								
								try {
									dn = (String) record.get("dn").get();
								} catch (NamingException e) {
									dn = "";
								}
								
								throw new InvalidRecordFormatException("Record [dn: " + dn + "] does not conform to specification.");
							}
						} catch(NamingException e) {
							log.error(e);
							return null;
						}
					}
					
				default:
					//Take no action -- applies to VersionIdentifier, Comments, and voided records.
			}
			
			line = reader.readLine();
			
		}

	}
	
	private void addAttributeToRecord(String buffer, LdapAttributes record) {
		try {
			if (StringUtils.isNotEmpty(buffer) && record != null) {
				//Validate previous attribute and add to record.
				Attribute attribute = attributePolicy.parse(buffer);
					
				if (attribute.getID().equalsIgnoreCase("dn")) {
					log.trace("...adding DN to record.");
					
					String dn;
					if (attribute.get() instanceof byte[]) {
						dn = new String((byte[]) attribute.get());
					} else {
						dn = (String) attribute.get();
					}

					record.setDN(new DistinguishedName(dn));
					
				} else {
					log.trace("...adding attribute to record.");
					Attribute attr = record.get(attribute.getID());
					
					if (attr != null) {
						attr.add(attribute.get());
					} else {
						record.put(attribute);
					}
				}
			}			
		} catch (NamingException e) {
			log.error(e);
		} catch (NoSuchElementException e) {
			log.error(e);
		} catch (InvalidAttributeFormatException e) {
			log.error(e);
			record = null;
		}
	}
		
}
