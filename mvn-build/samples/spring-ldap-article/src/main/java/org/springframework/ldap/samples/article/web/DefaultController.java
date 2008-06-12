package org.springframework.ldap.samples.article.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.samples.article.dao.PersonDao;
import org.springframework.ldap.samples.article.domain.Person;
import org.springframework.ldap.samples.utils.HtmlRowLdapTreeVisitor;
import org.springframework.ldap.samples.utils.LdapTree;
import org.springframework.ldap.samples.utils.LdapTreeBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DefaultController {

	@Autowired
	private LdapTreeBuilder ldapTreeBuilder;

	@Autowired
	private PersonDao personDao;

	@RequestMapping("/welcome.do")
	public void welcomeHandler() {
	}

	@RequestMapping("/doStuff.do")
	public ModelAndView showTree() {
		LdapTree ldapTree = ldapTreeBuilder.getLdapTree(DistinguishedName.EMPTY_PATH);
		HtmlRowLdapTreeVisitor visitor = new HtmlRowLdapTreeVisitor();
		ldapTree.traverse(visitor);
		return new ModelAndView("showTree", "rows", visitor.getRows());
	}

	@RequestMapping("/addPerson.do")
	public ModelAndView addPerson() {
		Person person = getPerson();

		personDao.create(person);
		return showTree();
	}

	@RequestMapping("/removePerson.do")
	public ModelAndView removePerson() {
		Person person = getPerson();

		personDao.delete(person);
		return showTree();
	}
	
	
	private Person getPerson() {
		Person person = new Person();
		person.setFullName("John Doe");
		person.setLastName("Doe");
		person.setCompany("company1");
		person.setCountry("Sweden");
		person.setDescription("Test user");
		return person;
	}

}
