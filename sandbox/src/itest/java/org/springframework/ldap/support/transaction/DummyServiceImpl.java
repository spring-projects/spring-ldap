package org.springframework.ldap.support.transaction;

public class DummyServiceImpl {
    private DummyDao dummyDaoImpl;

    public void setDummyDaoImpl(DummyDao dummyDaoImpl) {
        this.dummyDaoImpl = dummyDaoImpl;
    }
}
