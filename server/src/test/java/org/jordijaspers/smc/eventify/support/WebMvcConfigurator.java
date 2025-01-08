package org.jordijaspers.smc.eventify.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import lombok.extern.slf4j.Slf4j;
import org.jordijaspers.eventify.Application;
import org.jordijaspers.smc.eventify.support.config.BeanConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Type;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Slf4j
@Disabled
@Import(BeanConfiguration.class)
@ActiveProfiles("test")
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
        log.info("Setting up RestAssuredMockMvc");
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
        log.info("Finished setting up RestAssuredMockMvc");
    }
}
