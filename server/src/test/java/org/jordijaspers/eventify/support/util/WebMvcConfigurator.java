package org.jordijaspers.eventify.support.util;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;


/**
 * This class is used to configure the WebMvc environment for the tests.
 */
public class WebMvcConfigurator extends TestContextInitializer {

    protected MockMvc mockMvc;

    @BeforeEach
    public void setUpMockMvc() {
        this.mockMvc = configureMockMvc();
    }

    private MockMvc configureMockMvc() {
        return MockMvcBuilders
            .webAppContextSetup(applicationContext)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .addFilter(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
            .addFilters(hawaiiFilters.getFilters())
            .build();
    }
}
