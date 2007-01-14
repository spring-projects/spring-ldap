package org.springframework.ldap.support.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;

public class DummyDaoImpl implements DummyDao{
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
     * @see org.springframework.ldap.support.transaction.DummyDao#createWithException(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#createWithException(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createWithException(String country, String company,
            String fullname, String lastname, String description) {
        create(country, company, fullname, lastname, description);
        throw new DummyException("This method failed");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.DummyDao#create(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#create(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
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
//        jdbcTemplate.execute("insert into test values(1, 'kalle', 'pettersson', 123)");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.DummyDao#update(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#update(java.lang.String, java.lang.String, java.lang.String)
     */
    public void update(String dn, String lastname, String description) {
        DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
        ctx.setAttributeValue("sn", lastname);
        ctx.setAttributeValue("description", description);
        ctx.update();

        ldapTemplate.rebind(dn, ctx, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.DummyDao#updateWithException(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#updateWithException(java.lang.String, java.lang.String, java.lang.String)
     */
    public void updateWithException(String dn, String lastname,
            String description) {
        update(dn, lastname, description);
        throw new DummyException("This method failed.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.DummyDao#updateAndRename(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#updateAndRename(java.lang.String, java.lang.String, java.lang.String)
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
     * @see org.springframework.ldap.support.transaction.DummyDao#updateAndRenameWithException(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#updateAndRenameWithException(java.lang.String, java.lang.String, java.lang.String)
     */
    public void updateAndRenameWithException(String dn, String newDn,
            String description) {
        updateAndRename(dn, newDn, description);
        throw new DummyException("This method failed.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.DummyDao#modifyAttributes(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#modifyAttributes(java.lang.String, java.lang.String, java.lang.String)
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
     * @see org.springframework.ldap.support.transaction.DummyDao#modifyAttributesWithException(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#modifyAttributesWithException(java.lang.String, java.lang.String, java.lang.String)
     */
    public void modifyAttributesWithException(String dn, String lastName,
            String description) {
        modifyAttributes(dn, lastName, description);
        throw new DummyException("This method failed.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.DummyDao#unbind(java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#unbind(java.lang.String)
     */
    public void unbind(String dn) {
        ldapTemplate.unbind(dn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.DummyDao#unbindWithException(java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.springframework.ldap.support.transaction.TempDummyDao#unbindWithException(java.lang.String)
     */
    public void unbindWithException(String dn) {
        unbind(dn);
        throw new DummyException("This operation failed.");
    }
}
