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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Integration tests for {@link DistinguishedNameEditor}.
 * 
 * @author Mattias Arthursson
 */
@ContextConfiguration(locations = { "/conf/distinguishedNameEditorTestContext.xml" })
public class DistinguishedNameEditorITest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private DummyDistinguishedNameConsumer distinguishedNameConsumer;

	@Test
	public void testDistinguishedNameEditor() throws Exception {
		assertNotNull(distinguishedNameConsumer);
		DistinguishedName name = distinguishedNameConsumer.getDistinguishedName();
		assertEquals(new DistinguishedName("dc=jayway, dc=se"), name);
	}
}
