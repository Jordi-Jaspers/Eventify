package org.jordijaspers.smc.eventify.support;

import io.restassured.config.ObjectMapperConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;

import java.lang.reflect.Type;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * This class is used to configure the WebMvc environment for the tests.
 */
public class WebMvcConfigurator extends TestContextInitializer {

    @BeforeEach
    public void setUpMockMvc() {
        RestAssuredMockMvc.config = RestAssuredMockMvcConfig.config().objectMapperConfig(
            new ObjectMapperConfig().jackson2ObjectMapperFactory((final Type type, final String s) -> {
                ObjectMapper om = new ObjectMapper();
                om.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
                om.registerModule(new JavaTimeModule());
                om.findAndRegisterModules();
                return om;
            })
        );

        final MockMvc mockMvc = MockMvcBuilders
            .webAppContextSetup(applicationContext)
            .apply(springSecurity())
            .addFilters(hawaiiFilters.getFilters())
            .build();

        RestAssuredMockMvc.mockMvc(mockMvc);
    }

}
