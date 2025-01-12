package org.jordijaspers.eventify.general;

import io.restassured.module.mockmvc.response.MockMvcResponse;

import java.io.IOException;

import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.*;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.PUBLIC_HEALTH_PATH;
import static org.jordijaspers.eventify.support.container.TimescaleContainer.DATABASE_NAME;

/**
 * Test class to verify the context is loaded correctly.
 */
@Order(1)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApplicationContextTest extends IntegrationTest {

    @Test
    @Order(1)
    @DisplayName("Spring context loads successfully")
    public void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
    }

    @Test
    @Order(2)
    @DisplayName("Hawaii filters are loaded")
    public void testHawaiiFilters() {
        assertThat(hawaiiFilters, is(notNullValue()));
    }

    @Test
    @Order(3)
    @DisplayName("Timescale container is loaded and running")
    public void testDatabaseContainer() {
        assertThat(timescaleContainer, is(notNullValue()));
        assertThat(timescaleContainer.isRunning(), is(true));
    }

    @Test
    @Order(4)
    @DisplayName("Database connection is established with Timescale")
    public void testDatabaseConnection() throws IOException, InterruptedException {
        assertThat(timescaleContainer.getJdbcUrl(), not(emptyString()));
        assertThat(timescaleContainer.execInContainer("psql", "-U", DATABASE_NAME, "-c", "SELECT 1;").getExitCode(), is(0));
    }

    @Test
    @Order(5)
    @DisplayName("Health endpoint returns OK")
    public void healthEndpoint() {
        // When: The health endpoint is invoked
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
