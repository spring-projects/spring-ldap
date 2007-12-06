package se.jayway.demo.spring.ldap.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import se.jayway.demo.spring.ldap.service.GroupService;
import se.jayway.demo.spring.ldap.service.PersonService;

public class ListController extends AbstractController {

	private GroupService groupService;
	private PersonService personService;

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public final void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelMap map = new ModelMap();
		map.addObject("groups", groupService.findAll()).addObject("persons",
				personService.findAll());

		return new ModelAndView("list", map);
	}

}
