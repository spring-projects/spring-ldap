package org.springframework.ldap.transaction.compensating.manager.hibernate;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.transaction.compensating.manager.DummyException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
/**
 * @author Hans Westerbeek
 */
public class DummyDaoLdapAndHibernateImpl extends HibernateDaoSupport implements OrgPersonDao {

	private LdapTemplate ldapTemplate;
	
	public void create(OrgPerson person) {
		DistinguishedName dn = new DistinguishedName();
        dn.add("c", person.getCountry());
        dn.add("ou", person.getCompany());
        dn.add("cn", person.getFullname());

        DirContextAdapter ctx = new DirContextAdapter();
        ctx.setAttributeValues("objectclass", new String[] { "top", "person" });
        ctx.setAttributeValue("cn", person.getFullname());
        ctx.setAttributeValue("sn", person.getLastname());
        ctx.setAttributeValue("description", person.getDescription());
        ldapTemplate.bind(dn, ctx, null);
		this.getHibernateTemplate().saveOrUpdate(person);
		
		
	}

	public void createWithException(OrgPerson person) {
		this.create(person);
		throw new DummyException("This method failed");
		
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

	public void unbind(OrgPerson person) {
		String dn = prepareDn(person);
		ldapTemplate.unbind(dn);
        this.getHibernateTemplate().delete(person);
		
	}

	public void unbindWithException(OrgPerson person) {
		this.unbind(person);
		throw new DummyException("This method failed");
	}

	public void update(OrgPerson person) {
		String dn = prepareDn(person);
		DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
        ctx.setAttributeValue("sn", person.getLastname());
        ctx.setAttributeValue("description", person.getDescription());
        ctx.update();

        ldapTemplate.rebind(dn, ctx, null);
        this.getHibernateTemplate().saveOrUpdate(person);
		
	}

	public void updateWithException(OrgPerson person) {
		this.update(person);
		throw new DummyException("This method failed");
	}
	
	public void updateAndRename(String dn, String newDn, String updatedDescription) {
		
		DirContextAdapter ctx = (DirContextAdapter) ldapTemplate.lookup(dn);
        ctx.setAttributeValue("description", updatedDescription);
        ctx.update();

        ldapTemplate.rebind(dn, ctx, null);
        ldapTemplate.rename(dn, newDn);
		
	}

	public void updateAndRenameWithException(String dn, String newDn, String updatedDescription) {
		this.updateAndRename(dn, newDn, updatedDescription);
		throw new DummyException("This method failed");
	}

	

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
	
	private String prepareDn(OrgPerson person){
		return "cn=" + person.getFullname() + ",ou=" + person.getCompany() + ",c=" + person.getCountry();
	}

}
