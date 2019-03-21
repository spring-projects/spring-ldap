/*
 * Copyright 2005-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.ldif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.ldap.core.LdapAttribute;
import org.springframework.ldap.ldif.support.DefaultAttributeValidationPolicy;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Parses a preselected set of attributes to test the full spectrum of functionality
 * expected of an attribute parser.  Attributes are validated to ensure they conform to 
 * the requirements for attribute values prescribed in RFC2849.
 * 
 * @author Keith Barlow
 *
 */
@RunWith(Parameterized.class)
public class DefaultAttributeValidationPolicyTest {

	private static Logger log = LoggerFactory.getLogger(DefaultAttributeValidationPolicyTest.class);
	
	private static DefaultAttributeValidationPolicy policy = new DefaultAttributeValidationPolicy();

	private static enum AttributeType { STRING, BASE64, URL, UTF8 }
	
	private String line;
	private String id;
	private String options;
	private String value;
	private AttributeType type;
	
	private List<String> exceptions = Arrays.asList(new String[] {
			"description: :A big sailing fan.",
			"cn;lang-ja:: 5bCP56yg5Y6fIO.ODreODieODi+ODvA==",
			"url:< https://java.sun.com/j2se/1.3/docs/guide/collections/designfaq.html#28"
	});
	
	/**
	 * The data set to parse.
	 * @return
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				//Format: line, id, options, value, type
				
				//String
				{ "cn: Keith Barlow", "cn", "", "Keith Barlow", AttributeType.STRING}, 
				{ "sn: Jensen", "sn", "", "Jensen", AttributeType.STRING}, 
				{ "cn: Barbara J Jensen", "cn", "", "Barbara J Jensen", AttributeType.STRING}, 
				{ "telephonenumber: +1 408 555 1212", "telephonenumber", "", "+1 408 555 1212", AttributeType.STRING}, 
				{ "description: A big sailing fan.", "description", "", "A big sailing fan.", AttributeType.STRING}, 
				{ "title;lang-en;phonetic: Sales, Director", "title", ";lang-en;phonetic", "Sales, Director", AttributeType.STRING}, 
				{ "mail: rogasawara@airius.co.jp", "mail", "", "rogasawara@airius.co.jp", AttributeType.STRING}, 
				{ "description: A big sailing fan.", "description", "", "A big sailing fan.", AttributeType.STRING}, 
				{ "description: :A big sailing fan.", "description", "", ":A big sailing fan.", AttributeType.STRING}, 
				
				//Base64
				{ "xml:: PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4=", "xml", "", "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4=", AttributeType.BASE64},
				{ "ou;lang-ja;phonetic:: 44GI44GE44GO44KH44GG44G2", "ou", ";lang-ja;phonetic", "44GI44GE44GO44KH44GG44G2", AttributeType.BASE64 },
				{ "dn:: dWlkPXJvZ2FzYXdhcmEsb3U95Za25qWt6YOoLG89QWlyaXVz", "dn", "", "dWlkPXJvZ2FzYXdhcmEsb3U95Za25qWt6YOoLG89QWlyaXVz", AttributeType.BASE64 },
				{ "cn;lang-ja:: 5bCP56yg5Y6fIOODreODieODi+ODvA==", "cn", ";lang-ja", "5bCP56yg5Y6fIOODreODieODi+ODvA==", AttributeType.BASE64 },
				{ "cn;lang-ja:: 5bCP56yg5Y6fIO.ODreODieODi+ODvA==", "cn", ";lang-ja", "5bCP56yg5Y6fIO.ODreODieODi+ODvA==", AttributeType.BASE64 },
				
				//Url
				{ "url:< https://www.oracle.com/", "url", "", "https://www.oracle.com/", AttributeType.URL},
				{ "url:< https://java.sun.com/j2se/1.3/docs/guide/collections/designfaq.html", "url", "", "https://java.sun.com/j2se/1.3/docs/guide/collections/designfaq.html", AttributeType.URL},
				{ "url:< ftp://kbarlow:test@ftp.is.co.za/rfc/rfc1808.txt", "url", "", "ftp://kbarlow:test@ftp.is.co.za/rfc/rfc1808.txt", AttributeType.URL},
				{ "url;option:< ftp://ftp.is.co.za:2100/rfc/rfc1808.txt;type=a", "url", ";option", "ftp://ftp.is.co.za:2100/rfc/rfc1808.txt;type=a", AttributeType.URL},
				{ "url:< telnet://kbarlow@melvyl.ucop.edu/", "url", "", "telnet://kbarlow@melvyl.ucop.edu/", AttributeType.URL},
				{ "url;option1;option2:< telnet://kbarlow:test@melvyl.ucop.edu/", "url", ";option1;option2", "telnet://kbarlow:test@melvyl.ucop.edu/", AttributeType.URL},
				{ "url:< gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles", "url", "", "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles", AttributeType.URL},
				{ "url:< file:///usr/local/directory/photos/fiona.jpg", "url", "", "file:///usr/local/directory/photos/fiona.jpg", AttributeType.URL},
				{ "url:< mailto:java-net@java.sun.com", "url", "", "mailto:java-net@java.sun.com", AttributeType.URL},
				{ "url:< news:comp.infosystems.www.servers.unix", "url", "", "news:comp.infosystems.www.servers.unix", AttributeType.URL},
				{ "url:< prospero://host.dom:1525//pros/name;key=value", "url", "", "prospero://host.dom:1525//pros/name;key=value", AttributeType.URL},
				{ "url:< nntp://news.cs.hut.fi/alt.html/239157", "url", "", "nntp://news.cs.hut.fi/alt.html/239157", AttributeType.URL},
				{ "url:< wais://vega.lib.ncsu.edu/alawon.src?nren", "url", "", "wais://vega.lib.ncsu.edu/alawon.src?nren", AttributeType.URL},
				{ "url:< https://java.sun.com/j2se/1.3/docs/guide/collections/designfaq.html#28", "url", "", "https://java.sun.com/j2se/1.3/docs/guide/collections/designfaq.html#28", AttributeType.URL},
				
				//UTF8
				{ "company: Østfold Akershus", "company", "", "Østfold Akershus", AttributeType.STRING }
				
		});
	}

	/**
	 * DefaultAttributeValidationPolicyTest: Parameterized constructor.
	 * @param line The attribute to parse.
	 * @param id The ID portion of the attribute expected on successful parsing.
	 * @param options The Options expected on successful parsing.
	 * @param value The value expected from successful parsing.
	 * @param type The attribute type: one of enum AttributeType.
	 */
	public DefaultAttributeValidationPolicyTest(String line, String id, String options, String value, AttributeType type) {
		this.line = line;
		this.id = id;
		this.options = options;
		this.value = value;
		this.type = type;
	}

	/**
	 * The test case: parses passed in parameters and validates the outcome against the expected results.
	 */
	@Test
	public void parseAttribute() {
		try {
			LdapAttribute attribute = (LdapAttribute) policy.parse(line);
			
			assertThat(id.equalsIgnoreCase(attribute.getID())).as("IDs do not match: [expected: " + attribute.getID() + ", obtained: " + id + "]").isTrue();
			
			String[] expected = !StringUtils.hasLength(options) ? new String[] {} : options.replaceFirst(";","").split(";");
			Arrays.sort(expected);
			String[] obtained = attribute.getOptions().toArray(new String[] {});
			Arrays.sort(obtained);
			assertThat(obtained).as("Options do not match: ").isEqualTo(expected);
			
			switch(type) {
			case STRING:
				assertThat(attribute.get() instanceof String).as("Value is not a string.").isTrue();
				assertThat(attribute.get()).as("Values do not match: ").isEqualTo(value);
				break;
				
			case BASE64:
				byte[] bytes = LdapEncoder.parseBase64Binary(value);
				assertThat(attribute.get() instanceof byte[]).as("Value is not a byte[].").isTrue();
				assertThat((byte[]) attribute.get()).as("Values do not match: ").isEqualTo(bytes);
				break;
				
			case URL:
				URI  url = new URI(value);
				assertThat(attribute.get() instanceof URI).as("Value is not a URL.").isTrue();
				assertThat(attribute.get()).as("Values do not match: ").isEqualTo(url);
				break;
				
			case UTF8:
				assertThat(attribute.get() instanceof String).as("Value is not a UTF8.").isTrue();
				assertThat(attribute.get()).as("Values do not match: ").isEqualTo(value);
				break;
			}
			
			log.info("Success!");
			
		} catch (Exception e) {
			if (!exceptions.contains(line))
				fail("Exception thrown: " + e.getClass().getSimpleName() + " (message: " + e.getMessage() + ")");
		}
	}
}
