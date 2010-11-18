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

package org.springframework.ldap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

import javax.naming.directory.DirContext;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextSource;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests bind and lookup with Java objects where the ContextSource has a
 * <code>null</code> DirObjectFactory configured.
 * 
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateObjectBindTestContext.xml" })
public class LdapTemplateObjectBindIntegrationTest extends AbstractLdapTemplateIntegrationTest {

	@Autowired
	private LdapTemplate tested;

	@Test
	public void testBindJavaObjectDate() throws Exception {
		String dn = "cn=myRandomDate";
		Date now = new Date();
		tested.bind(dn, now, null);

		Date result = (Date) tested.lookup(dn);
		assertEquals(now, result);
		tested.unbind(dn);
	}

	@Test
	public void testBindJavaObjectInteger() throws Exception {
		String dn = "cn=myRandomInt";
		int i = 54321;
		tested.bind(dn, new Integer(i), null);

		Integer result = (Integer) tested.lookup(dn);
		assertEquals(i, result.intValue());
		tested.unbind(dn);
	}

	@Test
	public void testBindLinkedList() {
		LinkedList list = new LinkedList();
		list.add(new Integer(54321));
		list.add(new Integer(67890));

		String dn = "cn=myRandomList";
		tested.bind(dn, list, null);

		LinkedList result = (LinkedList) tested.lookup(dn);
		assertEquals(2, result.size());
		assertEquals(54321, ((Integer) result.get(0)).intValue());
		assertEquals(67890, ((Integer) result.get(1)).intValue());
		tested.unbind(dn);
	}

	@Test
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

	@Test
	public void testBindSerializableJavaObjectShouldSucceed() throws Exception {
		SerializablePojo pojo = new SerializablePojo();
		pojo.setName("A Name");

		tested.bind("cn=myRandomObject", pojo, null);

		SerializablePojo result = (SerializablePojo) tested.lookup("cn=myRandomObject");
		assertEquals("A Name", result.getName());
		tested.unbind("cn=myRandomObject");
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
	@Test
	public void testPlainJndiBindJavaObject() throws Exception {
		AbstractContextSource contextSource = (AbstractContextSource) tested.getContextSource();
		DirContext ctx = contextSource.getReadWriteContext();

		ctx.bind("cn=myRandomInt", new Integer(28420));

		Integer result = (Integer) ctx.lookup("cn=myRandomInt");
		assertEquals(28420, result.intValue());
		tested.unbind("cn=myRandomInt");
	}
}
