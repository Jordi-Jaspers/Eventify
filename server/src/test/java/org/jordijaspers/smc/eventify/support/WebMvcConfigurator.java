package org.jordijaspers.smc.eventify.support;

import io.restassured.config.ObjectMapperConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;

import java.lang.reflect.Type;

import org.jordijaspers.eventify.Application;
import org.jordijaspers.smc.eventify.support.config.BeanConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ActiveProfiles("test")
@Import(BeanConfiguration.class)
@SpringBootTest(
    classes = Application.class,
    webEnvironment = DEFINED_PORT
)
public class WebMvcConfigurator {

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected HawaiiFilters filters;

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
            .webAppContextSetup(this.context)
            .apply(springSecurity())
            .addFilters(filters.getFilters())
            .build();

        RestAssuredMockMvc.mockMvc(mockMvc);
    }
}
