package org.jordijaspers.smc.eventify.test_support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import org.jordijaspers.eventify.Application;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Type;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = Application.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = "spring.profiles.include=itest"
)
public class WebMvcConfigurator extends BaseTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(WebMvcConfigurator.class);

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected HawaiiFilters filters;

    @Before
    public void setUpMockMvc() {
        LOGGER.info("Setting up RestAssuredMockMvc");
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
        LOGGER.info("Finished setting up RestAssuredMockMvc");
    }
}
