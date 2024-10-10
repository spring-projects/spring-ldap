/*
 * Copyright 2002-2024 the original author or authors.
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

import javax.naming.directory.Attribute;
import javax.naming.directory.ModificationItem;

import static org.assertj.core.api.Assertions.assertThat;

final class TestModificationItems {

	private TestModificationItems() {

	}

	static ModificationItem add(String name, Object... values) {
		return add(TestNameAwareAttributes.attribute(name, values));
	}

	static ModificationItem add(Attribute attribute) {
		return new ModificationItem(MockDirContext.ADD_ATTRIBUTE, attribute);
	}

	static ModificationItem replace(String name, Object... values) {
		return remove(name, TestNameAwareAttributes.attribute(name, values));
	}

	static ModificationItem replace(Attribute attribute) {
		return new ModificationItem(MockDirContext.REPLACE_ATTRIBUTE, attribute);
	}

	static ModificationItem remove(String name, Object... values) {
		return remove(TestNameAwareAttributes.attribute(name, values));
	}

	static ModificationItem remove(Attribute attribute) {
		return new ModificationItem(MockDirContext.REMOVE_ATTRIBUTE, attribute);
	}

	static void assertEquals(ModificationItem actual, ModificationItem expected) {
		assertThat(actual.getAttribute()).isEqualTo(expected.getAttribute());
		assertThat(actual.getModificationOp()).isEqualTo(expected.getModificationOp());
	}

}
