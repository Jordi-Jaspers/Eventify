package io.github.eventify.api.channel.controller;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import io.github.jframe.exception.resource.ErrorResponseResource;
import io.github.jframe.exception.resource.ValidationErrorResponseResource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.EXTERNAL_CHANNELS_PATH;
import static io.github.eventify.common.security.filter.ApiKeyAuthenticationFilter.API_KEY_HEADER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Integration Test - API Channel Controller")
public class ApiChannelControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should create personal channel with personal API key")
    public void createPersonalChannelSuccess() throws Exception {
        // Given: User with personal API key
        final User user = aValidatedUser();
        final ApiKey apiKey = anApiKeyForUser(user, "Personal Key");

        // And: Valid channel creation request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("My Production Alerts")
            .setSlug("myapp.prod.alerts")
            .setDescription("Production error notifications");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain channel ID and slug
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getId(), is(notNullValue()));
        assertThat(channelResponse.getSlug(), is("myapp.prod.alerts"));
        assertThat(channelResponse.getName(), is("My Production Alerts"));
    }

    @Test
    @DisplayName("Should create org channel with org API key")
    public void createOrgChannelSuccess() throws Exception {
        // Given: Organization with org API key
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final ApiKey orgApiKey = anApiKeyForOrganisation(owner, org, "Org Key");

        // And: Valid channel creation request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Org Production Alerts")
            .setSlug("org.prod.backend.errors")
            .setDescription("Organization-wide error tracking");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, orgApiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getId(), is(notNullValue()));
        assertThat(channelResponse.getSlug(), is("org.prod.backend.errors"));
    }

    @Test
    @DisplayName("Should reject duplicate slug")
    public void createChannelFailsWhenSlugDuplicate() throws Exception {
        // Given: Existing channel with slug
        final User user = aValidatedUser();
        final Channel existingChannel = aChannelForUser(user, "Existing Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with duplicate slug
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("New Channel")
            .setSlug(existingChannel.getSlug())
            .setDescription("This should fail");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention duplicate slug
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);

        assertThat(error.getApiErrorReason(), containsStringIgnoringCase("slug"));
    }

    @Test
    @DisplayName("Should reject invalid slug format with uppercase")
    public void createChannelFailsWhenSlugHasUppercase() throws Exception {
        // Given: User with API key
        final User user = aValidatedUser();
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with invalid slug (uppercase)
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Test Channel")
            .setSlug("MyApp.Production.Alerts")
            .setDescription("Invalid slug format");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Validation error should mention slug
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getField().equals("slug")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject invalid slug format with special characters")
    public void createChannelFailsWhenSlugHasSpecialChars() throws Exception {
        // Given: User with API key
        final User user = aValidatedUser();
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with invalid slug (special characters)
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Test Channel")
            .setSlug("my-app_prod@alerts")
            .setDescription("Invalid slug format");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Validation error should mention slug
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getField().equals("slug")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject request with invalid API key")
    public void createChannelFailsWhenInvalidApiKey() throws Exception {
        // Given: Invalid API key
        final String invalidKey = "evt_invalidkey12345678901234567890";

        // And: Valid channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Test Channel")
            .setSlug("test.channel")
            .setDescription("Should fail");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, invalidKey)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should reject request without API key")
    public void createChannelFailsWhenNoApiKey() throws Exception {
        // Given: Valid channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Test Channel")
            .setSlug("test.channel")
            .setDescription("Should fail");

        // When: Creating channel without API key
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should create channel without description")
    public void createChannelSuccessWithoutDescription() throws Exception {
        // Given: User with personal API key
        final User user = aValidatedUser();
        final ApiKey apiKey = anApiKeyForUser(user, "Personal Key");

        // And: Request without description
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Minimal Channel")
            .setSlug("minimal.channel");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain channel details
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getId(), is(notNullValue()));
        assertThat(channelResponse.getSlug(), is("minimal.channel"));
    }

    @Test
    @DisplayName("Should reject missing required fields")
    public void createChannelFailsWhenMissingRequiredFields() throws Exception {
        // Given: User with API key
        final User user = aValidatedUser();
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request missing name and slug
        final CreateChannelRequest request = new CreateChannelRequest()
            .setDescription("Missing name and slug");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Validation errors should contain name and slug
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(error.getErrors().size(), is(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("Should reject expired API key")
    public void createChannelFailsWhenApiKeyExpired() throws Exception {
        // Given: User with expired API key
        final User user = aValidatedUser();
        final ApiKey expiredKey = anExpiredApiKeyForUser(user, "Expired Key");

        // And: Valid channel request
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Test Channel")
            .setSlug("test.channel")
            .setDescription("Should fail");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, expiredKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));

        // And: Error should mention expired key
        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);

        assertThat(error.getErrorMessage(), containsStringIgnoringCase("has expired"));
    }

    @Test
    @DisplayName("Should accept slug with numbers and dots")
    public void createChannelSuccessWithValidSlugFormat() throws Exception {
        // Given: User with API key
        final User user = aValidatedUser();
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with valid slug format (lowercase, numbers, dots)
        final CreateChannelRequest request = new CreateChannelRequest()
            .setName("Complex Slug Channel")
            .setSlug("app1.prod.backend.service2.errors")
            .setDescription("Valid complex slug");

        // When: Creating channel
        final MockHttpServletRequestBuilder createRequest = post(EXTERNAL_CHANNELS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(createRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Slug should be preserved
        final String content = response.andReturn().getResponse().getContentAsString();
        final ChannelDetailsResponse channelResponse = fromJson(content, ChannelDetailsResponse.class);

        assertThat(channelResponse.getSlug(), is("app1.prod.backend.service2.errors"));
    }
}
