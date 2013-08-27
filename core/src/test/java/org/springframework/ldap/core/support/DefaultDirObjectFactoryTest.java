/*
 * Copyright 2005-2013 the original author or authors.
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

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.directory.BasicAttributes;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultDirObjectFactoryTest {

	private Context contextMock;

	private static final Name DN = new DistinguishedName("ou=some unit, dc=jayway, dc=se");

	private static final String DN_STRING = "ou=some unit, dc=jayway, dc=se";

	private DefaultDirObjectFactory tested;

	private Context contextMock2;

    @Before
	public void setUp() throws Exception {
		contextMock = mock(Context.class);
		contextMock2 = mock(Context.class);

		tested = new DefaultDirObjectFactory();
	}

    @Test
	public void testGetObjectInstance() throws Exception {
		BasicAttributes expectedAttributes = new BasicAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(contextMock, DN, null,
				new Hashtable(), expectedAttributes);

		verify(contextMock).close();

		assertEquals(DN, adapter.getDn());
		assertEquals(expectedAttributes, adapter.getAttributes());
	}

    @Test
	public void testGetObjectInstance_CompositeName() throws Exception {
		BasicAttributes expectedAttributes = new BasicAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		CompositeName name = new CompositeName();
		name.add(DN_STRING);
		
		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(contextMock, name, null,
				new Hashtable(), expectedAttributes);

        verify(contextMock).close();

		assertEquals(DN, adapter.getDn());
		assertEquals(expectedAttributes, adapter.getAttributes());
	}

    @Test
	public void testGetObjectInstance_nullObject() throws Exception {
		BasicAttributes expectedAttributes = new BasicAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(null, DN, null, new Hashtable(),
				expectedAttributes);

		assertEquals(DN, adapter.getDn());
		assertEquals(expectedAttributes, adapter.getAttributes());
	}

    @Test
	public void testGetObjectInstance_ObjectNotContext() throws Exception {
		BasicAttributes expectedAttributes = new BasicAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(new Object(), DN, null,
				new Hashtable(), expectedAttributes);

		assertEquals(DN, adapter.getDn());
		assertEquals(expectedAttributes, adapter.getAttributes());
	}

	/**
	 * Make sure that the base suffix is stripped off from the DN.
	 * 
	 * @throws Exception
	 */
    @Test
	public void testGetObjectInstance_BaseSet() throws Exception {
		BasicAttributes expectedAttributes = new BasicAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		when(contextMock2.getNameInNamespace()).thenReturn("dc=jayway, dc=se");

		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(contextMock, new DistinguishedName(
				"ou=some unit"), contextMock2, new Hashtable(), expectedAttributes);

        verify(contextMock).close();

		assertEquals("ou=some unit", adapter.getDn().toString());
		assertEquals("ou=some unit,dc=jayway,dc=se", adapter.getNameInNamespace());
		assertEquals(expectedAttributes, adapter.getAttributes());
	}

    @Test
	public void testConstructAdapterFromName() throws InvalidNameException {
		CompositeName name = new CompositeName();
		name.add("ldap://localhost:389/ou=People,o=JNDITutorial");
		DefaultDirObjectFactory tested = new DefaultDirObjectFactory();
		DirContextAdapter result = tested.constructAdapterFromName(new BasicAttributes(), name, "");

		assertEquals("ou=People,o=JNDITutorial", result.getDn().toString());
		assertEquals("ldap://localhost:389", result.getReferralUrl().toString());
	}

    @Test
	public void testConstructAdapterFromName_Ldaps() throws InvalidNameException {
		CompositeName name = new CompositeName();
		name.add("ldaps://localhost:389/ou=People,o=JNDITutorial");
		DefaultDirObjectFactory tested = new DefaultDirObjectFactory();
		DirContextAdapter result = tested.constructAdapterFromName(new BasicAttributes(), name, "");

		assertEquals("ou=People,o=JNDITutorial", result.getDn().toString());
		assertEquals("ldaps://localhost:389", result.getReferralUrl().toString());
	}

    @Test
	public void testConstructAdapterFromName_EmptyName() throws InvalidNameException {
		CompositeName name = new CompositeName();
		name.add("ldap://localhost:389");
		DefaultDirObjectFactory tested = new DefaultDirObjectFactory();
		DirContextAdapter result = tested.constructAdapterFromName(new BasicAttributes(), name, "");

		assertEquals("", result.getDn().toString());
		assertEquals("ldap://localhost:389", result.getReferralUrl().toString());
	}

    @Test
	public void testConstructAdapterFromName_OnlySlash() throws InvalidNameException {
		CompositeName name = new CompositeName();
		name.add("ldap://localhost:389/");
		DefaultDirObjectFactory tested = new DefaultDirObjectFactory();
		DirContextAdapter result = tested.constructAdapterFromName(new BasicAttributes(), name, "");

		assertEquals("", result.getDn().toString());
		assertEquals("ldap://localhost:389", result.getReferralUrl().toString());
	}
}
