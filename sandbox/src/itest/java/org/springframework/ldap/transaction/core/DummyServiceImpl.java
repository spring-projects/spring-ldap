package org.springframework.ldap.transaction.core;

public class DummyServiceImpl {
    private DummyDao dummyDaoImpl;

    public void setDummyDaoImpl(DummyDao dummyDaoImpl) {
        this.dummyDaoImpl = dummyDaoImpl;
    }
}
