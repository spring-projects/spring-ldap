/*
 * Copyright 2005-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
