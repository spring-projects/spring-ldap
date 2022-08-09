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
public class LdapTemplateOdmTest {

	private LdapTemplate tested;
	private ObjectDirectoryMapper odmMock;

	@Before
	public void prepareTestedClass() {
		tested = mock(LdapTemplate.class);

		doCallRealMethod().when(tested).setObjectDirectoryMapper(any(ObjectDirectoryMapper.class));
		odmMock = mock(ObjectDirectoryMapper.class);

		tested.setObjectDirectoryMapper(odmMock);
	}


	@Test
	public void testFindByDn() {

	}
}
