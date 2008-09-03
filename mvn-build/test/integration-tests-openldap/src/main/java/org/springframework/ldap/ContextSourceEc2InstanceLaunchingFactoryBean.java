package org.springframework.ldap;

import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;

public class ContextSourceEc2InstanceLaunchingFactoryBean extends AbstractEc2InstanceLaunchingFactoryBean {

	private String base;

	private String userDn;

	private String password;

	@Override
	public final Class getObjectType() {
		return ContextSource.class;
	}

	@Override
	protected final Object doCreateInstance(final String dnsName) throws Exception {
		LdapContextSource instance = new LdapContextSource();
		instance.setUrl("ldap://" + dnsName);
		instance.setUserDn(userDn);
		instance.setPassword(password);
		instance.setBase(base);
		setAdditionalContextSourceProperties(instance, dnsName);

		instance.afterPropertiesSet();
		return instance;
	}

	/**
	 * Override to set additional properties on the ContextSource.
	 * 
	 * @param ctx The created instance.
	 * @param dnsName The dns name of the created Ec2 instance.
	 */
	protected void setAdditionalContextSourceProperties(LdapContextSource ctx, final String dnsName) {

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
