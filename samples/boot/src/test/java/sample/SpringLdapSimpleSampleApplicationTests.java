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

package sample;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpringLdapSimpleSampleApplicationTests {

	@Autowired
	MockMvc mvc;

	@Test
	void indexWhenCorrectUsernameAndPasswordThenAuthenticates() throws Exception {
		HttpHeaders http = new HttpHeaders();
		http.setBasicAuth("bob", "bobspassword");
		this.mvc.perform(get("/").headers(http))
				.andExpect(status().isOk())
				.andExpect(content().string("Hello, bob"));
	}

	@Test
	void cnWhenCorrectUsernameAndPasswordThenShowsCommonName() throws Exception {
		HttpHeaders http = new HttpHeaders();
		http.setBasicAuth("bob", "bobspassword");
		this.mvc.perform(get("/cn").headers(http))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0]").value("Bob Hamilton"));
	}
}
