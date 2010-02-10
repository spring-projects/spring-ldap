package org.springframework.ldap.samples.article.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.samples.article.dao.PersonDao;
import org.springframework.ldap.samples.article.domain.Person;
import org.springframework.ldap.samples.utils.HtmlRowLdapTreeVisitor;
import org.springframework.ldap.samples.utils.LdapTree;
import org.springframework.ldap.samples.utils.LdapTreeBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Default controller.
 * 
 * @author Mattias Hellborg Arthursson
 */
@Controller
public class DefaultController {

	@Autowired
	private LdapTreeBuilder ldapTreeBuilder;

	@Autowired
	private PersonDao personDao;

	@RequestMapping("/welcome.do")
	public void welcomeHandler() {
	}

	@RequestMapping("/showTree.do")
	public ModelAndView showTree() {
		LdapTree ldapTree = ldapTreeBuilder.getLdapTree(DistinguishedName.EMPTY_PATH);
		HtmlRowLdapTreeVisitor visitor = new PersonLinkHtmlRowLdapTreeVisitor();
		ldapTree.traverse(visitor);
		return new ModelAndView("showTree", "rows", visitor.getRows());
	}

	@RequestMapping("/addPerson.do")
	public ModelAndView addPerson() {
		Person person = getPerson();

		personDao.create(person);
		return showTree();
	}

	@RequestMapping("/updatePhoneNumber.do")
	public ModelAndView updatePhoneNumber() {
		Person person = personDao.findByPrimaryKey("Sweden", "company1", "John Doe");
		person.setPhone(StringUtils.join(new String[] { person.getPhone(), "0" }));

		personDao.update(person);
		return showTree();
	}

	@RequestMapping("/removePerson.do")
	public ModelAndView removePerson() {
		Person person = getPerson();

		personDao.delete(person);
		return showTree();
	}

	@RequestMapping("/showPerson.do")
	public ModelMap showPerson(String country, String company, String fullName) {
		Person person = personDao.findByPrimaryKey(country, company, fullName);
		return new ModelMap("person", person);
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
