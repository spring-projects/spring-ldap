package org.springframework.ldap.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NameAwareAttributesTest {
    // gh-548
    @Test
    public void removeWhenDifferentCaseThenRemoves() {
        NameAwareAttributes attributes = new NameAwareAttributes();
        attributes.put("myID", "value");
        attributes.put("myOtherID", "othervalue");
        assertThat(attributes.size()).isEqualTo(2);
        assertThat(attributes.get("myid").get()).isEqualTo("value");
        assertThat(attributes.get("myID").get()).isEqualTo("value");

        attributes.remove("myid");
        assertThat(attributes.get("myID")).isNull();
        assertThat(attributes.size()).isEqualTo(1);

        attributes.remove("myOtherID");
        assertThat(attributes.size()).isEqualTo(0);
    }
}
