/*
 * Copyright 2005 by Majitek. All Rights Reserved.
 *
 * This software is the proprietary information of Majitek. Use is subject to license terms.
 */

package org.springframework.ldap.odm.typeconversion;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.springframework.ldap.core.DistinguishedName;

public class NameEditorTest extends TestCase
{

    public void testThrowsExceptionWhenConstructorArgumentNotAName()
    {
        try
        {
            NameEditor nameEditor = new NameEditor(String.class);
            fail("Should've thrown exception.");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertEquals(e.getMessage(),
                    "NameEditor can only be created for LdapName or DistinguishedName");
        }
    }

    public void testGetAsText()
    {
        NameEditor editor = new NameEditor(DistinguishedName.class);
        DistinguishedName name = new DistinguishedName("uid=zzz, ou=people");
        editor.setValue(name);
        Assert.assertEquals(editor.getAsText(), "uid=zzz, ou=people");

        editor.setValue(null);
        Assert.assertEquals(editor.getAsText(), "");
    }

    public void testSetAsText()
    {
        NameEditor editor = new NameEditor(DistinguishedName.class);
        editor.setAsText("uid=zzz, ou=people");
        DistinguishedName expected = new DistinguishedName("uid=zzz, ou=people");
        Assert.assertEquals(editor.getValue(), expected);


    }


}
