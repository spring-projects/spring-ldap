package org.springframework.ldap.samples.article.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.samples.article.dao.PersonDao;
import org.springframework.ldap.samples.article.domain.Person;
import org.springframework.ldap.samples.utils.HtmlRowLdapTreeVisitor;
import org.springframework.ldap.samples.utils.LdapTree;
import org.springframework.ldap.samples.utils.LdapTreeBuilder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Default controller.
 * 
 * @author Mattias Hellborg Arthursson
 * @author Ulrik Sandberg
 */
public class DefaultController extends MultiActionController {

	private LdapTreeBuilder ldapTreeBuilder;

	public void setLdapTreeBuilder(LdapTreeBuilder ldapTreeBuilder) {
		this.ldapTreeBuilder = ldapTreeBuilder;
	}

	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

	private PersonDao personDao;

	public ModelAndView welcomeHandler(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("welcome");
	}

	public ModelAndView showTree(HttpServletRequest request, HttpServletResponse response) {
		LdapTree ldapTree = ldapTreeBuilder.getLdapTree(DistinguishedName.EMPTY_PATH);
		HtmlRowLdapTreeVisitor visitor = new PersonLinkHtmlRowLdapTreeVisitor();
		ldapTree.traverse(visitor);
		return new ModelAndView("showTree", "rows", visitor.getRows());
	}

	public ModelAndView addPerson(HttpServletRequest request, HttpServletResponse response) {
		Person person = getPerson();

		personDao.create(person);
		return showTree(request, response);
	}

	public ModelAndView updatePhoneNumber(HttpServletRequest request, HttpServletResponse response) {
		Person person = personDao.findByPrimaryKey("Sweden", "company1", "John Doe");
		person.setPhone(StringUtils.join(new String[] { person.getPhone(), "0" }));

		personDao.update(person);
		return showTree(request, response);
	}

	public ModelAndView removePerson(HttpServletRequest request, HttpServletResponse response) {
		Person person = getPerson();

		personDao.delete(person);
		return showTree(request, response);
	}

	public ModelAndView showPerson(HttpServletRequest request, HttpServletResponse response, Person query) {
		String country = query.getCountry();
		String company = query.getCompany();
		String fullName = query.getFullName();
		Person person = personDao.findByPrimaryKey(country, company, fullName);
		return new ModelAndView("showPerson", "person", person);
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

	/**
	 * Generates appropriate links for person leaves in the tree.
	 * 
	 * @author Mattias Hellborg Arthursson
	 */
	private static final class PersonLinkHtmlRowLdapTreeVisitor extends HtmlRowLdapTreeVisitor {
		@Override
		protected String getLinkForNode(DirContextOperations node) {
			String[] objectClassValues = node.getStringAttributes("objectClass");
			if (containsValue(objectClassValues, "person")) {
				DistinguishedName distinguishedName = (DistinguishedName) node.getDn();
				String country = encodeValue(distinguishedName.getValue("c"));
				String company = encodeValue(distinguishedName.getValue("ou"));
				String fullName = encodeValue(distinguishedName.getValue("cn"));

				return "showPerson.do?country=" + country + "&company=" + company + "&fullName=" + fullName;
			}
			else {
				return super.getLinkForNode(node);
			}
		}

		private String encodeValue(String value) {
			try {
				return URLEncoder.encode(value, "UTF8");
			}
			catch (UnsupportedEncodingException e) {
				// Not supposed to happen
				throw new RuntimeException("Unexpected encoding exception", e);
			}
		}

		private boolean containsValue(String[] values, String value) {
			for (String oneValue : values) {
				if (StringUtils.equals(oneValue, value)) {
					return true;
				}
			}
			return false;
		}
	}
}
