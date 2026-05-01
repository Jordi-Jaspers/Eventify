package io.github.eventify.api.event.controller;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.model.request.BatchEventRequest;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.api.event.model.response.EventCreatedResponse;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ValidationErrorResponseResource;
import tools.jackson.core.type.TypeReference;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.EXTERNAL_EVENTS_BATCH_PATH;
import static io.github.eventify.common.security.filter.ApiKeyAuthenticationFilter.API_KEY_HEADER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Batch Event Ingestion Controller")
public class BatchEventIngestionControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should successfully ingest batch of valid events")
    public void ingestBatchWithValidEventsSuccess() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Batch Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Batch Key");

        // And: Batch request with 3 valid events
        final CreateEventRequest event1 = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("First Event")
            .setMessage("First message")
            .setTimestamp(OffsetDateTime.now().minusHours(2));

        final CreateEventRequest event2 = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.WARNING)
            .setTitle("Second Event")
            .setMessage("Second message")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final CreateEventRequest event3 = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Third Event")
            .setMessage("Third message")
            .setTimestamp(OffsetDateTime.now().minusMinutes(30));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(event1, event2, event3));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Response should contain all events
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<EventCreatedResponse> responses = fromJson(content, new TypeReference<>() {});

        assertThat(responses, hasSize(3));
        assertThat(responses.get(0).getId(), is(notNullValue()));
        assertThat(responses.get(1).getId(), is(notNullValue()));
        assertThat(responses.get(2).getId(), is(notNullValue()));

        // And: All events should be stored with client-provided timestamps
        final long storedEventCount = eventRepository.countByChannelId(channel.getId());
        assertThat(storedEventCount, is(3L));
    }

    @Test
    @DisplayName("Should reject entire batch when one event has future timestamp")
    public void ingestBatchFailsWhenFutureTimestamp() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Batch with one future timestamp
        final CreateEventRequest validEvent = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("Valid Event")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final CreateEventRequest futureEvent = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.WARNING)
            .setTitle("Future Event")
            .setTimestamp(OffsetDateTime.now().plusHours(1));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(validEvent, futureEvent));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should indicate timestamp issue with index
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(error.getErrors().size(), is(greaterThan(0)));

        // And: No events should be stored
        final long storedEventCount = eventRepository.countByChannelId(channel.getId());
        assertThat(storedEventCount, is(0L));
    }

    @Test
    @DisplayName("Should reject batch when any channel is inaccessible")
    public void ingestBatchFailsWhenChannelInaccessible() throws Exception {
        // Given: Two users with separate channels
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();
        final Channel user1Channel = aChannelForUser(user1, "User 1 Channel");
        final Channel user2Channel = aChannelForUser(user2, "User 2 Channel");
        final ApiKey user1ApiKey = anApiKeyForUser(user1, "User 1 Key");

        // And: User 1 tries batch with both channels
        final CreateEventRequest accessibleEvent = new CreateEventRequest()
            .setSlug(user1Channel.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("Accessible Event")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final CreateEventRequest inaccessibleEvent = new CreateEventRequest()
            .setSlug(user2Channel.getSlug())
            .setSeverity(Severity.WARNING)
            .setTitle("Inaccessible Event")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(accessibleEvent, inaccessibleEvent));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, user1ApiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be NOT_FOUND (not 403, to prevent enumeration)
        response.andExpect(status().is(SC_NOT_FOUND));

        // And: No events should be stored
        final long storedEventCount = eventRepository.countByChannelIdIn(
            List.of(user1Channel.getId(), user2Channel.getId())
        );
        assertThat(storedEventCount, is(0L));
    }

    @Test
    @DisplayName("Should reject batch exceeding max size of 100 events")
    public void ingestBatchFailsWhenExceedsMaxSize() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Batch with 101 events
        final List<CreateEventRequest> events = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            events.add(
                new CreateEventRequest()
                    .setSlug(channel.getSlug())
                    .setSeverity(Severity.OK)
                    .setTitle("Event " + i)
                    .setTimestamp(OffsetDateTime.now().minusHours(i))
            );
        }

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(events);

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention batch size limit
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getCode().contains("100")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should require timestamp for all events in batch")
    public void ingestBatchFailsWhenMissingTimestamp() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Batch with one event missing timestamp
        final CreateEventRequest eventWithTimestamp = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("Valid Event")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final CreateEventRequest eventWithoutTimestamp = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.WARNING)
            .setTitle("Missing Timestamp");

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(eventWithTimestamp, eventWithoutTimestamp));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention timestamp required
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getField().contains("timestamp")),
            is(true)
        );

        // And: No events should be stored
        final long storedEventCount = eventRepository.countByChannelId(channel.getId());
        assertThat(storedEventCount, is(0L));
    }

    @Test
    @DisplayName("Should reject empty batch")
    public void ingestBatchFailsWhenEmpty() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Empty batch request
        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of());

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention at least one event required
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getCode().toLowerCase().contains("at least one")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should successfully ingest events across multiple channels")
    public void ingestBatchWithMultipleChannelsSuccess() throws Exception {
        // Given: User with two channels
        final User user = aValidatedUser();
        final Channel channel1 = aChannelForUser(user, "Channel 1");
        final Channel channel2 = aChannelForUser(user, "Channel 2");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Batch with events targeting both channels
        final CreateEventRequest event1 = new CreateEventRequest()
            .setSlug(channel1.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("Channel 1 Event")
            .setTimestamp(OffsetDateTime.now().minusHours(2));

        final CreateEventRequest event2 = new CreateEventRequest()
            .setSlug(channel2.getSlug())
            .setSeverity(Severity.WARNING)
            .setTitle("Channel 2 Event")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final CreateEventRequest event3 = new CreateEventRequest()
            .setSlug(channel1.getSlug())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Channel 1 Event 2")
            .setTimestamp(OffsetDateTime.now().minusMinutes(30));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(event1, event2, event3));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: All events should be accepted
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<EventCreatedResponse> responses = fromJson(content, new TypeReference<>() {});

        assertThat(responses, hasSize(3));
    }

    @Test
    @DisplayName("Should verify all-or-nothing semantics when second event fails")
    public void ingestBatchVerifiesAllOrNothingSemantics() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Batch where second event has invalid data
        final CreateEventRequest validEvent1 = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("First Valid Event")
            .setTimestamp(OffsetDateTime.now().minusHours(2));

        final CreateEventRequest invalidEvent = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.WARNING)
            .setTitle("Invalid Event")
            .setTimestamp(OffsetDateTime.now().plusHours(1));

        final CreateEventRequest validEvent2 = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Third Valid Event")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(validEvent1, invalidEvent, validEvent2));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: NO events should be stored (transaction rollback)
        final long storedEventCount = eventRepository.countByChannelId(channel.getId());
        assertThat(storedEventCount, is(0L));
    }

    @Test
    @DisplayName("Should allow org API key to access org channels in batch")
    public void ingestBatchWithOrgApiKeySuccess() throws Exception {
        // Given: Organization with channels
        final User owner = aValidatedUser();
        final Organization org = anOrganisationWithOwner(owner);
        final Channel orgChannel1 = aChannelForOrganisation(owner, org, "Org Channel 1");
        final Channel orgChannel2 = aChannelForOrganisation(owner, org, "Org Channel 2");
        final ApiKey orgApiKey = anApiKeyForOrganisation(owner, org, "Org Key");

        // And: Batch with events targeting org channels
        final CreateEventRequest event1 = new CreateEventRequest()
            .setSlug(orgChannel1.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("Org Event 1")
            .setTimestamp(OffsetDateTime.now().minusHours(2));

        final CreateEventRequest event2 = new CreateEventRequest()
            .setSlug(orgChannel2.getSlug())
            .setSeverity(Severity.WARNING)
            .setTitle("Org Event 2")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(event1, event2));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, orgApiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: All events should be accepted
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<EventCreatedResponse> responses = fromJson(content, new TypeReference<>() {});

        assertThat(responses, hasSize(2));
    }

    @Test
    @DisplayName("Should accept batch with exactly 100 events")
    public void ingestBatchWithExactly100EventsSuccess() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Batch with exactly 100 events
        final List<CreateEventRequest> events = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            events.add(
                new CreateEventRequest()
                    .setSlug(channel.getSlug())
                    .setSeverity(Severity.OK)
                    .setTitle("Event " + i)
                    .setTimestamp(OffsetDateTime.now().minusHours(i))
            );
        }

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(events);

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: All 100 events should be accepted
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<EventCreatedResponse> responses = fromJson(content, new TypeReference<>() {});

        assertThat(responses, hasSize(100));
    }

    @Test
    @DisplayName("Should accept timestamp at current time")
    public void ingestBatchWithCurrentTimestampSuccess() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Event with current timestamp
        final CreateEventRequest currentEvent = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("Current Event")
            .setTimestamp(OffsetDateTime.now());

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(currentEvent));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));
    }

    @Test
    @DisplayName("Should reject batch with missing severity")
    public void ingestBatchFailsWhenMissingSeverity() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Batch with event missing severity
        final CreateEventRequest invalidEvent = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setTitle("Missing Severity")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(invalidEvent));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention severity
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getField().contains("severity")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject batch with missing title")
    public void ingestBatchFailsWhenMissingTitle() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Batch with event missing title
        final CreateEventRequest invalidEvent = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.OK)
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(invalidEvent));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention title
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getField().contains("title")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject batch with missing slug")
    public void ingestBatchFailsWhenMissingSlug() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Batch with event missing slug
        final CreateEventRequest invalidEvent = new CreateEventRequest()
            .setSeverity(Severity.OK)
            .setTitle("Missing Channel")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(invalidEvent));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Error should mention slug
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(
            error.getErrors().stream()
                .anyMatch(e -> e.getField().contains("slug")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject batch without API key")
    public void ingestBatchFailsWhenNoApiKey() throws Exception {
        // Given: Valid batch request but no API key
        final CreateEventRequest event = new CreateEventRequest()
            .setSlug("test-slug")
            .setSeverity(Severity.OK)
            .setTitle("Test Event")
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(event));

        // When: Posting batch without API key header
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should successfully ingest batch with metadata")
    public void ingestBatchWithMetadataSuccess() throws Exception {
        // Given: An active channel and valid API key
        final User user = aValidatedUser();
        final Channel channel = aChannelForUser(user, "Test Channel");
        final ApiKey apiKey = anApiKeyForUser(user, "Test Key");

        // And: Events with metadata
        final Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("host", "server-01");
        metadata1.put("region", "us-east-1");

        final Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("host", "server-02");
        metadata2.put("region", "us-west-2");

        final CreateEventRequest event1 = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("Event with metadata 1")
            .setMetadata(metadata1)
            .setTimestamp(OffsetDateTime.now().minusHours(2));

        final CreateEventRequest event2 = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.WARNING)
            .setTitle("Event with metadata 2")
            .setMetadata(metadata2)
            .setTimestamp(OffsetDateTime.now().minusHours(1));

        final BatchEventRequest request = new BatchEventRequest()
            .setEvents(List.of(event1, event2));

        // When: Posting batch events
        final MockHttpServletRequestBuilder batchRequest = post(EXTERNAL_EVENTS_BATCH_PATH)
            .contentType(APPLICATION_JSON)
            .header(API_KEY_HEADER, apiKey.getKey())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(batchRequest);

        // Then: Response should be CREATED
        response.andExpect(status().is(SC_CREATED));

        // And: Both events should be accepted
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<EventCreatedResponse> responses = fromJson(content, new TypeReference<>() {});

        assertThat(responses, hasSize(2));
    }
}
