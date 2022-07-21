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
package org.springframework.ldap.core;

import org.junit.Test;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests that serve as regression tests for bugs that have been fixed.
 * 
 * @author Luke Taylor
 */
public class DirContextAdapterBugTest {

	@Test
	public void testResetAttributeValuesNotReportedAsModifications() {
		BasicAttributes attrs = new BasicAttributes("myattr", "a");
		attrs.get("myattr").add("b");
		attrs.get("myattr").add("c");
		UpdateAdapter ctx = new UpdateAdapter(attrs, LdapUtils.emptyLdapName());

		ctx.setAttributeValues("myattr", new String[] { "a", "b" });
		ctx.setAttributeValues("myattr", new String[] { "a", "b", "c" });

		assertThat(ctx.getModificationItems().length).isEqualTo(0);
	}

	@Test
	public void testResetAttributeValuesSameLengthNotReportedAsModifications() {
		BasicAttributes attrs = new BasicAttributes("myattr", "a");
		attrs.get("myattr").add("b");
		attrs.get("myattr").add("c");
		UpdateAdapter ctx = new UpdateAdapter(attrs, LdapUtils.emptyLdapName());

		ctx.setAttributeValues("myattr", new String[] { "a", "b", "d" });
		ctx.setAttributeValues("myattr", new String[] { "a", "b", "c" });

		assertThat(ctx.getModificationItems().length).isEqualTo(0);
	}

	/**
	 * This test starts with an array with a null value in it (because that's
	 * how BasicAttributes will do it), changes to <code>[a]</code>, and then
	 * changes to <code>null</code>. The current code interprets this as a
	 * change and will replace the original array with an empty array.
	 * 
	 * TODO Is this correct behaviour?
	 */
	@Test
	public void testResetNullAttributeValuesReportedAsModifications() {
		BasicAttributes attrs = new BasicAttributes("myattr", null);
		UpdateAdapter ctx = new UpdateAdapter(attrs, LdapUtils.emptyLdapName());

		ctx.setAttributeValues("myattr", new String[] { "a" });
		ctx.setAttributeValues("myattr", null);

		assertThat(ctx.getModificationItems().length).isEqualTo(1);
	}

	@Test
	public void testResetNullAttributeValueNotReportedAsModification() throws Exception {
		BasicAttributes attrs = new BasicAttributes("myattr", "b");
		UpdateAdapter ctx = new UpdateAdapter(attrs, LdapUtils.emptyLdapName());

		ctx.setAttributeValue("myattr", "a");
		ctx.setAttributeValue("myattr", "b");

		assertThat(ctx.getModificationItems().length).isEqualTo(0);
	}

	private static class UpdateAdapter extends DirContextAdapter {
		public UpdateAdapter(Attributes attrs, Name dn) {
			super(attrs, dn);
			setUpdateMode(true);
		}
	}
}
