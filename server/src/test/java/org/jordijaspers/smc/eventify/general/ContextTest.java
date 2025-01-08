package org.jordijaspers.smc.eventify.general;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.jordijaspers.smc.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.io.IOException;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.PUBLIC_HEALTH_PATH;
import static org.jordijaspers.smc.eventify.support.config.ContainersConfiguration.DATABASE_NAME;

@AutoConfigureMockMvc
public class ContextTest extends IntegrationTest {

    @Test
    @DisplayName("Spring context loads successfully")
    public void contextLoads() {
        assertThat(context, is(notNullValue()));
    }

    @Test
    @DisplayName("Database container can start up")
    public void testDatabaseContainer() {
        assertThat(timescaleContainer.isRunning(), is(true));
    }

    @Test
    @DisplayName("Database connection is established")
    public void testDatabaseConnection() throws IOException, InterruptedException {
        assertThat(timescaleContainer.getJdbcUrl(), not(emptyString()));
        assertThat(timescaleContainer.execInContainer("psql", "-U", DATABASE_NAME, "-c", "SELECT 1;").getExitCode(), is(0));
    }

    @Test
    @DisplayName("Health endpoint returns OK")
    public void healthEndpoint() {
        // When: SMC approves the change data status change via TOP.
        // @formatter:off
        final MockMvcResponse response = given()
            .when()
            .get(PUBLIC_HEALTH_PATH)
            .andReturn();
        // @formatter:on

        // Then: The response is 200 - OK
        response.then().statusCode(SC_OK);

        // And: The response body contains the status UP
        response.then().body("status", equalTo("UP"));
    }


}
