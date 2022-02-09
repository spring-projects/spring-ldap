package sample;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpringLdapSimpleSampleApplicationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void indexWhenCorrectUsernameAndPasswordThenAuthenticates() throws Exception {
        HttpHeaders http = new HttpHeaders();
        http.setBasicAuth("bob", "bobspassword");
        this.mvc.perform(get("/").headers(http))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, bob"));
    }

    @Test
    void cnWhenCorrectUsernameAndPasswordThenShowsCommonName() throws Exception {
        HttpHeaders http = new HttpHeaders();
        http.setBasicAuth("bob", "bobspassword");
        this.mvc.perform(get("/cn").headers(http))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0]").value("Bob Hamilton"));
    }
}
