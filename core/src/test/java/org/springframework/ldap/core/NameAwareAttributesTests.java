/*
 * Copyright 2005-2025 the original author or authors.
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

import java.util.List;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NameAwareAttributesTests {

	// gh-548
	@Test
	public void removeWhenDifferentCaseThenRemoves() {
		NameAwareAttributes attributes = new NameAwareAttributes();
		attributes.put("myID", "value");
		attributes.put("myOtherID", "othervalue");
		assertThat(attributes.size()).isEqualTo(2);
		assertThat(attributes.get("myid").get()).isEqualTo("value");
		assertThat(attributes.get("myID").get()).isEqualTo("value");

		attributes.remove("myid");
		assertThat(attributes.get("myID")).isNull();
		assertThat(attributes.size()).isEqualTo(1);

		attributes.remove("myOtherID");
		assertThat(attributes.size()).isEqualTo(0);
	}

	@Test
	public void iteratorWhenAttributesThenIterates() {
		NameAwareAttributes attributes = new NameAwareAttributes();
		attributes.put("myID", "value");
		attributes.put("myOtherID", "othervalue");
		List<String> ids = StreamSupport.stream(attributes.spliterator(), false)
			.map(NameAwareAttribute::getID)
			.toList();
		assertThat(ids).containsOnly("myID", "myOtherID");
	}

}
