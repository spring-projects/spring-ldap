package org.springframework.ldap.transaction.core;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;

public class LdapAndJdbcDummyDaoImpl implements DummyDao {
    private LdapTemplate ldapTemplate;

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#createWithException(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void createWithException(String country, String company,
            String fullname, String lastname, String description) {
        create(country, company, fullname, lastname, description);
        throw new DummyException("This method failed");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#create(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
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
        jdbcTemplate.update("insert into PERSON values(?, ?, ?)", new Object[] {
                fullname, lastname, description });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#update(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void update(String dn, String fullname, String lastname,
            String description) {
        DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
        ctx.setAttributeValue("sn", lastname);
        ctx.setAttributeValue("description", description);
        ctx.update();

        ldapTemplate.rebind(dn, ctx, null);
        jdbcTemplate
                .update(
                        "update PERSON set lastname=?, description = ? where fullname = ?",
                        new Object[] { lastname, description, fullname });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#updateWithException(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void updateWithException(String dn, String fullname,
            String lastname, String description) {
        update(dn, fullname, lastname, description);
        throw new DummyException("This method failed.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#updateAndRename(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void updateAndRename(String dn, String newDn, String description) {
        DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
        ctx.setAttributeValue("description", description);
        ctx.update();

        ldapTemplate.rebind(dn, ctx, null);
        ldapTemplate.rename(dn, newDn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#updateAndRenameWithException(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void updateAndRenameWithException(String dn, String newDn,
            String description) {
        updateAndRename(dn, newDn, description);
        throw new DummyException("This method failed.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#modifyAttributes(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void modifyAttributes(String dn, String lastName, String description) {
        DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
        ctx.setAttributeValue("sn", lastName);
        ctx.setAttributeValue("description", description);

        ldapTemplate.modifyAttributes(dn, ctx.getModificationItems());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#modifyAttributesWithException(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void modifyAttributesWithException(String dn, String lastName,
            String description) {
        modifyAttributes(dn, lastName, description);
        throw new DummyException("This method failed.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#unbind(java.lang.String)
     */
    public void unbind(String dn, String fullname) {
        ldapTemplate.unbind(dn);
        jdbcTemplate.update("delete from PERSON where fullname=?",
                new Object[] { fullname });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.transaction.core.DummyDao#unbindWithException(java.lang.String)
     */
    public void unbindWithException(String dn, String fullname) {
        unbind(dn, fullname);
        throw new DummyException("This operation failed.");
    }
}
