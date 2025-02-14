package org.jordijaspers.eventify.general;

import java.io.IOException;

import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.PUBLIC_HEALTH_PATH;
import static org.jordijaspers.eventify.support.container.TimescaleContainer.DATABASE_NAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @DisplayName("RabbitMQ container is loaded and running")
    public void testRabbitContainer() {
        assertThat(rabbitContainer, is(notNullValue()));
        assertThat(rabbitContainer.isRunning(), is(true));
    }

    @Test
    @Order(6)
    @DisplayName("Health endpoint returns OK")
    public void healthEndpoint() throws Exception {
        // Given: The request for the health endpoint
        final MockHttpServletRequestBuilder healthRequest = get(PUBLIC_HEALTH_PATH);

        // When: The health endpoint is invoked
        final ResultActions response = mockMvc.perform(healthRequest);

        // Then: The response is 200 - OK
        response.andExpect(status().is(SC_OK));

        // And: The response body contains the status UP
        final String responseBody = response.andReturn().getResponse().getContentAsString();
        assertThat(responseBody, containsString("UP"));
    }
}
