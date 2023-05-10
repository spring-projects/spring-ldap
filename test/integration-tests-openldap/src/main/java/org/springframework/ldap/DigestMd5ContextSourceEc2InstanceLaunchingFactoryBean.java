/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ldap;

import org.springframework.ldap.core.support.DigestMd5DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.ContextSourceEc2InstanceLaunchingFactoryBean;

/**
 * FactoryBean for testing LDAP TLS connections on an Amazon EC2 image launched by
 * superclass.
 */
public class DigestMd5ContextSourceEc2InstanceLaunchingFactoryBean
		extends ContextSourceEc2InstanceLaunchingFactoryBean {

	protected void setAdditionalContextSourceProperties(LdapContextSource ctx, final String dnsName) {
		DigestMd5DirContextAuthenticationStrategy authenticationStrategy = new DigestMd5DirContextAuthenticationStrategy();

		// authenticationStrategy.setHostnameVerifier(new HostnameVerifier() {
		// public boolean verify(String hostname, SSLSession session) {
		// return hostname.equals(dnsName);
		// }
		// });

		ctx.setAuthenticationStrategy(authenticationStrategy);
		ctx.setPooled(false);
	}

}
