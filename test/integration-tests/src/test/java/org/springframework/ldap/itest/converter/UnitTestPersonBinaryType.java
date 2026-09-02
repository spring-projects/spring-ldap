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

package org.springframework.ldap.itest.converter;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Attribute.Type;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

/**
 * @author Ngoc Nhan
 */
@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" })
public class UnitTestPersonBinaryType {

	@Id
	private Name dn;

	@Attribute(name = "testBytes", type = Type.BINARY)
	private byte[] testBytes;

	@Attribute(name = "testString", type = Type.BINARY)
	private String testString;

	public Name getDn() {
		return this.dn;
	}

	public void setDn(Name dn) {
		this.dn = dn;
	}

	public byte[] getTestBytes() {
		return this.testBytes;
	}

	public void setTestBytes(byte[] testBytes) {
		this.testBytes = testBytes;
	}

	public String getTestString() {
		return this.testString;
	}

	public void setTestString(String testString) {
		this.testString = testString;
	}

}
