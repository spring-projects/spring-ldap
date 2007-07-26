/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.mapping;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.springframework.ldap.odm.typeconversion.LdapTypeConverter;
import org.springframework.ldap.odm.typeconversion.ReferencedEntryEditorFactory;
import org.springframework.ldap.odm.entity.UnitTestPerson;

public class ObjectDirectoryMapperFactoryTest extends TestCase
{
    private ObjectDirectoryMapperFactory odmFactory;
    private LdapTypeConverter typeConverter;
    private ReferencedEntryEditorFactory refEditorFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        typeConverter = EasyMock.createStrictMock(LdapTypeConverter.class);
        refEditorFactory = EasyMock.createStrictMock(ReferencedEntryEditorFactory.class);
        odmFactory = new ObjectDirectoryMapperFactory(typeConverter, refEditorFactory);
    }

    public void testObjectDirectoryMapperForClass() throws MappingException
    {
        replayMocks();
        odmFactory.objectDirectoryMapperForClass(UnitTestPerson.class);
        //second call to retrieve mapper from cache
        odmFactory.objectDirectoryMapperForClass(UnitTestPerson.class);
    }

    private void replayMocks()
    {
        EasyMock.replay(typeConverter);
        EasyMock.replay(refEditorFactory);
    }
}
