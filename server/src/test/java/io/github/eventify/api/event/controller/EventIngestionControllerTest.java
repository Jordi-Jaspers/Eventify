package io.github.eventify.api.event.controller;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.model.request.BatchEventRequest;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.api.event.model.response.EventCreatedResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.util.TimeProvider;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import io.github.jframe.exception.resource.ErrorResponseResource;
import io.github.jframe.exception.resource.RateLimitErrorResponseResource;
import io.github.jframe.exception.resource.ValidationErrorResponseResource;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.EXTERNAL_EVENTS_BATCH_PATH;
import static io.github.eventify.api.Paths.EXTERNAL_EVENTS_PATH;
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
@DisplayName("Integration Test - Event Ingestion Controller")
public class EventIngestionControllerTest extends IntegrationTest {


    @Test
    @DisplayName("Should ingest event with all fields successfully")
    public void ingestEventWithAllFieldsSuccess() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Production Alerts");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Valid request with all fields
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("server", "prod-01");
        metadata.put("region", "us-east-1");

        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Production Server Down")
            .setMessage("Server experienced critical failure at 10:30 AM")
            .setMetadata(metadata);

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain event ID and timestamp
        final String content = response.andReturn().getResponse().getContentAsString();
        final EventCreatedResponse eventResponse = fromJson(content, EventCreatedResponse.class);

        assertThat(eventResponse.getId(), is(notNullValue()));
        assertThat(eventResponse.getTimestamp(), is(notNullValue()));
        assertThat(eventResponse.getTimestamp().isBefore(OffsetDateTime.now().plusSeconds(1)), is(true));
    }

    @Test
    @DisplayName("Should ingest event with minimal fields successfully")
    public void ingestEventWithMinimalFieldsSuccess() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Health Checks");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Valid request with only required fields
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.OK)
            .setTitle("System healthy");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain event ID and timestamp
        final String content = response.andReturn().getResponse().getContentAsString();
        final EventCreatedResponse eventResponse = fromJson(content, EventCreatedResponse.class);

        assertThat(eventResponse.getId(), is(notNullValue()));
        assertThat(eventResponse.getTimestamp(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should reject missing required fields")
    public void ingestEventFailsWhenMissingRequiredFields() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request missing severity and title
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId());

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Validation errors should contain severity and title
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(error.getErrors().size(), is(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("Should reject invalid severity value")
    public void ingestEventFailsWhenInvalidSeverity() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with invalid severity
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(null)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention invalid severity
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getField().equals("severity")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject non-existent channel with 403 to avoid leaking existence")
    public void ingestEventFailsWhenChannelNotFound() throws Exception {
        // Given: Valid API key but non-existent channel
        final User user = aValidatedUser();
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with non-existent channel
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(99999L)
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be FORBIDDEN (not 404, to avoid leaking channel existence)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject channel not owned by API key")
    public void ingestEventFailsWhenChannelAccessDenied() throws Exception {
        // Given: Two users with separate channels
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();
        final Channel user1Channel = aChannelForUser(user1, "User 1 Channel");
        final ApiKey user2ApiKey = anApiKeyForUser(user2, "User 2 Key");

        // And: User 2 tries to access User 1's channel
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(user1Channel.getId())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, user2ApiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be FORBIDDEN (Spring Security handles access denied)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject paused channel")
    public void ingestEventFailsWhenChannelPaused() throws Exception {
        // Given: A paused channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Paused Channel");
        pauseChannel(channel);
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be BAD_REQUEST with error message
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention channel is paused
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);

        assertThat(error.getApiErrorReason(), containsStringIgnoringCase("paused"));
    }

    @Test
    @DisplayName("Should reject request without API key")
    public void ingestEventFailsWhenNoApiKey() throws Exception {
        // Given: Valid request but no API key
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(1L)
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        // When: Ingesting event without API key header
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should reject expired API key")
    public void ingestEventFailsWhenApiKeyExpired() throws Exception {
        // Given: An expired API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anExpiredApiKeyForUser(user, "Expired Key");

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));

        // And: Error should mention expired key (filter returns ErrorResponseResource)
        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);

        assertThat(error.getErrorMessage(), containsStringIgnoringCase("has expired"));
    }

    @Test
    @DisplayName("Should ignore client timestamp and use server timestamp")
    public void ingestEventIgnoresClientTimestamp() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request (client cannot provide timestamp in request)
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.WARNING)
            .setTitle("Test Event");

        final OffsetDateTime beforeIngest = OffsetDateTime.now();

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        final OffsetDateTime afterIngest = OffsetDateTime.now();

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Timestamp should be assigned by server
        final String content = response.andReturn().getResponse().getContentAsString();
        final EventCreatedResponse eventResponse = fromJson(content, EventCreatedResponse.class);

        assertThat(eventResponse.getTimestamp(), is(notNullValue()));
        assertThat(eventResponse.getTimestamp().isAfter(beforeIngest.minusSeconds(1)), is(true));
        assertThat(eventResponse.getTimestamp().isBefore(afterIngest.plusSeconds(1)), is(true));
    }

    @Test
    @DisplayName("Should allow org API key to access org channel")
    public void ingestEventSuccessWithOrgApiKey() throws Exception {
        // Given: Organization with channel and org API key
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Channel orgChannel = aChannelForOrganisation(owner, org, "Org Channel");
        final ApiKey orgApiKey = anApiKeyForOrganisation(owner, org, "Org Key");

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(orgChannel.getId())
            .setSeverity(Severity.WARNING)
            .setTitle("Org Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, orgApiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain event ID
        final String content = response.andReturn().getResponse().getContentAsString();
        final EventCreatedResponse eventResponse = fromJson(content, EventCreatedResponse.class);

        assertThat(eventResponse.getId(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should reject personal API key accessing org channel")
    public void ingestEventFailsWhenPersonalKeyAccessesOrgChannel() throws Exception {
        // Given: Organization with channel, but user has personal API key
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Channel orgChannel = aChannelForOrganisation(owner, org, "Org Channel");
        final ApiKey personalKey = anApiKeyForUser(owner, "Personal Key");

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(orgChannel.getId())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, personalKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be FORBIDDEN (Spring Security handles access denied)
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should reject title exceeding 255 characters")
    public void ingestEventFailsWhenTitleTooLong() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with title exceeding 255 characters
        final String longTitle = "a".repeat(256);
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.CRITICAL)
            .setTitle(longTitle);

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Validation error should mention title
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getField().equals("title")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept title with exactly 255 characters")
    public void ingestEventSuccessWhenTitleExactly255Chars() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with title exactly 255 characters
        final String maxTitle = "a".repeat(255);
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.OK)
            .setTitle(maxTitle);

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));
    }

    @Test
    @DisplayName("Should reject message exceeding 10KB")
    public void ingestEventFailsWhenMessageTooLarge() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with message exceeding 10KB
        final String largeMessage = "a".repeat(10241);
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event")
            .setMessage(largeMessage);

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should accept empty metadata object")
    public void ingestEventSuccessWithEmptyMetadata() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Request with empty metadata
        final Map<String, Object> emptyMetadata = new HashMap<>();
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.OK)
            .setTitle("Test Event")
            .setMetadata(emptyMetadata);

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));
    }

    // ===== Quota Enforcement Tests =====

    @Test
    @DisplayName("Should accept event when user is under quota")
    public void ingestEventSuccessWhenUnderQuota() throws Exception {
        // Given: User has sent 500 events (under 1000 limit)
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");
        seedUserEventQuota(user, 500);

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.OK)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Event count should be incremented to 501
        final int updatedCount = getUserQuotaEventCount(user);
        assertThat(updatedCount, is(501));
    }

    @Test
    @DisplayName("Should reject event when quota reached")
    public void ingestEventFailsWhenQuotaReached() throws Exception {
        // Given: User has reached quota (1000/1000 events)
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");
        seedUserEventQuota(user, 1000);

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be TOO_MANY_REQUESTS
        response.andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()));

        // And: Response body should contain rate limit info
        final String content = response.andReturn().getResponse().getContentAsString();
        final RateLimitErrorResponseResource error = fromJson(content, RateLimitErrorResponseResource.class);

        assertThat(error.getErrorMessage(), containsStringIgnoringCase("Monthly event quota exceeded"));
        assertThat(error.getLimit(), is(1000));
        assertThat(error.getRemaining(), is(0));
        assertThat(error.getResetDate(), is(notNullValue()));

        // And: Event should NOT be stored
        final int eventCount = getUserQuotaEventCount(user);
        assertThat(eventCount, is(1000));
    }

    @Test
    @DisplayName("Should include rate limit info in 429 response body")
    public void ingestEventIncludesRateLimitInfoWhenQuotaExceeded() throws Exception {
        // Given: User has reached quota
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");
        seedUserEventQuota(user, 1000);

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.WARNING)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should have rate limit fields in body
        response.andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()));

        final String content = response.andReturn().getResponse().getContentAsString();
        final RateLimitErrorResponseResource error = fromJson(content, RateLimitErrorResponseResource.class);

        assertThat(error.getLimit(), is(1000));
        assertThat(error.getRemaining(), is(0));
        assertThat(error.getResetDate(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should create quota record on first event for new user")
    public void ingestEventCreatesQuotaRecordForNewUser() throws Exception {
        // Given: New user with no quota record
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.OK)
            .setTitle("First Event");

        // When: Ingesting first event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Quota record should be created with event_count = 1
        final int eventCount = getUserQuotaEventCount(user);
        assertThat(eventCount, is(1));
    }

    @Test
    @DisplayName("Should accept event when quota is exactly one under limit")
    public void ingestEventSuccessWhenExactlyOneUnderLimit() throws Exception {
        // Given: User has 999 events (one under limit)
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");
        seedUserEventQuota(user, 999);

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.OK)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Event count should be incremented to 1000
        final int updatedCount = getUserQuotaEventCount(user);
        assertThat(updatedCount, is(1000));
    }

    @Test
    @DisplayName("Should reject event when quota is over limit")
    public void ingestEventFailsWhenQuotaOverLimit() throws Exception {
        // Given: User has 1001 events (over limit - edge case)
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");
        seedUserEventQuota(user, 1001);

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(channel.getId())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        // When: Ingesting event
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be TOO_MANY_REQUESTS
        response.andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()));
    }

    @Test
    @DisplayName("Should not count org API key events against user quota")
    public void ingestEventWithOrgKeyDoesNotCountAgainstUserQuota() throws Exception {
        // Given: User A creates org and has 900 events
        final User userA = aValidatedUser();
        final Organization org = anOrganisationWithOwner(userA);
        final Channel orgChannel = aChannelForOrganisation(userA, org, "Org Channel");
        final ApiKey orgApiKey = anApiKeyForOrganisation(userA, org, "Org Key");
        seedUserEventQuota(userA, 900);

        // And: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(orgChannel.getId())
            .setSeverity(Severity.OK)
            .setTitle("Org Event");

        // When: Sending event via org API key
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, orgApiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: User A's quota should remain at 900 (NOT incremented)
        final int updatedCount = getUserQuotaEventCount(userA);
        assertThat(updatedCount, is(900));
    }

    @Test
    @DisplayName("Should accept org API key events when user quota exhausted")
    public void ingestEventWithOrgKeySucceedsWhenUserQuotaExhausted() throws Exception {
        // Given: User A has 1000 events (quota exhausted) and org API key
        final User userA = aValidatedUser();
        final Organization org = anOrganisationWithOwner(userA);
        final Channel orgChannel = aChannelForOrganisation(userA, org, "Org Channel");
        final ApiKey orgApiKey = anApiKeyForOrganisation(userA, org, "Org Key");
        seedUserEventQuota(userA, 1000);

        // And: Valid event request
        final CreateEventRequest request = new CreateEventRequest()
            .setChannelId(orgChannel.getId())
            .setSeverity(Severity.OK)
            .setTitle("Org Event");

        // When: Sending event via org API key
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, orgApiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: Event should be accepted despite exhausted quota
        response.andExpect(status().is(SC_CREATED));

        // And: User quota should remain at 1000 (NOT incremented)
        final int finalCount = getUserQuotaEventCount(userA);
        assertThat(finalCount, is(1000));
    }

    @Test
    @DisplayName("Should not count batch events against user quota when using org API key")
    public void ingestBatchWithOrgKeyBypassesQuota() throws Exception {
        // Given: User A with org API key and quota at 999
        final User userA = aValidatedUser();
        final Organization org = anOrganisationWithOwner(userA);
        final Channel orgChannel = aChannelForOrganisation(userA, org, "Org Channel");
        final ApiKey orgApiKey = anApiKeyForOrganisation(userA, org, "Org Key");
        seedUserEventQuota(userA, 999);

        // And: Batch request with 5 events (using past timestamps with microsecond precision)
        final OffsetDateTime now = TimeProvider.now();
        final BatchEventRequest batchRequest = new BatchEventRequest()
            .setEvents(
                List.of(
                    new CreateEventRequest()
                        .setChannelId(orgChannel.getId())
                        .setSeverity(Severity.OK)
                        .setTitle("Event 1")
                        .setTimestamp(now.minusSeconds(5)),
                    new CreateEventRequest()
                        .setChannelId(orgChannel.getId())
                        .setSeverity(Severity.OK)
                        .setTitle("Event 2")
                        .setTimestamp(now.minusSeconds(4)),
                    new CreateEventRequest()
                        .setChannelId(orgChannel.getId())
                        .setSeverity(Severity.OK)
                        .setTitle("Event 3")
                        .setTimestamp(now.minusSeconds(3)),
                    new CreateEventRequest()
                        .setChannelId(orgChannel.getId())
                        .setSeverity(Severity.OK)
                        .setTitle("Event 4")
                        .setTimestamp(now.minusSeconds(2)),
                    new CreateEventRequest()
                        .setChannelId(orgChannel.getId())
                        .setSeverity(Severity.OK)
                        .setTitle("Event 5")
                        .setTimestamp(now.minusSeconds(1))
                )
            );

        // When: Sending batch via org API key
        final MockHttpServletRequestBuilder ingestRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, orgApiKey.getKey())
            .content(toJson(batchRequest));

        final ResultActions response = mockMvc.perform(ingestRequest);

        // Then: All events should be accepted
        response.andExpect(status().is(SC_CREATED));

        // And: User quota should remain at 999 (unchanged)
        final int updatedCount = getUserQuotaEventCount(userA);
        assertThat(updatedCount, is(999));
    }
}
