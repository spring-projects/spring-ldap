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

package org.springframework.ldap.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.Assert;

/**
 * @author Mattias Hellborg Arthursson
 * @since 2.0
 */
public class LdifPopulator implements InitializingBean {

	private Resource resource;

	private ContextSource contextSource;

	private String base = "";

	private boolean clean = false;

	private String defaultBase;

	public void setContextSource(ContextSource contextSource) {
		this.contextSource = contextSource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public void setClean(boolean clean) {
		this.clean = clean;
	}

	public void setDefaultBase(String defaultBase) {
		this.defaultBase = defaultBase;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.contextSource, "ContextSource must be specified");
		Assert.notNull(this.resource, "Resource must be specified");

		if (!LdapUtils.newLdapName(this.base).equals(LdapUtils.newLdapName(this.defaultBase))) {
			List<String> lines = IOUtils.readLines(this.resource.getInputStream());

			StringWriter sw = new StringWriter();
			PrintWriter writer = new PrintWriter(sw);
			for (String line : lines) {
				writer.println(StringUtils.replace(line, this.defaultBase, this.base));
			}

			writer.flush();
			this.resource = new ByteArrayResource(sw.toString().getBytes("UTF8"));
		}

		if (this.clean) {
			LdapTestUtils.clearSubContexts(this.contextSource, LdapUtils.emptyLdapName());
		}

		LdapTestUtils.loadLdif(this.contextSource, this.resource);
	}

}
