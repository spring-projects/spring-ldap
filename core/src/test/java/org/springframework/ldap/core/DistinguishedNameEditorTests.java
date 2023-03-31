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

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Unit tests for {@link DistinguishedNameEditor}.
 *
 * @author Mattias Hellborg Arthursson
 */
public class DistinguishedNameEditorTests {

	private DistinguishedNameEditor tested;

	@Before
	public void setUp() throws Exception {
		this.tested = new DistinguishedNameEditor();
	}

	@Test
	public void testSetAsText() throws Exception {
		String expectedDn = "dc=jayway, dc=se";

		this.tested.setAsText(expectedDn);
		DistinguishedName result = (DistinguishedName) this.tested.getValue();
		assertThat(result).isEqualTo(new DistinguishedName(expectedDn));

		try {
			result.getNames().add(new LdapRdn("cn", "john doe"));
			fail("UnsupportedOperationException expected");
		}
		catch (UnsupportedOperationException expected) {
			assertThat(true).isTrue();
		}
	}

	@Test
	public void testSetAsTextNullValue() throws Exception {
		this.tested.setAsText(null);
		Object result = this.tested.getValue();
		assertThat(result).isNull();
	}

	@Test
	public void testGetAsText() throws Exception {
		String expectedDn = "dc=jayway,dc=se";
		this.tested.setValue(new DistinguishedName(expectedDn));
		String text = this.tested.getAsText();
		assertThat(text).isEqualTo(expectedDn);
	}

	@Test
	public void testGetAsTextNullValue() throws Exception {
		this.tested.setValue(null);
		String text = this.tested.getAsText();
		assertThat(text).isNull();
	}

}
