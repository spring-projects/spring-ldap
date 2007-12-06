package se.jayway.demo.spring.ldap.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import se.jayway.demo.spring.ldap.service.GroupService;

public class GroupController extends AbstractController {

	private GroupService groupService;

	public final void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String name = request.getParameter("name");
		return new ModelAndView("showgroup", "group", groupService
				.findByPrimaryKey(name));
	}

}
