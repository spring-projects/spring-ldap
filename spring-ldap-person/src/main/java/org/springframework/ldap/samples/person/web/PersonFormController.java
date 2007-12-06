package se.jayway.demo.spring.ldap.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.SimpleFormController;

import se.jayway.demo.spring.ldap.domain.Person;
import se.jayway.demo.spring.ldap.service.PersonService;

public class PersonFormController extends SimpleFormController {

	private PersonService personService;
	
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {

		String fullName = (String) request.getParameter("name");
		String country = (String) request.getParameter("country");
		String company = (String) request.getParameter("company");

		return personService.findByPrimaryKey(country, company, fullName);
	}
	
	public final void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	@Override
	protected void doSubmitAction(Object command) throws Exception {
		personService.update((Person)command);
	}
}
