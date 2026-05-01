package io.github.eventify.general;

import io.github.eventify.support.IntegrationTest;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.PUBLIC_HEALTH_PATH;
import static io.github.eventify.api.Paths.PUBLIC_SWAGGER_PATH;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class to verify the context is loaded correctly.
 */
@DisplayName("Integration Test - Application Setup")
public class ApplicationContextTest extends IntegrationTest {

    @Test
    @DisplayName("Should respond to health endpoint with status UP")
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

    @Test
    @DisplayName("Should be able to retrieve Swagger JSON documentation")
    public void swaggerJson() throws Exception {
        // Given: The request for the health endpoint
        final MockHttpServletRequestBuilder healthRequest = get(PUBLIC_SWAGGER_PATH);

        // When: The health endpoint is invoked
        final ResultActions response = mockMvc.perform(healthRequest);

        // Then: The response is 200 - OK
        response.andExpect(status().is(HttpServletResponse.SC_OK));

        // And: The response body contains the swagger JSON
        final String responseBody = response.andReturn().getResponse().getContentAsString();
        assertThat(responseBody, containsString("Eventify"));
    }
}
