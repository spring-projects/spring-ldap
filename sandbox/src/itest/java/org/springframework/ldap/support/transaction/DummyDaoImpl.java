package org.springframework.ldap.support.transaction;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;

public class DummyDaoImpl {
    private LdapTemplate ldapTemplate;

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void createWithException(String country, String company,
            String fullname, String lastname, String description) {
        create(country, company, fullname, lastname, description);
        throw new DummyException("This method failed");
    }

    public void create(String country, String company, String fullname,
            String lastname, String description) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("c", country);
        dn.add("ou", company);
        dn.add("cn", fullname);

        DirContextAdapter ctx = new DirContextAdapter();
        ctx.setAttributeValues("objectclass", new String[] { "top", "person" });
        ctx.setAttributeValue("cn", fullname);
        ctx.setAttributeValue("sn", lastname);
        ctx.setAttributeValue("description", description);
        ldapTemplate.bind(dn, ctx, null);
    }

    public void update(String dn, String lastname, String description) {
        DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
        ctx.setAttributeValue("sn", lastname);
        ctx.setAttributeValue("description", description);
        ctx.update();

        ldapTemplate.rebind(dn, ctx, null);
    }

    public void updateWithException(String dn, String lastname,
            String description) {
        update(dn, lastname, description);
        throw new DummyException("This method failed.");
    }

    public void updateAndRename(String dn, String newDn, String description) {
        DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
        ctx.setAttributeValue("description", description);
        ctx.update();

        ldapTemplate.rebind(dn, ctx, null);

        ldapTemplate.rename(dn, newDn);
    }

    public void updateAndRenameWithException(String dn, String newDn,
            String description) {
        updateAndRename(dn, newDn, description);
        throw new DummyException("This method failed.");
    }

    public void modifyAttributes(String dn, String lastName, String description) {
        DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
        ctx.setAttributeValue("sn", lastName);
        ctx.setAttributeValue("description", description);

        ldapTemplate.modifyAttributes(dn, ctx.getModificationItems());
    }

    public void modifyAttributesWithException(String dn, String lastName,
            String description) {
        modifyAttributes(dn, lastName, description);
        throw new DummyException("This method failed.");
    }

    public void unbind(String dn) {
        ldapTemplate.unbind(dn);
    }

    public void unbindWithException(String dn) {
        unbind(dn);
        throw new DummyException("This operation failed.");
    }
}
