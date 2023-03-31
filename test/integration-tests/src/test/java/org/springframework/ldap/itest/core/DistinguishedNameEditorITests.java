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
package org.springframework.ldap.itest.core;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link org.springframework.ldap.core.DistinguishedNameEditor}.
 *
 * @author Mattias Hellborg Arthursson
 */
@ContextConfiguration(locations = { "/conf/distinguishedNameEditorTestContext.xml" })
public class DistinguishedNameEditorITests extends AbstractJUnit4SpringContextTests {

	@Autowired
	private DummyDistinguishedNameConsumer distinguishedNameConsumer;

	@Test
	public void testDistinguishedNameEditor() throws Exception {
		assertThat(distinguishedNameConsumer).isNotNull();
		DistinguishedName name = distinguishedNameConsumer.getDistinguishedName();
		assertThat(name).isEqualTo(new DistinguishedName("dc=jayway, dc=se"));
	}

}
