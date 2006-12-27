package org.springframework.ldap.support.transaction;

import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DistinguishedName;

public class DummyDaoImpl {
    private LdapTemplate ldapTemplate;

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void createWithException(String country, String company,
            String fullname, String lastname, String description) {
        create(country, company, fullname, lastname, description);
        throw new RuntimeException("This method failed");
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
        throw new RuntimeException("This method failed.");
    }
}
