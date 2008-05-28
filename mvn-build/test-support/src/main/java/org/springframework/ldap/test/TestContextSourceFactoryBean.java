package org.springframework.ldap.test;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;

public class TestContextSourceFactoryBean extends AbstractFactoryBean {
	private int port;

	private String defaultPartitionSuffix;

	private String defaultPartitionName;

	private String principal;

	private String password;

	private boolean baseOnTarget = true;

	private Resource ldifFile;

	private Class dirObjectFactory = DefaultDirObjectFactory.class;

	private boolean pooled = true;

	private AuthenticationSource authenticationSource;

	public void setAuthenticationSource(
			AuthenticationSource authenticationSource) {
		this.authenticationSource = authenticationSource;
	}

	public void setPooled(boolean pooled) {
		this.pooled = pooled;
	}

	public void setDirObjectFactory(Class dirObjectFactory) {
		this.dirObjectFactory = dirObjectFactory;
	}

	public void setLdifFile(Resource ldifFile) {
		this.ldifFile = ldifFile;
	}

	public void setBaseOnTarget(boolean baseOnTarget) {
		this.baseOnTarget = baseOnTarget;
	}

	public void setDefaultPartitionSuffix(String defaultPartitionSuffix) {
		this.defaultPartitionSuffix = defaultPartitionSuffix;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setDefaultPartitionName(String defaultPartitionName) {
		this.defaultPartitionName = defaultPartitionName;
	}

	public void setPort(int port) {
		this.port = port;
	}

	protected Object createInstance() throws Exception {
		LdapTestUtils.startApacheDirectoryServer(port, defaultPartitionSuffix,
				defaultPartitionName);

		LdapContextSource targetContextSource = new LdapContextSource();
		if (baseOnTarget) {
			targetContextSource.setBase(defaultPartitionSuffix);
		}

		targetContextSource.setUrl("ldap://localhost:" + port);
		targetContextSource.setUserDn(principal);
		targetContextSource.setPassword(password);
		targetContextSource.setDirObjectFactory(dirObjectFactory);
		targetContextSource.setPooled(pooled);
		if (authenticationSource != null) {
			targetContextSource.setAuthenticationSource(authenticationSource);
		}
		targetContextSource.afterPropertiesSet();

		if (baseOnTarget) {
			LdapTestUtils.clearSubContexts(targetContextSource,
					DistinguishedName.EMPTY_PATH);
		} else {
			LdapTestUtils.clearSubContexts(targetContextSource,
					new DistinguishedName(defaultPartitionSuffix));
		}

		if (ldifFile != null) {
			LdapTestUtils.loadLdif(targetContextSource, ldifFile);
		}

		return targetContextSource;
	}

	public Class getObjectType() {
		return ContextSource.class;
	}

	protected void destroyInstance(Object instance) throws Exception {
		super.destroyInstance(instance);

		LdapTestUtils.destroyApacheDirectoryServer(principal, password);
	}
}
