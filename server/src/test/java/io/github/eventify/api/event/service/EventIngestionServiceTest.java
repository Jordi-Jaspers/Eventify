package io.github.eventify.api.event.service;

import io.github.eventify.api.apikey.model.ApiKeyScope;
import io.github.eventify.api.channel.cache.ChannelCache;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.quota.service.UserQuotaService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Event Ingestion Service")
public class EventIngestionServiceTest extends UnitTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ChannelCache channelCache;

    @Mock
    private UserQuotaService userQuotaService;

    @InjectMocks
    private EventIngestionService eventIngestionService;

    private User user;
    private Channel channel;
    private ApiKeyPrincipal principal;

    @BeforeEach
    public void setUp() {
        user = aValidUser();
        user.setId(1L);

        channel = aChannel(1L, "test-channel", "Test Channel", user, null);
        channel.setStatus(ChannelStatus.ACTIVE);

        principal = aPersonalPrincipal(user);
    }

    @Test
    @DisplayName("Should ingest event with all fields successfully")
    public void shouldIngestEventWithAllFieldsSuccessfully() {
        // Given: Valid request with all fields
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("server", "prod-01");
        metadata.put("region", "us-east-1");

        final CreateEventRequest request = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Server Down")
            .setMessage("Production server experienced critical failure")
            .setMetadata(metadata);

        when(channelCache.getBySlug(channel.getSlug())).thenReturn(Optional.empty());
        when(channelRepository.findBySlugAndPrincipal(channel.getSlug(), principal))
            .thenReturn(Optional.of(channel));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            final Event event = invocation.getArgument(0);
            event.setId(100L);
            return event;
        });

        // When: Ingesting event
        final Event result = eventIngestionService.ingestEvent(request, principal);

        // Then: Returned event should have ID and timestamp
        assertThat(result.getId(), is(equalTo(100L)));
        assertThat(result.getTimestamp(), is(notNullValue()));

        // And: Event should be saved with correct fields
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        final Event capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getSeverity(), is(Severity.CRITICAL));
        assertThat(capturedEvent.getTitle(), is("Server Down"));
        assertThat(capturedEvent.getMessage(), is("Production server experienced critical failure"));
        assertThat(capturedEvent.getMetadata(), is(notNullValue()));
        assertThat(capturedEvent.getMetadata().get("server"), is("prod-01"));
        assertThat(capturedEvent.getTimestamp(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should ingest event with minimal fields successfully")
    public void shouldIngestEventWithMinimalFieldsSuccessfully() {
        // Given: Valid request with only required fields
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("System healthy");

        when(channelCache.getBySlug(channel.getSlug())).thenReturn(Optional.empty());
        when(channelRepository.findBySlugAndPrincipal(channel.getSlug(), principal))
            .thenReturn(Optional.of(channel));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            final Event event = invocation.getArgument(0);
            event.setId(101L);
            return event;
        });

        // When: Ingesting event
        final Event result = eventIngestionService.ingestEvent(request, principal);

        // Then: Returned event should have ID and timestamp
        assertThat(result.getId(), is(equalTo(101L)));
        assertThat(result.getTimestamp(), is(notNullValue()));

        // And: Event should be saved with correct fields
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        final Event capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getSeverity(), is(Severity.OK));
        assertThat(capturedEvent.getTitle(), is("System healthy"));
        assertThat(capturedEvent.getMessage(), is(nullValue()));
        assertThat(capturedEvent.getMetadata(), is(nullValue()));
    }

    @Test
    @DisplayName("Should assign server timestamp")
    public void shouldAssignServerTimestamp() {
        // Given: Valid request
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.WARNING)
            .setTitle("High Memory Usage");

        when(channelCache.getBySlug(channel.getSlug())).thenReturn(Optional.empty());
        when(channelRepository.findBySlugAndPrincipal(channel.getSlug(), principal))
            .thenReturn(Optional.of(channel));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            final Event event = invocation.getArgument(0);
            event.setId(102L);
            return event;
        });

        // When: Ingesting event
        final Event result = eventIngestionService.ingestEvent(request, principal);

        // Then: Server should assign its own timestamp (set in Event constructor)
        assertThat(result.getTimestamp(), is(notNullValue()));

        // And: Saved event has server-assigned timestamp
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        final Event capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should throw not found when channel does not exist")
    public void shouldThrowNotFoundWhenChannelDoesNotExist() {
        // Given: Request with non-existent channel slug
        final String nonExistentSlug = "non-existent-channel";
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug(nonExistentSlug)
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event");

        when(channelCache.getBySlug(nonExistentSlug)).thenReturn(Optional.empty());
        when(channelRepository.findBySlugAndPrincipal(nonExistentSlug, principal))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> eventIngestionService.ingestEvent(request, principal)
        );

        // And: Event should not be saved
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Should preserve metadata structure")
    public void shouldPreserveMetadataStructure() {
        // Given: Request with complex metadata
        final Map<String, Object> metadata = new HashMap<>();
        metadata.put("string", "value");
        metadata.put("number", 42);
        metadata.put("boolean", true);

        final CreateEventRequest request = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.CRITICAL)
            .setTitle("Test Event")
            .setMetadata(metadata);

        when(channelCache.getBySlug(channel.getSlug())).thenReturn(Optional.empty());
        when(channelRepository.findBySlugAndPrincipal(channel.getSlug(), principal))
            .thenReturn(Optional.of(channel));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            final Event event = invocation.getArgument(0);
            event.setId(108L);
            return event;
        });

        // When: Ingesting event
        eventIngestionService.ingestEvent(request, principal);

        // Then: Metadata structure should be preserved
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        final Event capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getMetadata(), is(notNullValue()));
        assertThat(capturedEvent.getMetadata().get("string"), is("value"));
        assertThat(capturedEvent.getMetadata().get("number"), is(42));
        assertThat(capturedEvent.getMetadata().get("boolean"), is(true));
    }

    @Test
    @DisplayName("Should use cached channel when available")
    public void shouldUseCachedChannelWhenAvailable() {
        // Given: Channel is already cached (populated by @PreAuthorize security layer)
        final CreateEventRequest request = new CreateEventRequest()
            .setSlug(channel.getSlug())
            .setSeverity(Severity.OK)
            .setTitle("Test Event");

        when(channelCache.getBySlug(channel.getSlug())).thenReturn(Optional.of(channel));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            final Event event = invocation.getArgument(0);
            event.setId(109L);
            return event;
        });

        // When: Ingesting event
        eventIngestionService.ingestEvent(request, principal);

        // Then: Should NOT query database (cache hit)
        verify(channelRepository, never()).findBySlugAndPrincipal(any(), any());
        verify(eventRepository).save(any(Event.class));
    }

    // NOTE: Quota enforcement tests omitted from unit tests
    // These are tested at integration level in EventIngestionControllerTest
    // where the full flow including UserQuotaService is validated

    // ===== Factory Methods =====

    private Channel aChannel(final Long id, final String slug, final String name, final User owner,
        final Organization org) {
        final Channel ch = new Channel();
        ch.setId(id);
        ch.setSlug(slug);
        ch.setName(name);
        ch.setUser(owner);
        ch.setOrganization(org);
        ch.setStatus(ChannelStatus.ACTIVE);
        return ch;
    }

    private ApiKeyPrincipal aPersonalPrincipal(final User user) {
        return new ApiKeyPrincipal(
            1L,
            "evt_",
            ApiKeyScope.USER,
            user.getId(),
            user,
            null,
            null
        );
    }
}
