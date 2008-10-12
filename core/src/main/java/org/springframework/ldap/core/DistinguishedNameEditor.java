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
package org.springframework.ldap.core;

import java.beans.PropertyEditorSupport;

/**
 * Property editor for use with {@link DistinguishedName} instances. The
 * {@link #setAsText(String)} method sets the value as an <i>immutable</i>
 * instance of a DistinguishedName.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public class DistinguishedNameEditor extends PropertyEditorSupport {

	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		if (text == null) {
			setValue(null);
		}
		else {
			setValue(new DistinguishedName(text).immutableDistinguishedName());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	public String getAsText() {
		Object theValue = getValue();
		if (theValue == null) {
			return null;
		}
		else {
			return ((DistinguishedName) theValue).toString();
		}
	}

}
