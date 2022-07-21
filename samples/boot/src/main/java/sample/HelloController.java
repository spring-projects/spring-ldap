package sample;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
	@Autowired
	LdapTemplate ldap;

	@GetMapping
	public String hello(Authentication authentication) {
		return "Hello, " + authentication.getName();
	}

	@GetMapping("/cn")
	public List<String> cn(Authentication authentication) {
		AttributesMapper<String> mapper = (attrs) -> attrs.get("cn").get().toString();
		return this.ldap.search("ou=people", "uid=" + authentication.getName(), mapper);
	}
}
