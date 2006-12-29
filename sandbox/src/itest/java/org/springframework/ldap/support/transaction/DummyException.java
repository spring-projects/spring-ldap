package org.springframework.ldap.support.transaction;

public class DummyException extends RuntimeException {
    public DummyException(String message) {
        super(message);
    }
}
