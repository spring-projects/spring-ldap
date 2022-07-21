package org.springframework.ldap.schema;

import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.support.LdapEncoder;

import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

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
			LdapName dn = record.getName();
			if (dn != null) {
				
				//objectClass definition is required.
				if (record.get("objectClass") != null) {
					
					//Naming attribute is required.
					Rdn rdn = dn.getRdn(dn.size() - 1);
					if (record.get(rdn.getType()) != null) {
						Object object = record.get(rdn.getType()).get();
						
						if (object instanceof String) {
							String value = (String) object;
							if (((String)rdn.getValue()).equalsIgnoreCase(value)) {
								return true;
							}
						} else if(object instanceof byte[]) {
							String rdnValue = LdapEncoder.printBase64Binary(((String)rdn.getValue()).getBytes());
							String attributeValue = LdapEncoder.printBase64Binary((byte[]) object);
							if (rdnValue.equals(attributeValue)) return true;
						} 
					}
				}
			}
		}
		
		return false;
	}

}
