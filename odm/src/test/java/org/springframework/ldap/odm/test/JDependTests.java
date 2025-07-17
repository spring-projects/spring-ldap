/*
 * Copyright 2005-2013 the original author or authors.
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

package org.springframework.ldap.odm.test;

import java.io.IOException;

import jdepend.framework.JDepend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JDependTests {

	private JDepend jdepend;

	@BeforeEach
	public void setUp() throws IOException {
		this.jdepend = new JDepend();
		this.jdepend.addDirectory("build/classes/java/main");
	}

	@Test
	public void testAllPackages() {
		this.jdepend.analyze();
		assertThat(this.jdepend.containsCycles()).isFalse();
	}

}
