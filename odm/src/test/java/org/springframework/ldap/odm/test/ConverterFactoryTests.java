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

package org.springframework.ldap.odm.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.ldap.odm.test.utils.ExecuteRunnable;
import org.springframework.ldap.odm.test.utils.RunnableTests;
import org.springframework.ldap.odm.typeconversion.ConverterManager;
import org.springframework.ldap.odm.typeconversion.impl.Converter;
import org.springframework.ldap.odm.typeconversion.impl.ConverterManagerFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

public class ConverterFactoryTests {

	private static final Converter nullConverter = new NullConverter();

	private static ConverterConfigTestData[] converterConfigTestData = new ConverterConfigTestData[] {
			new ConverterConfigTestData(new Class<?>[] { String.class }, "", new Class<?>[] { Integer.class }),
			new ConverterConfigTestData(new Class<?>[] { Byte.class, java.lang.Integer.class }, "",
					new Class<?>[] { String.class, Long.class }),
			new ConverterConfigTestData(new Class<?>[] { String.class }, "123",
					new Class<?>[] { java.net.URI.class }), };

	private ConverterTestData[] converterTestData = new ConverterTestData[] {
			new ConverterTestData(java.lang.String.class, "", java.lang.Integer.class, true),
			new ConverterTestData(java.lang.Byte.class, "", java.lang.Long.class, true),
			new ConverterTestData(java.lang.Integer.class, "444", java.lang.String.class, true),
			new ConverterTestData(java.lang.String.class, "123", java.net.URI.class, true),
			new ConverterTestData(java.lang.String.class, "123", java.lang.Byte.class, false),
			new ConverterTestData(java.lang.Byte.class, "", java.lang.Integer.class, false) };

	@Test
	public void testConverterFactory() throws Exception {
		ConverterManagerFactoryBean converterManagerFactory = new ConverterManagerFactoryBean();
		Set<ConverterManagerFactoryBean.ConverterConfig> configList = new HashSet<>();
		for (ConverterConfigTestData config : converterConfigTestData) {
			ConverterManagerFactoryBean.ConverterConfig converterConfig = new ConverterManagerFactoryBean.ConverterConfig();
			converterConfig.setFromClasses(new HashSet<>(Arrays.asList(config.fromClasses)));
			converterConfig.setSyntax(config.syntax);
			converterConfig.setToClasses(new HashSet<>(Arrays.asList(config.toClasses)));
			converterConfig.setConverter(nullConverter);
			configList.add(converterConfig);
		}
		converterManagerFactory.setConverterConfig(configList);
		final ConverterManager converterManager = (ConverterManager) converterManagerFactory.getObject();

		new ExecuteRunnable<ConverterTestData>().runTests(new RunnableTests<>() {
			public void runTest(ConverterTestData testData) {
				assertThat(testData.canConvert)
					.isEqualTo(converterManager.canConvert(testData.fromClass, testData.syntax, testData.toClass));
			}
		}, this.converterTestData);
	}

	private static class NullConverter implements Converter {

		@Override
		public <T> T convert(Object source, Class<T> toClass) throws Exception {
			return null;
		}

	}

	private static final class ConverterConfigTestData {

		private Class<?>[] fromClasses;

		private String syntax;

		private Class<?>[] toClasses;

		private ConverterConfigTestData(Class<?>[] fromClasses, String syntax, Class<?>[] toClasses) {
			this.fromClasses = fromClasses;
			this.syntax = syntax;
			this.toClasses = toClasses;
		}

	}

	private static final class ConverterTestData {

		private final Class<?> fromClass;

		private final String syntax;

		private final Class<?> toClass;

		private final boolean canConvert;

		private ConverterTestData(Class<?> fromClass, String syntax, Class<?> toClass, boolean canConvert) {
			this.fromClass = fromClass;
			this.syntax = syntax;
			this.toClass = toClass;
			this.canConvert = canConvert;
		}

	}

}
