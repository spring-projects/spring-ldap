/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.schema;

import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.support.LdapEncoder;

/**
 * BasicSchemaSpecification establishes a minimal set of requirements for object classes.
 * <p>
 * This basic specification, which does not actually validate against any schema, deems
 * objects valid as long as they meet the following criteria:
 * <ul>
 * <li>the object has a non-null DN.</li>
 * <li>the object contains the naming attribute declared by the DN.</li>
 * <li>the object declares an objectClass.</li>
 * </ul>
 *
 * @author Keith Barlow
 *
 */
public class BasicSchemaSpecification implements Specification<LdapAttributes> {

	/**
	 * Determines if the policy is satisfied by the supplied LdapAttributes object.
	 * @throws NamingException
	 */
	public boolean isSatisfiedBy(LdapAttributes record) throws NamingException {
		if (record == null) {
			return false;
		}

		// DN is required.
		LdapName dn = record.getName();
		if (dn == null) {
			return false;
		}

		// objectClass definition is required.
		if (record.get("objectClass") == null) {
			return false;
		}

		// Naming attribute is required.
		Rdn rdn = dn.getRdn(dn.size() - 1);
		if (record.get(rdn.getType()) == null) {
			return false;
		}

		Object object = record.get(rdn.getType()).get();

		if (object instanceof String) {
			String value = (String) object;
			return ((String) rdn.getValue()).equalsIgnoreCase(value);
		}
		else if (object instanceof byte[]) {
			String rdnValue = LdapEncoder.printBase64Binary(((String) rdn.getValue()).getBytes());
			String attributeValue = LdapEncoder.printBase64Binary((byte[]) object);
			return rdnValue.equals(attributeValue);
		}

		return false;
	}

}
