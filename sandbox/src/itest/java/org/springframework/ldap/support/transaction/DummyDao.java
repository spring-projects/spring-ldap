package org.springframework.ldap.support.transaction;

public interface DummyDao {
    void createWithException(String country, String company, String fullname,
            String lastname, String description);

    void create(String country, String company, String fullname,
            String lastname, String description);

    void update(String dn, String lastname, String description);

    void updateWithException(String dn, String lastname, String description);

    void updateAndRename(String dn, String newDn, String description);

    void updateAndRenameWithException(String dn, String newDn,
            String description);

    void modifyAttributes(String dn, String lastName, String description);

    void modifyAttributesWithException(String dn, String lastName,
            String description);

    void unbind(String dn);

    void unbindWithException(String dn);

}
