package org.springframework.ldap.itest.transaction.compensating.manager.hibernate;

public interface OrgPersonDao {
	void createWithException(OrgPerson person);

    void create(OrgPerson person);

    void update(OrgPerson person);

    void updateWithException(OrgPerson person);

    void updateAndRename(String dn, String newDn, String updatedDescription);

    void updateAndRenameWithException(String dn, String newDn, String updatedDescription);

    void modifyAttributes(String dn, String lastName, String description);

    void modifyAttributesWithException(String dn, String lastName, String description);

    void unbind(OrgPerson person);

    void unbindWithException(OrgPerson person);
}
