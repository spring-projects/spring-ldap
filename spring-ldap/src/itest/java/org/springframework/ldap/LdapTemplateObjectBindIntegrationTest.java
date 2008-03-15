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

package org.springframework.ldap;

import java.io.Serializable;
import java.util.LinkedList;

import javax.naming.directory.DirContext;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextSource;

/**
 * Tests bind and lookup with Java objects where the ContextSource has a
 * <code>null</code> DirObjectFactory configured.
 * 
 * @author Ulrik Sandberg
 */
public class LdapTemplateObjectBindIntegrationTest extends AbstractLdapTemplateIntegrationTest {

	private LdapTemplate tested;

	protected String[] getConfigLocations() {
		return new String[] { "/conf/ldapTemplateObjectBindTestContext.xml" };
	}

	public void testBindJavaObject() throws Exception {
		tested.bind("cn=myRandomInt", new Integer(54321), null);

		Integer result = (Integer) tested.lookup("cn=myRandomInt");
		assertEquals(54321, result.intValue());
	}

	public void testBindLinkedList() {
		LinkedList list = new LinkedList();
		list.add(new Integer(54321));
		list.add(new Integer(67890));

		tested.bind("cn=myRandomList", list, null);

		LinkedList result = (LinkedList) tested.lookup("cn=myRandomList");
		assertEquals(2, result.size());
		assertEquals(54321, ((Integer) result.get(0)).intValue());
		assertEquals(67890, ((Integer) result.get(1)).intValue());
	}

	public void testBindNonSerializableJavaObjectShouldFail() throws Exception {
		NonSerializablePojo pojo = new NonSerializablePojo();
		pojo.setName("A Name");

		try {
			tested.bind("cn=myRandomObject", pojo, null);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException expected) {
			assertEquals("can only bind Referenceable, Serializable, DirContext", expected.getMessage());
		}
	}

	/**
	 * Custom non-serializable class used for demonstrating bind and lookup of
	 * Java objects.
	 */
	public static class NonSerializablePojo {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public void testBindSerializableJavaObjectShouldSucceed() throws Exception {
		SerializablePojo pojo = new SerializablePojo();
		pojo.setName("A Name");

		tested.bind("cn=myRandomObject", pojo, null);

		SerializablePojo result = (SerializablePojo) tested.lookup("cn=myRandomObject");
		assertEquals("A Name", result.getName());
	}

	/**
	 * Custom serializable class used for demonstrating bind and lookup of Java
	 * objects.
	 */
	public static class SerializablePojo implements Serializable {
		private static final long serialVersionUID = 3655768927093734908L;

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	/**
	 * This test demonstrates that it's fully possible to perform plain JNDI
	 * operations from Spring LDAP.
	 */
	public void testPlainJndiBindJavaObject() throws Exception {
		AbstractContextSource contextSource = (AbstractContextSource) tested.getContextSource();
		DirContext ctx = contextSource.getReadWriteContext();

		ctx.bind("cn=myRandomInt", new Integer(28420));

		Integer result = (Integer) ctx.lookup("cn=myRandomInt");
		assertEquals(28420, result.intValue());
	}

	public void setTested(LdapTemplate tested) {
		this.tested = tested;
	}
}
