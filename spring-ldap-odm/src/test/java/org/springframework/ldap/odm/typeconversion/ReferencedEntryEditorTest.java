/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.typeconversion;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.odm.mapping.MappingException;
import org.springframework.ldap.odm.mapping.ObjectDirectoryMapper;

public class ReferencedEntryEditorTest extends TestCase
{
    private LdapTemplate ldapTemplate;
    private ObjectDirectoryMapper objectDirectoryMapper;
    private ReferencedEntryEditor editor;

    protected void setUp() throws Exception
    {
        super.setUp();
        ldapTemplate = EasyMock.createStrictMock(LdapTemplate.class);
        objectDirectoryMapper = EasyMock.createStrictMock(ObjectDirectoryMapper.class);
        editor = new ReferencedEntryEditor(SavePolicy.SUPPRESS_REFERENTIAL_INTEGRITY_EXCEPTIONS,
                LoadPolicy.SUPPRESS_REFERENTIAL_INTEGRITY_EXCEPTIONS,
                new DistinguishedName("dc=example, dc=com"), ldapTemplate, objectDirectoryMapper);
    }

    public void testGetAsText() throws MappingException
    {
        TestReferencedEntry referencedEntry = new TestReferencedEntry();
        editor.setValue(referencedEntry);

        EasyMock.expect(objectDirectoryMapper.buildDn(referencedEntry))
                .andReturn(new DistinguishedName("uid=referencedEntry, ou=foobars"));

        replayMocks();
        editor.getAsText();
        verifyMocks();
    }

    public void testGetAsTextThrowsRuntimeExceptionWhenMappingExceptionOccurs()
            throws MappingException
    {
        TestReferencedEntry referencedEntry = new TestReferencedEntry();
        editor.setValue(referencedEntry);

        EasyMock.expect(objectDirectoryMapper.buildDn(referencedEntry))
                .andThrow(new MappingException("no mapper"));

        replayMocks();
        try
        {
            editor.getAsText();
            fail("Should've propogated mapping exception.");
        }
        catch (RuntimeException e)
        {
            //expected
        }

        verifyMocks();
    }

    public void testSetAsText()
    {
        DistinguishedName referenceName =
                new DistinguishedName("uid = referencedEntry, ou=foobars");
        TestReferencedEntry referencedEntry = new TestReferencedEntry();

        EasyMock.expect(ldapTemplate.lookup(referenceName, objectDirectoryMapper))
                .andReturn(referencedEntry);

        replayMocks();
        editor.setAsText("uid = referencedEntry, ou=foobars");

        verifyMocks();
    }

    public void testSetAsTextRemovingBaseDn()
    {
        DistinguishedName referenceName =
                new DistinguishedName("uid = referencedEntry, ou=foobars");
        TestReferencedEntry referencedEntry = new TestReferencedEntry();


        EasyMock.expect(ldapTemplate.lookup(referenceName, objectDirectoryMapper))
                .andReturn(referencedEntry);

        replayMocks();
        editor.setAsText("uid = referencedEntry, ou=foobars, dc=example, dc=com");
        verifyMocks();
    }

    private void verifyMocks()
    {
        EasyMock.verify(ldapTemplate);
        EasyMock.verify(objectDirectoryMapper);
    }

    private void replayMocks()
    {
        EasyMock.replay(ldapTemplate);
        EasyMock.replay(objectDirectoryMapper);
    }


}
