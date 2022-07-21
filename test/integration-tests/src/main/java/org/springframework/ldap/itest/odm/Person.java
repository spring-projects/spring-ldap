package org.springframework.ldap.itest.odm;

import java.util.List;

import javax.naming.Name;

import org.springframework.data.domain.Persistable;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

/**
 * @author Mattias Hellborg Arthursson
 */
@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" })
public class Person implements Persistable<Name> {
	@Id
	private Name dn;

	@Attribute(name = "cn")
	private String commonName;

	@Attribute(name = "sn")
	private String surname;

	@Attribute(name = "description")
	private List<String> desc;

	@Attribute(name = "uid")
	private List<String> userId;

	@Attribute(name = "telephoneNumber")
	private String telephoneNumber;

	@Transient
	private boolean isNew = false;

	// operational attribute according to https://tools.ietf.org/html/rfc4530
	@Attribute(name = "entryUUID", readonly = true)
	private String entryUuid;

	@Override
	public Name getId() {
		return getDn();
	}

	public void setNew() {
		isNew = true;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	public Name getDn() {
		return dn;
	}

	public void setDn(Name dn) {
		this.dn = dn;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public List<String> getDesc() {
		return desc;
	}

	public void setDesc(List<String> desc) {
		this.desc = desc;
	}

	public List<String> getUserId() {
		return userId;
	}

	public void setUserId(List<String> userId) {
		this.userId = userId;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getEntryUuid() {
		return entryUuid;
	}
}
