package org.springframework.ldap.test.unboundid;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;

public class EmbeddedLdapServerTest {

    private static String tempLogFile;

    @BeforeClass
    public static void before() throws IOException {
        tempLogFile = Files.createTempFile("ldap-log-", ".txt").toAbsolutePath().toString();
    }

    @Test
    public void testServerStartup_withCustomConfig() {

        EmbeddedLdapServer.Builder serverBuilder = EmbeddedLdapServer.Builder
            .withEntry(null)
            .withPort(1234)
            .withConfigurationCustomizer(config -> config.setCodeLogDetails(tempLogFile, true));

        try (EmbeddedLdapServer server = serverBuilder.build()) {
            server.start();

            ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "/applicationContext-testContextSource-withCustomInterceptor.xml");
            LdapTemplate ldapTemplate = ctx.getBean(LdapTemplate.class);
            assertThat(ldapTemplate).isNotNull();

            ldapTemplate.search(LdapQueryBuilder.query().where("objectclass").is("person"), new AttributesMapper<>() {
                public String mapFromAttributes(Attributes attrs) throws NamingException {
                    return (String) attrs.get("cn").get();
                }
            });

            assertThat(Path.of(tempLogFile)).isNotEmptyFile();
        }

    }

}