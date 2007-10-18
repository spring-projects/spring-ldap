/*
 * Copyright 2005-2007 the original author or authors.
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
package org.springframework.ldap.core.support;

import org.springframework.ldap.core.DistinguishedName;

/**
 * Interface to be implemented by classes that want to have access to the base
 * context used in the active <code>ContextSource</code>. There are several
 * cases in which services may want to have access to the base context, e.g.
 * when working with groups (<code>groupOfNames</code> objectclass), in which
 * case the full DN of each group member needs to be specified in the attribute
 * value.
 * <p>
 * If a class implements this interface and a
 * {@link BaseLdapPathBeanPostProcessor} is defined in the
 * <code>ApplicationContext</code>, the default base path will automatically
 * passed to the {@link #setBaseLdapPath(DistinguishedName)} method on
 * initialization.
 * <p>
 * <b>NB:</b>The <code>ContextSource</code> needs to be a subclass of
 * {@link AbstractContextSource} for this mechanism to work.
 * 
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public interface BaseLdapPathAware {

	/**
	 * Set the base LDAP path specified in the current
	 * <code>ApplicationContext</code>.
	 * @param baseLdapPath the base path used in the <code>ContextSource</code>
	 */
	public void setBaseLdapPath(DistinguishedName baseLdapPath);
}
