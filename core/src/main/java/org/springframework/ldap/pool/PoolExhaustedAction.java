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

package org.springframework.ldap.pool;

/**
 * @author Mattias Hellborg Arthursson
 */
public enum PoolExhaustedAction {

	FAIL((byte) 0), BLOCK((byte) 1), GROW((byte) 2);

	private final byte value;

	PoolExhaustedAction(byte value) {
		this.value = value;
	}

	public byte getValue() {
		return this.value;
	}

}
