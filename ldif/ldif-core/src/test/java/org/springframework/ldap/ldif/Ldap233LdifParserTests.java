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

package org.springframework.ldap.ldif;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import org.springframework.ldap.ldif.parser.LdifParser;
import org.springframework.ldap.schema.BasicSchemaSpecification;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mattias Hellborg Arthursson
 */
public class Ldap233LdifParserTests {

	/**
	 * This previously went into endless loop.
	 * @throws IOException
	 */
	@Test
	public void ldap233Test() throws IOException {
		File testFile = File.createTempFile("ldapTest", ".ldif");
		Files.write(testFile.toPath(), "This is just some random text".getBytes());

		LdifParser parser = new LdifParser(testFile);
		parser.setRecordSpecification(new BasicSchemaSpecification());
		parser.open();
		assertThat(parser.getRecord()).isNull();

		testFile.delete();
	}

}
