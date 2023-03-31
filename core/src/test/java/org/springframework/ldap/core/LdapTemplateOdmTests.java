package org.springframework.ldap.core;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LdapTemplateOdmTests {

	private LdapTemplate tested;

	private ObjectDirectoryMapper odmMock;

	@Before
	public void prepareTestedClass() {
		this.tested = mock(LdapTemplate.class);

		doCallRealMethod().when(this.tested).setObjectDirectoryMapper(any(ObjectDirectoryMapper.class));
		this.odmMock = mock(ObjectDirectoryMapper.class);

		this.tested.setObjectDirectoryMapper(this.odmMock);
	}

	@Test
	public void testFindByDn() {

	}

}
