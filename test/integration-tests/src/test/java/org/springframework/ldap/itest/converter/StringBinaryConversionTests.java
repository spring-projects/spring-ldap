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

package org.springframework.ldap.itest.converter;

import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.odm.config.ObjectDirectoryMapperConfiguration;
import org.springframework.ldap.odm.core.impl.DefaultObjectDirectoryMapper;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ngoc Nhan
 */
@ExtendWith(SpringExtension.class)
public class StringBinaryConversionTests {

	@Autowired
	private DefaultObjectDirectoryMapper mapper;

	@Test
	public void mapToLdapDataEntryWhenBinaryType() {

		UnitTestPersonBinaryType testPerson = new UnitTestPersonBinaryType();
		testPerson.setTestString("testStringValue");
		DirContextAdapter adapter = new DirContextAdapter();
		this.mapper.mapToLdapDataEntry(testPerson, adapter);
		assertThat(adapter.getObjectAttribute("testString")).isNotNull()
			.isInstanceOf(byte[].class)
			.isEqualTo("testStringValue".getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void mapFromLdapDataEntryWhenBinaryAttribute() {

		DirContextAdapter adapter = new DirContextAdapter();
		adapter.setAttributeValues("objectclass",
				new String[] { "inetOrgPerson", "organizationalPerson", "person", "top" });
		adapter.setAttributeValue("testBytes", "testBytesValue");
		UnitTestPersonBinaryType testPerson = this.mapper.mapFromLdapDataEntry(adapter, UnitTestPersonBinaryType.class);
		assertThat(testPerson).isNotNull();
		assertThat(testPerson.getTestBytes()).isNotNull()
			.isInstanceOf(byte[].class)
			.isEqualTo("testBytesValue".getBytes(StandardCharsets.UTF_8));
	}

	@Import(ObjectDirectoryMapperConfiguration.class)
	@Configuration
	static class EmbeddedLdapConfig {

		@Bean
		static ConversionServiceFactoryBean conversionService() {
			return new ConversionServiceFactoryBean();
		}

		@Autowired
		void setup(ConversionService conversionService) {

			if (conversionService instanceof ConverterRegistry registry) {
				registry.addConverter(new StringToBinaryConverter());
				registry.addConverter(new BinaryToStringConverter());
			}
		}

	}

	static class StringToBinaryConverter implements Converter<String, byte @Nullable []> {

		@Override
		public byte @Nullable [] convert(@Nullable String source) {

			if (source == null) {
				return null;
			}

			if (LdapUtils.isLdapSid(source)) {
				return LdapUtils.convertStringSidToBinary(source);
			}

			return source.getBytes(StandardCharsets.UTF_8);
		}

	}

	static class BinaryToStringConverter implements Converter<byte[], String> {

		@Override
		public @Nullable String convert(byte @Nullable [] source) {

			if (source == null) {
				return null;
			}

			if (LdapUtils.isLdapSid(source)) {
				return LdapUtils.convertBinarySidToString(source);
			}

			// source to be decoded into characters
			return new String(source, StandardCharsets.UTF_8);
		}

	}

}
