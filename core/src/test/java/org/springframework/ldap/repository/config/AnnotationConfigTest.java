package org.springframework.ldap.repository.config;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.config.DummyLdapRepository;

import static org.junit.Assert.assertNotNull;

/**
 * @author Mattias Hellborg Arthursson
 */
public class AnnotationConfigTest {

    @Test
    public void testAnnotationConfig() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/ldap-annotation-config.xml");

        DummyLdapRepository repository = ctx.getBean(DummyLdapRepository.class);
        assertNotNull(repository);
    }
}
