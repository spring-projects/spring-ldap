package org.springframework.ldap.samples.utils;

import static junit.framework.Assert.assertEquals;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = { "/conf/testContext.xml" })
public class LdapTreeBuilderIntegrationTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private LdapTreeBuilder tested;

	@Test
	public void testGetLdapTree() {
		LdapTree ldapTree = tested.getLdapTree(new DistinguishedName("c=Sweden"));
		ldapTree.traverse(new TestVisitor());
	}

	private static final class TestVisitor implements LdapTreeVisitor {
		private static final DistinguishedName DN_1 = new DistinguishedName("c=Sweden");

		private static final DistinguishedName DN_2 = new DistinguishedName("ou=company1,c=Sweden");

		private static final DistinguishedName DN_3 = new DistinguishedName("cn=Some Person,ou=company1,c=Sweden");

		private static final DistinguishedName DN_4 = new DistinguishedName("cn=Some Person2,ou=company1,c=Sweden");

		private Map<DistinguishedName, Integer> names = new LinkedHashMap<DistinguishedName, Integer>();

		private Iterator<DistinguishedName> keyIterator;

		public TestVisitor() {
			names.put(DN_1, 0);
			names.put(DN_2, 1);
			names.put(DN_3, 2);
			names.put(DN_4, 2);

			keyIterator = names.keySet().iterator();
		}

		public void visit(DirContextOperations node, int currentDepth) {
			DistinguishedName next = keyIterator.next();
			assertEquals(next, node.getDn());
			assertEquals(names.get(next).intValue(), currentDepth);
		}
	}

}
