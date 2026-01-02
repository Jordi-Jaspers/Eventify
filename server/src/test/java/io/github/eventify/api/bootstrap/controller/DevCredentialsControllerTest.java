package io.github.eventify.api.bootstrap.controller;

import io.github.eventify.api.bootstrap.model.response.DevCredentialsResponse;
import io.github.eventify.support.IntegrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.DEV_CREDENTIALS_PATH;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@DisplayName("Integration Test - Dev Credentials Controller")
class DevCredentialsControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return dev credentials with email and password")
    public void shouldReturnDevCredentialsWithEmailAndPassword() throws Exception {
        // Given: No authentication required

        // When: Requesting dev credentials
        final MockHttpServletRequestBuilder request = get(DEV_CREDENTIALS_PATH)
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain email and password fields
        final String content = response.andReturn().getResponse().getContentAsString();
        final DevCredentialsResponse credentials = fromJson(content, DevCredentialsResponse.class);

        assertThat(credentials.getEmail(), is(notNullValue()));
        assertThat(credentials.getPassword(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return credentials from configuration")
    public void shouldReturnCredentialsFromConfiguration() throws Exception {
        // Given: No authentication required

        // When: Requesting dev credentials
        final MockHttpServletRequestBuilder request = get(DEV_CREDENTIALS_PATH)
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain valid email format
        final String content = response.andReturn().getResponse().getContentAsString();
        final DevCredentialsResponse credentials = fromJson(content, DevCredentialsResponse.class);

        assertThat(credentials.getEmail(), containsString("@"));
        assertThat(credentials.getPassword(), not(emptyOrNullString()));
    }

    @Test
    @DisplayName("Should return credentials without authentication")
    public void shouldReturnCredentialsWithoutAuthentication() throws Exception {
        // Given: No authentication header provided

        // When: Requesting dev credentials without auth
        final MockHttpServletRequestBuilder request = get(DEV_CREDENTIALS_PATH)
            .accept(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK (no auth required)
        response.andExpect(status().is(SC_OK));

        // And: Response should contain credentials
        final String content = response.andReturn().getResponse().getContentAsString();
        final DevCredentialsResponse credentials = fromJson(content, DevCredentialsResponse.class);

        assertThat(credentials, is(notNullValue()));
        assertThat(credentials.getEmail(), is(notNullValue()));
        assertThat(credentials.getPassword(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return consistent credentials on multiple requests")
    public void shouldReturnConsistentCredentialsOnMultipleRequests() throws Exception {
        // Given: No authentication required

        // When: Requesting dev credentials first time
        final MockHttpServletRequestBuilder firstRequest = get(DEV_CREDENTIALS_PATH)
            .accept(APPLICATION_JSON);

        final ResultActions firstResponse = mockMvc.perform(firstRequest);

        // Then: Response should be OK
        firstResponse.andExpect(status().is(SC_OK));

        final String firstContent = firstResponse.andReturn().getResponse().getContentAsString();
        final DevCredentialsResponse firstCredentials = fromJson(firstContent, DevCredentialsResponse.class);

        // When: Requesting dev credentials second time
        final MockHttpServletRequestBuilder secondRequest = get(DEV_CREDENTIALS_PATH)
            .accept(APPLICATION_JSON);

        final ResultActions secondResponse = mockMvc.perform(secondRequest);

        // Then: Response should be OK
        secondResponse.andExpect(status().is(SC_OK));

        final String secondContent = secondResponse.andReturn().getResponse().getContentAsString();
        final DevCredentialsResponse secondCredentials = fromJson(secondContent, DevCredentialsResponse.class);

        // And: Credentials should be identical
        assertThat(secondCredentials.getEmail(), is(equalTo(firstCredentials.getEmail())));
        assertThat(secondCredentials.getPassword(), is(equalTo(firstCredentials.getPassword())));
    }
}
