package org.springframework.ldap.schema;

import javax.naming.NamingException;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.core.LdapRdn;

import sun.misc.BASE64Encoder;

/**
 * BasicSchemaSpecification establishes a minimal set of requirements for object classes.
 * <p>
 * This basic specification, which does not actually validate against any schema, deems objects
 * valid as long as they meet the following criteria:
 * <ul>
 * 	<li>the object has a non-null DN.</li>
 * 	<li>the object contains the naming attribute declared by the DN.</li>
 * 	<li>the object declares an objectClass.</li>
 * </ul> 
 * 
 * @author Keith Barlow
 *
 */
public class BasicSchemaSpecification implements Specification<LdapAttributes> {

	/**
	 * Determines if the policy is satisfied by the supplied LdapAttributes object.
	 * 
	 * @throws NamingException 
	 */	
	public boolean isSatisfiedBy(LdapAttributes record) throws NamingException {
		if (record != null) {
			
			//DN is required.
			DistinguishedName dn = record.getDN();
			if (dn != null) {
				
				//objectClass definition is required.
				if (record.get("objectClass") != null) {
					
					//Naming attribute is required.
					LdapRdn rdn = dn.getLdapRdn(dn.size() - 1);				
					if (record.get(rdn.getKey()) != null) {
						Object object = record.get(rdn.getKey()).get();
						
						if (object instanceof String) {
							String value = (String) object;
							if (rdn.getValue().equalsIgnoreCase(value)) {
								return true;
							}
						} else if(object instanceof byte[]) {
							BASE64Encoder encoder = new BASE64Encoder();
							String rdnValue = encoder.encode(rdn.getValue().getBytes());
							String attributeValue = encoder.encode((byte[]) object);
							if (rdnValue.equals(attributeValue)) return true;
						} 
					}
				}
			}
		}
		
		return false;
	}

}
