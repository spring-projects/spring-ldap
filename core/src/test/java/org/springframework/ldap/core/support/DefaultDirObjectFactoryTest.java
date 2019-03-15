/*
 * Copyright 2005-2016 the original author or authors.
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
package org.springframework.ldap.core.support;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.NameAwareAttributes;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import java.util.Hashtable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultDirObjectFactoryTest {

	private Context contextMock;

	private static final Name DN = LdapUtils.newLdapName("ou=some unit, dc=jayway, dc=se");

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
		Attributes expectedAttributes = new NameAwareAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(contextMock, DN, null,
				new Hashtable(), expectedAttributes);

		verify(contextMock).close();

		assertThat(adapter.getDn()).isEqualTo(DN);
		assertThat(adapter.getAttributes()).isEqualTo(expectedAttributes);
	}

    @Test
	public void testGetObjectInstance_CompositeName() throws Exception {
        Attributes expectedAttributes = new NameAwareAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		CompositeName name = new CompositeName();
		name.add(DN_STRING);
		
		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(contextMock, name, null,
				new Hashtable(), expectedAttributes);

        verify(contextMock).close();

		assertThat(adapter.getDn()).isEqualTo(DN);
		assertThat(adapter.getAttributes()).isEqualTo(expectedAttributes);
	}

    @Test
	public void testGetObjectInstance_nullObject() throws Exception {
		Attributes expectedAttributes = new NameAwareAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(null, DN, null, new Hashtable(),
				expectedAttributes);

		assertThat(adapter.getDn()).isEqualTo(DN);
		assertThat(adapter.getAttributes()).isEqualTo(expectedAttributes);
	}

    @Test
	public void testGetObjectInstance_ObjectNotContext() throws Exception {
		Attributes expectedAttributes = new NameAwareAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(new Object(), DN, null,
				new Hashtable(), expectedAttributes);

		assertThat(adapter.getDn()).isEqualTo(DN);
		assertThat(adapter.getAttributes()).isEqualTo(expectedAttributes);
	}

	/**
	 * Make sure that the base suffix is stripped off from the DN.
	 * 
	 * @throws Exception
	 */
    @Test
	public void testGetObjectInstance_BaseSet() throws Exception {
		Attributes expectedAttributes = new NameAwareAttributes();
		expectedAttributes.put("someAttribute", "someValue");

		when(contextMock2.getNameInNamespace()).thenReturn("dc=jayway, dc=se");

		DirContextAdapter adapter = (DirContextAdapter) tested.getObjectInstance(contextMock, LdapUtils.newLdapName(
				"ou=some unit"), contextMock2, new Hashtable(), expectedAttributes);

        verify(contextMock).close();

		assertThat(adapter.getDn().toString()).isEqualTo("ou=some unit");
		assertThat(adapter.getNameInNamespace()).isEqualTo("ou=some unit,dc=jayway,dc=se");
		assertThat(adapter.getAttributes()).isEqualTo(expectedAttributes);
	}

    @Test
	public void testConstructAdapterFromName() throws InvalidNameException {
		CompositeName name = new CompositeName();
		name.add("ldap://localhost:389/ou=People,o=JNDITutorial");
		DefaultDirObjectFactory tested = new DefaultDirObjectFactory();
		DirContextAdapter result = tested.constructAdapterFromName(new BasicAttributes(), name, "");

		assertThat(result.getDn().toString()).isEqualTo("ou=People,o=JNDITutorial");
		assertThat(result.getReferralUrl().toString()).isEqualTo("ldap://localhost:389");
	}

    @Test
	public void testConstructAdapterFromName_Ldaps() throws InvalidNameException {
		CompositeName name = new CompositeName();
		name.add("ldaps://localhost:389/ou=People,o=JNDITutorial");
		DefaultDirObjectFactory tested = new DefaultDirObjectFactory();
		DirContextAdapter result = tested.constructAdapterFromName(new BasicAttributes(), name, "");

		assertThat(result.getDn().toString()).isEqualTo("ou=People,o=JNDITutorial");
		assertThat(result.getReferralUrl().toString()).isEqualTo("ldaps://localhost:389");
	}

    @Test
	public void testConstructAdapterFromName_EmptyName() throws InvalidNameException {
		CompositeName name = new CompositeName();
		name.add("ldap://localhost:389");
		DefaultDirObjectFactory tested = new DefaultDirObjectFactory();
		DirContextAdapter result = tested.constructAdapterFromName(new BasicAttributes(), name, "");

		assertThat(result.getDn().toString()).isEqualTo("");
		assertThat(result.getReferralUrl().toString()).isEqualTo("ldap://localhost:389");
	}

    @Test
	public void testConstructAdapterFromName_OnlySlash() throws InvalidNameException {
		CompositeName name = new CompositeName();
		name.add("ldap://localhost:389/");
		DefaultDirObjectFactory tested = new DefaultDirObjectFactory();
		DirContextAdapter result = tested.constructAdapterFromName(new BasicAttributes(), name, "");

		assertThat(result.getDn().toString()).isEqualTo("");
		assertThat(result.getReferralUrl().toString()).isEqualTo("ldap://localhost:389");
	}
}
