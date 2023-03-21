package org.springframework.ldap.itest.odm;

import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

/**
 * @author Mattias Hellborg Arthursson
 */
@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" })
public class PersonWithDnAnnotations {

	@Id
	private Name dn;

	@Attribute(name = "cn")
	@DnAttribute(value = "cn", index = 2)
	private String commonName;

	@Attribute(name = "sn")
	private String surname;

	@Attribute(name = "description")
	private List<String> desc;

	@Attribute(name = "uid")
	private List<String> userId;

	@Attribute(name = "telephoneNumber")
	private String telephoneNumber;

	@DnAttribute(value = "ou", index = 1)
	@Transient
	private String company;

	@DnAttribute(value = "ou", index = 0)
	@Transient
	private String country;

	// operational attribute according to https://tools.ietf.org/html/rfc4530
	@Attribute(name = "entryUUID", readonly = true)
	private String entryUuid;

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

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getEntryUuid() {
		return entryUuid;
	}

}
