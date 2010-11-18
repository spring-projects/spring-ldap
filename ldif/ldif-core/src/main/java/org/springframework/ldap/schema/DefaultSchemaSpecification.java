/*
 * Copyright 2005-2010 the original author or authors.
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

import org.springframework.ldap.core.LdapAttributes;

/**
 * DefaultSchemaSpecification does not validate objects at all - it simply returns true.
 * <p>
 * This specification is intended for cases where validation of the parsed entries is not
 * required.
 * 
 * @author Keith Barlow
 *
 */
public class DefaultSchemaSpecification implements Specification<LdapAttributes> {

	/**
	 * Determines if the policy is satisfied by the supplied LdapAttributes object.
	 * 
	 * @throws NamingException 
	 */	
	public boolean isSatisfiedBy(LdapAttributes record) throws NamingException {
		return true;
	}

}
