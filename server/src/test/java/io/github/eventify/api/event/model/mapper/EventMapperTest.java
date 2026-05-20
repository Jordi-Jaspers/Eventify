package io.github.eventify.api.event.model.mapper;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.model.response.EventSearchResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@DisplayName("Unit Test - EventMapper")
public class EventMapperTest extends UnitTest {

    private EventMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new EventMapperImpl();
    }

    @Test
    @DisplayName("Should map title when event has title")
    public void shouldMapTitleWhenEventHasTitle() {
        // Given: An event with a title
        final Event event = anEventWithChannel("Deployment failed", "prod-alerts", null);

        // When: Mapping to search response
        final EventSearchResponse response = mapper.toResourceObject(event);

        // Then: Title is mapped correctly
        assertThat(response, is(notNullValue()));
        assertThat(response.getTitle(), is(equalTo("Deployment failed")));
    }

    @Test
    @DisplayName("Should map channelName when event has channel with name")
    public void shouldMapChannelNameWhenEventHasChannelWithName() {
        // Given: An event with a channel named "prod-alerts"
        final Event event = anEventWithChannel("Service down", "prod-alerts", null);

        // When: Mapping to search response
        final EventSearchResponse response = mapper.toResourceObject(event);

        // Then: channelName is mapped from channel.name
        assertThat(response, is(notNullValue()));
        assertThat(response.getChannelName(), is(equalTo("prod-alerts")));
    }

    @Test
    @DisplayName("Should map existing fields when event has message, timestamp and severity")
    public void shouldMapExistingFieldsWhenEventHasMessageTimestampAndSeverity() {
        // Given: An event with all standard fields
        final OffsetDateTime timestamp = OffsetDateTime.now().minusHours(1);
        final Event event = anEventWithChannel("Alert", "alerts", "Something went wrong");
        event.setTimestamp(timestamp);
        event.setSeverity(Severity.WARNING);

        // When: Mapping to search response
        final EventSearchResponse response = mapper.toResourceObject(event);

        // Then: All existing fields are mapped
        assertThat(response.getMessage(), is(equalTo("Something went wrong")));
        assertThat(response.getTimestamp(), is(equalTo(timestamp)));
        assertThat(response.getSeverity(), is(equalTo(Severity.WARNING)));
    }

    @Test
    @DisplayName("Should return null channelName when event channel is null")
    public void shouldReturnNullChannelNameWhenEventChannelIsNull() {
        // Given: An event with no channel (lazy load not initialized / null)
        final Event event = new Event();
        event.setId(1L);
        event.setTitle("Orphan event");
        event.setSeverity(Severity.OK);
        event.setTimestamp(OffsetDateTime.now());
        event.setChannel(null);

        // When: Mapping to search response
        final EventSearchResponse response = mapper.toResourceObject(event);

        // Then: channelName is null (no NPE)
        assertThat(response, is(notNullValue()));
        assertThat(response.getChannelName(), is(nullValue()));
    }

    @Test
    @DisplayName("Should return null message when event message is null")
    public void shouldReturnNullMessageWhenEventMessageIsNull() {
        // Given: An event with null message (nullable field)
        final Event event = anEventWithChannel("Heartbeat", "monitoring", null);

        // When: Mapping to search response
        final EventSearchResponse response = mapper.toResourceObject(event);

        // Then: message is null, no NPE
        assertThat(response, is(notNullValue()));
        assertThat(response.getMessage(), is(nullValue()));
    }

    // ========================= FACTORY METHODS =========================

    private Event anEventWithChannel(final String title, final String channelName, final String message) {
        final User user = aValidUser();
        final Channel channel = aChannel(1L, channelName, user);

        final Event event = new Event();
        event.setId(1L);
        event.setTitle(title);
        event.setMessage(message);
        event.setChannel(channel);
        event.setSeverity(Severity.OK);
        event.setTimestamp(OffsetDateTime.now());
        return event;
    }
}
