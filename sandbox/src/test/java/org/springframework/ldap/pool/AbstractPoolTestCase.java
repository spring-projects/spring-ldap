package org.springframework.ldap.pool;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.pool.KeyedObjectPool;
import org.easymock.MockControl;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.validation.DirContextValidator;

import junit.framework.TestCase;

/**
 * Contains mocks common to many tests for the connection pool.
 * 
 * @author Ulrik Sandberg
 */
public abstract class AbstractPoolTestCase extends TestCase {

    protected MockControl contextControl;

    protected Context contextMock;

    protected MockControl dirContextControl;

    protected DirContext dirContextMock;

    protected MockControl ldapContextControl;

    protected LdapContext ldapContextMock;

    protected MockControl keyedObjectPoolControl;

    protected KeyedObjectPool keyedObjectPoolMock;

    protected MockControl contextSourceControl;

    protected ContextSource contextSourceMock;

    protected MockControl dirContextValidatorControl;

    protected DirContextValidator dirContextValidatorMock;

    protected void setUp() throws Exception {
        super.setUp();

        contextControl = MockControl.createControl(Context.class);
        contextMock = (Context) contextControl.getMock();

        dirContextControl = MockControl.createControl(DirContext.class);
        dirContextMock = (DirContext) dirContextControl.getMock();

        ldapContextControl = MockControl.createControl(LdapContext.class);
        ldapContextMock = (LdapContext) ldapContextControl.getMock();

        keyedObjectPoolControl = MockControl
                .createControl(KeyedObjectPool.class);
        keyedObjectPoolMock = (KeyedObjectPool) keyedObjectPoolControl
                .getMock();

        contextSourceControl = MockControl.createControl(ContextSource.class);
        contextSourceMock = (ContextSource) contextSourceControl.getMock();

        dirContextValidatorControl = MockControl.createControl(DirContextValidator.class);
        dirContextValidatorMock = (DirContextValidator) dirContextValidatorControl.getMock();
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        contextControl = null;
        contextMock = null;

        dirContextControl = null;
        dirContextMock = null;

        ldapContextControl = null;
        ldapContextMock = null;

        keyedObjectPoolControl = null;
        keyedObjectPoolMock = null;

        contextSourceControl = null;
        contextSourceMock = null;

        dirContextValidatorControl = null;
        dirContextValidatorMock = null;
    }

    protected void replay() {
        contextControl.replay();
        dirContextControl.replay();
        ldapContextControl.replay();
        keyedObjectPoolControl.replay();
        contextSourceControl.replay();
        dirContextValidatorControl.replay();
    }

    protected void verify() {
        contextControl.verify();
        dirContextControl.verify();
        ldapContextControl.verify();
        keyedObjectPoolControl.verify();
        contextSourceControl.verify();
        dirContextValidatorControl.verify();
    }
}
