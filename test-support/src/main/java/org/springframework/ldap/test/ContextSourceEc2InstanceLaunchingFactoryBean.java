/*
 * Copyright 2005-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.test;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.AbstractEc2InstanceLaunchingFactoryBean;
import org.springframework.util.Assert;

/**
 * FactoryBean to create a ContextSource using the EC2 instance created by
 * superclass.
 */
public class ContextSourceEc2InstanceLaunchingFactoryBean extends AbstractEc2InstanceLaunchingFactoryBean {

	private String base;

	private String userDn;

	private String password;

	private boolean pooled = false;

	@Override
	public final Class getObjectType() {
		return ContextSource.class;
	}

	@Override
	protected final Object doCreateInstance(final String dnsName) throws Exception {
		Assert.hasText(userDn);
		LdapContextSource instance = new LdapContextSource();
		instance.setUrl("ldap://" + dnsName);
		instance.setUserDn(userDn);
		instance.setPassword(password);
		instance.setBase(base);
		instance.setPooled(pooled);
		setAdditionalContextSourceProperties(instance, dnsName);

		instance.afterPropertiesSet();
		return instance;
	}

	public void setPooled(boolean pooled) {
		this.pooled = pooled;
	}

	/**
	 * Override to set additional properties on the ContextSource.
	 * 
	 * @param ctx The created instance.
	 * @param dnsName The dns name of the created Ec2 instance.
	 */
	protected void setAdditionalContextSourceProperties(LdapContextSource ctx, final String dnsName) {
		// Nothing to do here
	}

	public void setBase(String base) {
		this.base = base;
	}

	public void setUserDn(String userDn) {
		this.userDn = userDn;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
