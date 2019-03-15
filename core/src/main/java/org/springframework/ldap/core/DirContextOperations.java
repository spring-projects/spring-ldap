/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ldap.core;

import org.springframework.LdapDataEntry;

import javax.naming.Name;
import javax.naming.directory.DirContext;

/**
 * Interface for DirContextAdapter.
 * 
 * @author Mattias Hellborg Arthursson
 * @see DirContextAdapter
 */
public interface DirContextOperations extends DirContext, LdapDataEntry,
		AttributeModificationsAware {

	/**
	 * Gets the update mode. An entry in update mode will keep track of its
	 * modifications so that they can be retrieved using
	 * {@link AttributeModificationsAware#getModificationItems()}. The update
	 * mode should be <code>true</code> for a new entry and <code>true</code>
	 * for an existing entry that is being updated.
	 * 
	 * @return update mode.
	 */
	boolean isUpdateMode();

	/**
	 * Creates a String array of the names of the attributes which have been
	 * changed.
	 * 
	 * If this is a new entry, all set entries will be in the list. If this is
	 * an updated entry, only changed and removed entries will be in the array.
	 * 
	 * @return Array of String
	 */
	String[] getNamesOfModifiedAttributes();

	/**
	 * Update the attributes.This will mean that the getters (
	 * <code>getStringAttribute</code> methods) will return the updated values,
	 * and the modifications will be forgotten (i.e.
	 * {@link AttributeModificationsAware#getModificationItems()} will return an
	 * empty array.
	 */
	void update();

	/**
	 * Set the dn of this entry.
	 * 
	 * @param dn the dn.
	 */
	void setDn(Name dn);

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.naming.Context#getNameInNamespace()
	 */
	String getNameInNamespace();

	/**
	 * If this instance results from a referral, this method returns the url of
	 * the referred server.
	 * 
	 * @return The url of the referred server, e.g.
	 * <code>ldap://localhost:389</code>, or the empty string if this is not a
	 * referral.
	 * @since 1.3
	 */
	String getReferralUrl();

	/**
	 * Checks whether this instance results from a referral.
	 * 
	 * @return <code>true</code> if this instance results from a referral,
	 * <code>false</code> otherwise.
	 * @since 1.3
	 */
	boolean isReferral();
}
