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

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.naming.directory.Attributes;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.ldif.parser.LdifParser;
import org.springframework.ldap.ldif.parser.Parser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that {@link Parser} can be used in a try-with-resources statement, as
 * requested in gh-1936.
 *
 * @author Huy Vu
 */
public class LdifParserCloseableTests {

	private static final String LDIF = "dn: cn=Barbara Jensen, ou=Product Development, dc=airius, dc=com\n"
			+ "objectclass: top\n"
			+ "objectclass: person\n"
			+ "cn: Barbara Jensen\n"
			+ "sn: Jensen\n"
			+ "\n";

	@Test
	public void parserCanBeUsedInTryWithResources() throws Exception {
		int count = 0;
		try (Parser parser = new LdifParser(
				new InputStreamResource(new ByteArrayInputStream(LDIF.getBytes(StandardCharsets.UTF_8))))) {
			parser.open();
			while (parser.hasMoreRecords()) {
				Attributes attributes = parser.getRecord();
				if (attributes != null) {
					assertThat(attributes.get("objectclass")).isNotNull();
					count++;
				}
			}
		}
		assertThat(count).isEqualTo(1);
	}

	@Test
	public void tryWithResourcesClosesUnderlyingStreamForNonOpenResource() throws Exception {
		TrackingInputStream stream = new TrackingInputStream(
				new ByteArrayInputStream(LDIF.getBytes(StandardCharsets.UTF_8)));
		// Most Resource implementations (for example ClassPathResource and
		// FileSystemResource) report isOpen() == false, so closing the parser must
		// not depend on it.
		Resource resource = new InputStreamResource(stream) {
			@Override
			public boolean isOpen() {
				return false;
			}
		};
		try (Parser parser = new LdifParser(resource)) {
			parser.open();
			while (parser.hasMoreRecords()) {
				parser.getRecord();
			}
		}
		assertThat(stream.isClosed()).isTrue();
	}

	private static final class TrackingInputStream extends FilterInputStream {

		private boolean closed;

		TrackingInputStream(InputStream in) {
			super(in);
		}

		@Override
		public void close() throws IOException {
			this.closed = true;
			super.close();
		}

		boolean isClosed() {
			return this.closed;
		}

	}

}
