package org.springframework.ldap.schema;

import javax.naming.NamingException;

import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.core.LdapRdn;

import sun.misc.BASE64Encoder;

/**
 * DefaultSchemaSpecification establishes a minimal set of requirements for object classes.
 * <p>
 * The default specification, which does not validate against any schema, simply deems all
 * objects valid as long as they have the following:
 * <ul>
 * 	<li>a non-null DN</li>
 * 	<li>a matching naming attribute class definition.</li>
 * 	<li>and an object</li>
 * </ul> 
 * 
 * @author Keith Barlow
 */
public class DefaultSchemaSpecification implements Specification<LdapAttributes> {

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
				
				//objectclass definition is required.
				if (record.get("objectclass") != null) {
					
					//Naming attribute is required.
					LdapRdn rdn = dn.getLdapRdn(dn.size() - 1);				
					if (record.get(rdn.getKey()) != null) {
						Object object = record.get(rdn.getKey()).get();
						
						if (object instanceof String) {
							String value = (String) record.get(rdn.getKey()).get();
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
