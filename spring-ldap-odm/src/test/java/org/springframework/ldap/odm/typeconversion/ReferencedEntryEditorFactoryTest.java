/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.typeconversion;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.mapping.MappingException;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapper;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapperFactory;


public class ReferencedEntryEditorFactoryTest extends TestCase
{
    private LdapTemplate ldapTemplate;
    private ObjectDirectoryMapperFactory odmFactory;
    private ObjectDirectoryMapper odm;
    private ReferencedEntryEditorFactory editorFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        ldapTemplate = EasyMock.createStrictMock(LdapTemplate.class);
        odmFactory = EasyMock.createStrictMock(ObjectDirectoryMapperFactory.class);
        odm = EasyMock.createStrictMock(ObjectDirectoryMapper.class);
        editorFactory = new ReferencedEntryEditorFactory(null, ldapTemplate);
        editorFactory.setObjectDirectoryMapperFactory(odmFactory);
    }

    /**
     * Asserts that a referenced entry editor is created, after successful mapping of
     * the referenced entity.
     *
     * @throws MappingException
     *
     */
    public void testReferencedEditorForClass_successfulMapping()
            throws MappingException
    {
        EasyMock.expect(odmFactory.objectDirectoryMapperForClass(TestReferencedEntry.class)).andReturn(odm);
        replayMocks();

        try
        {
            editorFactory.referencedEntryEditorForClass(TestReferencedEntry.class);
            //second call to return cached editor, verified by the fact that objectDirectoryMapperForClass
            //is only called once.
            editorFactory.referencedEntryEditorForClass(TestReferencedEntry.class);
        }
        catch (MappingException e)
        {
            fail();
        }
        verifyMocks();
    }


    /**
     * Asserts that an exception is thrown when the referenced entity cannot be mapped
     *
     * @throws org.springframework.ldap.odm.mapping.MappingException
     *
     *
     */
    public void testReferencedEditorForClass_unsuccessfulMapping()
            throws MappingException
    {
        ReferencedEntryEditorFactory editorFactory =
                new ReferencedEntryEditorFactory(null, ldapTemplate);
        editorFactory.setObjectDirectoryMapperFactory(odmFactory);

        EasyMock.expect(odmFactory.objectDirectoryMapperForClass(TestReferencedEntry.class))
                .andThrow(new MappingException("unsuccessful mapping"));

        replayMocks();

        try
        {
            editorFactory.referencedEntryEditorForClass(TestReferencedEntry.class);
            fail("Should've thrown exception");
        }
        catch (MappingException e)
        {
            //expected behaviour
        }

        verifyMocks();
    }

    private void verifyMocks()
    {
        EasyMock.verify(ldapTemplate);
        EasyMock.verify(odmFactory);
        EasyMock.verify(odm);
    }

    private void replayMocks()
    {
        EasyMock.replay(ldapTemplate);
        EasyMock.replay(odmFactory);
        EasyMock.replay(odm);
    }


}
