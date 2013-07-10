package org.springframework.ldap.odm.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import jdepend.framework.JDepend;

import org.junit.Before;
import org.junit.Test;

public class TestsWithJdepend {
    private JDepend jdepend;

    @Before
    public void setUp() throws IOException {
        jdepend = new JDepend();
        jdepend.addDirectory("build/classes/main");
    }
    
    @Test
    public void testAllPackages() {
        jdepend.analyze();
        assertEquals(false, jdepend.containsCycles());
    }

}
