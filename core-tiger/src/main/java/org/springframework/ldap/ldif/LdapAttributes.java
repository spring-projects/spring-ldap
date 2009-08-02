/**
 * Extends BasicAttributes adding support for DNs.
 */
package org.springframework.ldap.ldif;

import java.net.URI;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttributes;

import org.springframework.ldap.core.DistinguishedName;

import sun.misc.BASE64Encoder;

/**
 * Extends {@link javax.naming.directory.BasicAttributes} to add specialized support
 * for DNs.
 * <p>
 * While DNs appear to be and can be treated as attributes, they have a special
 * meaning in that they define the address to which the object is bound.  DNs must
 * conform to special formating rules and are typically required to be handled
 * separately from other attributes.
 * <p>
 * This class makes this distinction between the DN and other
 * attributes prominent and apparent.
 * 
 * @author Keith Barlow
 *
 */
public class LdapAttributes extends BasicAttributes {

	private static final long serialVersionUID = 97903297123869138L;

	private static final String SAFE_CHAR = "[\\p{ASCII}&&[^\\x00\\x0A\\x0D]]"; //Any ASCII except NUL, LF, and CR
	
	private static final String SAFE_INIT_CHAR = "[\\p{ASCII}&&[^ \\x00\\x0A\\x0D\\x3A\\x3C]]"; //Any ASCII except NUL, LF, CR, SPACE, colon, and less-than
	
	/**
	 * Distinguished name to which the object is bound.
	 */
	protected DistinguishedName dn = new DistinguishedName();
	
	/**
	 * Default constructor.
	 */
	public LdapAttributes() {
		
	}

	/**
	 * Creates an LdapAttributes object with the specified DN.
	 * 
	 * @param dn The {@link org.springframework.ldap.core.DistinguishedName} to which this object is bound.
	 */
	public LdapAttributes(DistinguishedName dn) {
		super();
		this.dn = dn;
	}

	/**
	 * Constructor for specifying whether or not the object is case sensitive.
	 * 
	 * @param ignoreCase {@link java.lang.boolean} indicator.
	 */
	public LdapAttributes(boolean ignoreCase) {
		super(ignoreCase);
	}

	/**
	 * Creates an LdapAttributes object with the specified DN and case sensitivity setting.
	 * 
	 * @param dn The {@link org.springframework.ldap.core.DistinguishedName} to which this object is bound.
	 * @param ignoreCase {@link java.lang.boolean} indicator.
	 */
	public LdapAttributes(DistinguishedName dn, boolean ignoreCase) {
		super(ignoreCase);
		this.dn = dn;
	}

	/**
	 * Creates an LdapAttributes object with the specified attribute.
	 * 
	 * @param attrID {@link java.lang.String} ID of the attribute.
	 * @param val Value of the attribute.
	 */
	public LdapAttributes(String attrID, Object val) {
		put(new LdapAttribute(attrID, val));
	}

	/**
	 * Creates an LdapAttributes object with the specifying attribute and value and case sensitivity setting.
	 * 
	 * @param dn The {@link org.springframework.ldap.core.DistinguishedName} to which this object is bound.
	 * @param attrID {@link java.lang.String} ID of the attribute.
	 * @param val Value of the attribute.
	 */
	public LdapAttributes(DistinguishedName dn, String attrID, Object val) {
		this.dn = dn;
		put(new LdapAttribute(attrID, val));
	}

	/**
	 * Creates an LdapAttributes object with the specifying attribute and value and case sensitivity setting.
	 * 
	 * @param attrID {@link java.lang.String} ID of the attribute.
	 * @param val Value of the attribute.
	 * @param ignoreCase {@link java.lang.boolean} indicator.
	 */
	public LdapAttributes(String attrID, Object val, boolean ignoreCase) {
		put(new LdapAttribute(attrID, val, ignoreCase));
	}

	/**
	 * Creates an LdapAttributes object for the supplied DN with the attribute specified.
	 * 
	 * @param dn The {@link org.springframework.ldap.core.DistinguishedName} to which this object is bound.
	 * @param attrID {@link java.lang.String} ID of the attribute.
	 * @param val Value of the attribute.
	 * @param ignoreCase {@link java.lang.boolean} indicator.
	 */
	public LdapAttributes(DistinguishedName dn, String attrID, Object val, boolean ignoreCase) {
		this.dn = dn;
		put(new LdapAttribute(attrID, val, ignoreCase));
	}
	
	/**
	 * Returns the distinguished name to which the object is bound.
	 * 
	 * @return {@link org.springframework.ldap.core.DistinguishedName} specifying the name to which the object is bound.
	 */
	public DistinguishedName getDN() {
		return dn;
	}
	
	/**
	 * Sets the distinguished name of the object.
	 * 
	 * @param dn {@link org.springframework.ldap.core.DistinguishedName} specifying the name to which the object is bound.
	 */
	public void setDN(DistinguishedName dn) {
		this.dn = dn;
	}
	
	/**
	 * Returns a string representation of the object in LDIF format.
	 * 
	 * @return {@link java.lang.String} formated to RFC2849 LDIF specifications.
	 */
	public String toString() {
		try {
			StringBuilder  sb = new StringBuilder();
			
			DistinguishedName dn = getDN();
			
			if (!dn.toString().matches(SAFE_INIT_CHAR + SAFE_CHAR + "*")) {
				sb.append("dn:: " + new BASE64Encoder().encode(dn.toString().getBytes()) + "\n");
			} else {
				sb.append("dn: " + getDN() + "\n");
			}
			
			NamingEnumeration<Attribute> attributes = getAll();
			
			while (attributes.hasMore()) {
				Attribute attribute = attributes.next();
				NamingEnumeration<?> values = attribute.getAll();
				
				while (values.hasMore()) {
					Object value = values.next();
					
					if (value instanceof String)
						sb.append(attribute.getID() + ": " + (String) value + "\n");
					
					else if (value instanceof byte[])
						sb.append(attribute.getID() + ":: " + new BASE64Encoder().encode((byte[]) value) + "\n");
					
					else if (value instanceof URI)
						sb.append(attribute.getID() + ":< " + (URI) value + "\n");
					
					else {
						sb.append(attribute.getID() + ": " + value + "\n");
					}
				}
			}
			
			return sb.toString();
			
		} catch (NamingException e) {
			e.printStackTrace();
			return "";
		}
	}
}
