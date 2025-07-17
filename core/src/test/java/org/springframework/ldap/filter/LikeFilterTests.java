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

package org.springframework.ldap.filter;

import com.gargoylesoftware.base.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Anders Henja
 */
public class LikeFilterTests {

	@Test
	public void testEncodeValue_blank() {
		assertThat("").isEqualTo(new LikeFilter("", null).getEncodedValue());
		assertThat(" ").isEqualTo(new LikeFilter("", " ").getEncodedValue());
	}

	@Test
	public void testEncodeValue_normal() {
		assertThat("foo").isEqualTo(new LikeFilter("", "foo").getEncodedValue());
		assertThat("foo*bar").isEqualTo(new LikeFilter("", "foo*bar").getEncodedValue());
		assertThat("*foo*bar*").isEqualTo(new LikeFilter("", "*foo*bar*").getEncodedValue());
		assertThat("**foo**bar**").isEqualTo(new LikeFilter("", "**foo**bar**").getEncodedValue());
	}

	@Test
	public void testEncodeValue_escape() {
		assertThat("*\\28*\\29*").isEqualTo(new LikeFilter("", "*(*)*").getEncodedValue());
		assertThat("*\\5c2a*").isEqualTo(new LikeFilter("", "*\\2a*").getEncodedValue());
	}

	@Test
	public void testEquals() {
		String attribute = "a";
		String value = "b";
		LikeFilter originalObject = new LikeFilter(attribute, value);
		LikeFilter identicalObject = new LikeFilter(attribute, value);
		LikeFilter differentObject = new LikeFilter(attribute, "c");
		LikeFilter subclassObject = new LikeFilter(attribute, value) {
		};

		new EqualsTester(originalObject, identicalObject, differentObject, subclassObject);
	}

}
