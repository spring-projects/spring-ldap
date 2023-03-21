package org.springframework.ldap.core.support;

import javax.naming.ldap.LdapContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * @author Rob Winch
 * @since 5.0
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultTlsDirContextAuthenticationStrategyTests {

	@Mock
	private LdapContext context;

	private DefaultTlsDirContextAuthenticationStrategy strategy = new DefaultTlsDirContextAuthenticationStrategy();

	// gh-430, gh-502
	@Test
	public void applyAuthenticationThenLookupInvoked() throws Exception {
		this.strategy.applyAuthentication(this.context, "username", "password");

		verify(this.context).lookup("");
	}

}