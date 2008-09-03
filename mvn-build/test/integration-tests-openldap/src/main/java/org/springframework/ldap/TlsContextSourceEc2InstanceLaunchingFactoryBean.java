package org.springframework.ldap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;

public class TlsContextSourceEc2InstanceLaunchingFactoryBean extends ContextSourceEc2InstanceLaunchingFactoryBean {
	
	protected void setAdditionalContextSourceProperties(LdapContextSource ctx, final String dnsName) {
		DefaultTlsDirContextAuthenticationStrategy authenticationStrategy = new DefaultTlsDirContextAuthenticationStrategy();
	
		authenticationStrategy.setHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return hostname.equals(dnsName);
			}
		});

		ctx.setAuthenticationStrategy(authenticationStrategy);
		ctx.setPooled(false);
	}
}
