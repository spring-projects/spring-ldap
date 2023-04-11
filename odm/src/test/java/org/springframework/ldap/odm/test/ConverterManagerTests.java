/*
 * Copyright 2005-2023 the original author or authors.
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

import java.net.URI;
import java.util.BitSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.ldap.odm.test.utils.ExecuteRunnable;
import org.springframework.ldap.odm.test.utils.RunnableTests;
import org.springframework.ldap.odm.typeconversion.ConverterException;
import org.springframework.ldap.odm.typeconversion.impl.Converter;
import org.springframework.ldap.odm.typeconversion.impl.ConverterManagerImpl;
import org.springframework.ldap.odm.typeconversion.impl.converters.FromStringConverter;
import org.springframework.ldap.odm.typeconversion.impl.converters.ToStringConverter;

import static org.junit.Assert.assertEquals;

public final class ConverterManagerTests {

	private ConverterManagerImpl converterManager;

	@Before
	public void setUp() {
		converterManager = new ConverterManagerImpl();

		Converter ptc = new FromStringConverter();
		converterManager.addConverter(String.class, "", Byte.class, ptc);
		converterManager.addConverter(String.class, "", Short.class, ptc);
		converterManager.addConverter(String.class, "", Integer.class, ptc);
		converterManager.addConverter(String.class, "", Long.class, ptc);
		converterManager.addConverter(String.class, "", Double.class, ptc);
		converterManager.addConverter(String.class, "", Float.class, ptc);
		converterManager.addConverter(String.class, "", Boolean.class, ptc);

		Converter tsc = new ToStringConverter();
		converterManager.addConverter(Byte.class, "", String.class, tsc);
		converterManager.addConverter(Short.class, "", String.class, tsc);
		converterManager.addConverter(Integer.class, "", String.class, tsc);
		converterManager.addConverter(Long.class, "", String.class, tsc);
		converterManager.addConverter(Double.class, "", String.class, tsc);
		converterManager.addConverter(Float.class, "", String.class, tsc);
		converterManager.addConverter(Boolean.class, "", String.class, tsc);

		Converter uric = new UriConverter();
		converterManager.addConverter(URI.class, "", String.class, uric);
		converterManager.addConverter(String.class, "", URI.class, uric);
	}

	@After
	public void tearDown() {
		converterManager = null;
	}

	private static class ConverterTestData<T> {

		public final Class<T> destClass;

		public final Object sourceData;

		public final T expectedValue;

		public final String syntax;

		public ConverterTestData(Object sourceData, Class<T> destClass, T expectedValue) {
			this(sourceData, "", destClass, expectedValue);
		}

		public ConverterTestData(Object sourceData, String syntax, Class<T> destClass, T expectedValue) {
			this.destClass = destClass;
			this.sourceData = sourceData;
			this.expectedValue = expectedValue;
			this.syntax = syntax;
		}

		@Override
		public String toString() {
			return String.format("sourceData=%1$s | syntax=%2$s | destClass=%3$s | expectedValue=%4$s", sourceData,
					syntax, destClass, expectedValue);
		}

	}

	// Class to Class conversion without any syntaxes
	@Test
	public void basicTypeConverion() throws Exception {
		final ConverterTestData<?>[] primitiveTypeTests = new ConverterTestData<?>[] {
				new ConverterTestData<Byte>("33", Byte.class, Byte.valueOf((byte) 33)),
				new ConverterTestData<Byte>("-88", Byte.class, Byte.valueOf((byte) -88)),
				new ConverterTestData<Short>("666", Short.class, Short.valueOf((short) 666)),
				new ConverterTestData<Short>("-123", Short.class, Short.valueOf((short) -123)),
				new ConverterTestData<Integer>("123", Integer.class, Integer.valueOf(123)),
				new ConverterTestData<Integer>("-500", Integer.class, Integer.valueOf(-500)),
				new ConverterTestData<Long>("123456", Long.class, Long.valueOf(123456)),
				new ConverterTestData<Long>("-654321", Long.class, Long.valueOf(-654321)),
				new ConverterTestData<Double>("2", Double.class, Double.valueOf(2)),
				new ConverterTestData<Double>("-0.4", Double.class, Double.valueOf(-0.4)),
				new ConverterTestData<Float>("666", Float.class, Float.valueOf(666)),
				new ConverterTestData<Float>("-0.75", Float.class, Float.valueOf(-0.75F)),
				new ConverterTestData<Boolean>("false", Boolean.class, Boolean.FALSE),
				new ConverterTestData<Boolean>("TRUE", Boolean.class, Boolean.TRUE),
				new ConverterTestData<String>("This is a string", String.class, "This is a string"),
				new ConverterTestData<String>("This is another String", String.class, "This is another String"),
				new ConverterTestData<String>((byte) 66, String.class, "66"),
				new ConverterTestData<String>((int) 1234, String.class, "1234"),
				new ConverterTestData<String>((int) -9876, String.class, "-9876"),
				new ConverterTestData<URI>("https://google.com/", URI.class, new URI("https://google.com/")),
				new ConverterTestData<URI>("https://apache.org/index.html", URI.class,
						new URI("https://apache.org/index.html")),
				new ConverterTestData<String>(new URI("https://google.com/"), String.class, "https://google.com/"),
				new ConverterTestData<String>(new URI("https://apache.org/index.html"), String.class,
						"https://apache.org/index.html") };

		new ExecuteRunnable<ConverterTestData<?>>().runTests(new RunnableTests<ConverterTestData<?>>() {
			public void runTest(ConverterTestData<?> testData) {
				assertEquals(testData.expectedValue,
						converterManager.convert(testData.sourceData, "", testData.destClass));
			}
		}, primitiveTypeTests);
	}

	private static class SquaredConverter implements Converter {

		public <T> T convert(Object source, Class<T> toClass) throws Exception {
			Integer intSource = null;

			if (source.getClass() == String.class) {
				intSource = new Integer((String) source);
			}
			else {
				if (source.getClass() == Integer.class) {
					intSource = (Integer) source;
				}
			}

			Integer result = null;
			if (intSource != null) {
				result = intSource * intSource;
			}

			return toClass.cast(result);
		}

	}

	private static class CubedConverter implements Converter {

		public <T> T convert(Object source, Class<T> toClass) throws Exception {
			Integer intSource = null;

			if (source.getClass() == String.class) {
				intSource = new Integer((String) source);
			}
			else {
				if (source.getClass() == Integer.class) {
					intSource = (Integer) source;
				}
			}

			Integer result = null;
			if (intSource != null) {
				result = intSource * intSource * intSource;
			}

			return toClass.cast(result);
		}

	}

	// Tests using syntaxes for "finer grained" mapping
	@Test
	public void syntaxBasedConversion() throws Exception {
		Converter squaredConverter = new SquaredConverter();
		converterManager.addConverter(String.class, "1", Integer.class, squaredConverter);
		converterManager.addConverter(Integer.class, "1", Integer.class, squaredConverter);
		Converter cubedConverter = new CubedConverter();
		converterManager.addConverter(String.class, "2", Integer.class, cubedConverter);
		converterManager.addConverter(Integer.class, "3", Integer.class, cubedConverter);

		final ConverterTestData<?>[] syntaxTests = new ConverterTestData<?>[] {
				new ConverterTestData<Integer>("3", "", Integer.class, Integer.valueOf(3)),
				new ConverterTestData<Integer>("4", "", Integer.class, Integer.valueOf(4)),
				new ConverterTestData<Integer>(5, "", Integer.class, Integer.valueOf(5)),
				new ConverterTestData<Integer>(6, "", Integer.class, Integer.valueOf(6)),
				new ConverterTestData<Integer>("3", "1", Integer.class, Integer.valueOf(9)),
				new ConverterTestData<Integer>("4", "1", Integer.class, Integer.valueOf(16)),
				new ConverterTestData<Integer>(5, "1", Integer.class, Integer.valueOf(25)),
				new ConverterTestData<Integer>(6, "1", Integer.class, Integer.valueOf(36)),
				new ConverterTestData<Integer>("3", "2", Integer.class, Integer.valueOf(27)),
				new ConverterTestData<Integer>("4", "2", Integer.class, Integer.valueOf(64)),
				new ConverterTestData<Integer>(5, "3", Integer.class, Integer.valueOf(125)),
				new ConverterTestData<Integer>(6, "3", Integer.class, Integer.valueOf(216)), };

		new ExecuteRunnable<ConverterTestData<?>>().runTests(new RunnableTests<ConverterTestData<?>>() {
			public void runTest(ConverterTestData<?> testData) {
				assertEquals(testData.expectedValue,
						converterManager.convert(testData.sourceData, testData.syntax, testData.destClass));
			}
		}, syntaxTests);

	}

	// No converter for classes
	@Test(expected = ConverterException.class)
	public void noClassConverter() throws Exception {
		converterManager.convert(BitSet.class, "", Integer.class);
	}

	// Invalid syntax so converter fails
	@Test(expected = ConverterException.class)
	public void invalidSyntax() throws Exception {
		converterManager.convert(String.class, "not a uri", URI.class);
	}

}
