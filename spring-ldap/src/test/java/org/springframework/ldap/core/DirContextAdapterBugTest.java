/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.core;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import junit.framework.TestCase;


/**
 * Unit tests that serve as regression tests for bugs that have been fixed.
 * 
 * @author Luke Taylor
 */
public class DirContextAdapterBugTest extends TestCase {

    public void testResetAttributeValuesNotReportedAsModifications() {
        BasicAttributes attrs = new BasicAttributes("myattr", "a");
        attrs.get("myattr").add("b");
        attrs.get("myattr").add("c");
        UpdateAdapter ctx = new UpdateAdapter(attrs, new DistinguishedName());

        ctx.setAttributeValues("myattr", new String[] { "a", "b" });
        ctx.setAttributeValues("myattr", new String[] { "a", "b", "c" });

        assertEquals(0, ctx.getModificationItems().length);
    }

    public void testResetAttributeValuesSameLengthNotReportedAsModifications() {
        BasicAttributes attrs = new BasicAttributes("myattr", "a");
        attrs.get("myattr").add("b");
        attrs.get("myattr").add("c");
        UpdateAdapter ctx = new UpdateAdapter(attrs, new DistinguishedName());

        ctx.setAttributeValues("myattr", new String[] { "a", "b", "d" });
        ctx.setAttributeValues("myattr", new String[] { "a", "b", "c" });

        assertEquals(0, ctx.getModificationItems().length);
    }

    /**
     * This test starts with an array with a null value in it (because that's
     * how BasicAttributes will do it), changes to <code>[a]</code>, and then
     * changes to <code>null</code>. The current code interprets this as a
     * change and will replace the original array with an empty array.
     * 
     * TODO Is this correct behaviour?
     */
    public void testResetNullAttributeValuesReportedAsModifications() {
        BasicAttributes attrs = new BasicAttributes("myattr", null);
        UpdateAdapter ctx = new UpdateAdapter(attrs, new DistinguishedName());

        ctx.setAttributeValues("myattr", new String[] { "a" });
        ctx.setAttributeValues("myattr", null);

        assertEquals(1, ctx.getModificationItems().length);
    }

    public void testResetNullAttributeValueNotReportedAsModification() throws Exception {
        BasicAttributes attrs = new BasicAttributes("myattr", "b");
        UpdateAdapter ctx = new UpdateAdapter(attrs, new DistinguishedName());

        ctx.setAttributeValue("myattr", "a");
        ctx.setAttributeValue("myattr", "b");

        assertEquals(0, ctx.getModificationItems().length);
    }

    private static class UpdateAdapter extends DirContextAdapter {
        public UpdateAdapter(Attributes attrs, Name dn) {
            super(attrs, dn);
            setUpdateMode(true);
        }
    }
}
