package org.springframework.ldap.ldif;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;
import org.springframework.ldap.core.LdapAttribute;
import org.springframework.ldap.ldif.support.DefaultAttributeValidationPolicy;

import sun.misc.BASE64Decoder;

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

	private static Log log = LogFactory.getLog(DefaultAttributeValidationPolicyTest.class);
	
	private static DefaultAttributeValidationPolicy policy = new DefaultAttributeValidationPolicy();

	private static enum AttributeType { STRING, BASE64, URL }
	
	private String line;
	private String id;
	private String options;
	private String value;
	private AttributeType type;
	
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
				
				//Base64
				{ "xml:: PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4=", "xml", "", "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4=", AttributeType.BASE64},
				{ "ou;lang-ja;phonetic:: 44GI44GE44GO44KH44GG44G2", "ou", ";lang-ja;phonetic", "44GI44GE44GO44KH44GG44G2", AttributeType.BASE64 },
				{ "dn:: dWlkPXJvZ2FzYXdhcmEsb3U95Za25qWt6YOoLG89QWlyaXVz", "dn", "", "dWlkPXJvZ2FzYXdhcmEsb3U95Za25qWt6YOoLG89QWlyaXVz", AttributeType.BASE64 },
				{ "cn;lang-ja:: 5bCP56yg5Y6fIOODreODieODi+ODvA==", "cn", ";lang-ja", "5bCP56yg5Y6fIOODreODieODi+ODvA==", AttributeType.BASE64 },
				
				//Url
				{ "url:< http://www.oracle.com/", "url", "", "http://www.oracle.com/", AttributeType.URL},
				{ "url:< http://java.sun.com/j2se/1.3/docs/guide/collections/designfaq.html", "url", "", "http://java.sun.com/j2se/1.3/docs/guide/collections/designfaq.html", AttributeType.URL},
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
				{ "url:< wais://vega.lib.ncsu.edu/alawon.src?nren", "url", "", "wais://vega.lib.ncsu.edu/alawon.src?nren", AttributeType.URL}
				
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
			
			assertTrue("IDs do not match: [expected: " + attribute.getID() + ", obtained: " + id + "]", id.equalsIgnoreCase(attribute.getID()));
			
			String[] expected = StringUtils.isEmpty(options) ? new String[] {} : options.replaceFirst(";","").split(";");
			Arrays.sort(expected);
			String[] obtained = attribute.getOptions().toArray(new String[] {});
			Arrays.sort(obtained);
			assertArrayEquals("Options do not match: ", expected, obtained);
			
			switch(type) {
			case STRING:
				assertTrue("Value is not a string.", attribute.get() instanceof String);
				assertEquals("Values do not match: ", value, (String) attribute.get());
				break;
				
			case BASE64:
				byte[] bytes = new BASE64Decoder().decodeBuffer(value);
				assertTrue("Value is not a byte[].", attribute.get() instanceof byte[]);
				assertArrayEquals("Values do not match: ", bytes, (byte[]) attribute.get());
				break;
				
			case URL:
				URI  url = new URI(value);
				assertTrue("Value is not a URL.", attribute.get() instanceof URI);
				assertEquals("Values do not match: ", url, (URI) attribute.get());
				break;
			}
			
			log.info("Success!");
			
		} catch (Exception e) {
			fail("Exception thrown: " + e.getClass().getSimpleName() + " (message: " + e.getMessage() + ")");
		}
	}
}
