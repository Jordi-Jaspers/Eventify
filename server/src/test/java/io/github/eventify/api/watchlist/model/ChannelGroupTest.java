package io.github.eventify.api.watchlist.model;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineSource;
import io.github.eventify.api.monitor.model.response.TimelineDuration;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for ChannelGroup timeline functionality.
 */
@DisplayName("Unit Test - Channel Group")
class ChannelGroupTest extends UnitTest {

    @Test
    @DisplayName("Should return consolidated timeline from member channels")
    void shouldReturnConsolidatedTimelineFromMemberChannels() {
        // Given: Two channels with different severities at same time
        final OffsetDateTime start = OffsetDateTime.now().minusHours(1);
        final OffsetDateTime end = OffsetDateTime.now();

        final Channel okChannel = createChannelWithTimeline(1L, "ok-channel", Severity.OK, start, end);
        final Channel warningChannel = createChannelWithTimeline(2L, "warning-channel", Severity.WARNING, start, end);

        final ChannelGroup group = ChannelGroup.builder()
            .id(UUID.randomUUID())
            .name("API Services")
            .channels(new ArrayList<>(List.of(okChannel, warningChannel)))
            .build();

        // When: Getting group timeline
        final Timeline timeline = group.getTimeline();

        // Then: Should return consolidated timeline with worst severity (WARNING)
        assertThat(timeline, is(notNullValue()));
        assertThat(timeline.getDurations(), hasSize(1));
        assertThat(timeline.getDurations().getFirst().getSeverity(), is(Severity.WARNING));
    }

    @Test
    @DisplayName("Should return empty timeline when no channels")
    void shouldReturnEmptyTimelineWhenNoChannels() {
        // Given: Group with no channels
        final ChannelGroup group = ChannelGroup.builder()
            .id(UUID.randomUUID())
            .name("Empty Group")
            .channels(new ArrayList<>())
            .build();

        // When: Getting group timeline
        final Timeline timeline = group.getTimeline();

        // Then: Should return empty timeline
        assertThat(timeline, is(notNullValue()));
        assertThat(timeline.getDurations(), is(empty()));
    }

    @Test
    @DisplayName("Should return empty timeline when channels field is null")
    void shouldReturnEmptyTimelineWhenChannelsFieldIsNull() {
        // Given: Group with channels field not populated (null)
        final ChannelGroup group = ChannelGroup.builder()
            .id(UUID.randomUUID())
            .name("Unenriched Group")
            .build();
        group.setChannels(null);

        // When: Getting group timeline
        final Timeline timeline = group.getTimeline();

        // Then: Should return empty timeline
        assertThat(timeline, is(notNullValue()));
        assertThat(timeline.getDurations(), is(empty()));
    }

    @Test
    @DisplayName("Should implement TimelineSource interface")
    void shouldImplementTimelineSourceInterface() {
        // Given: A channel group
        final ChannelGroup group = ChannelGroup.builder()
            .id(UUID.randomUUID())
            .name("Test Group")
            .channels(new ArrayList<>())
            .build();

        // Then: Should be a TimelineSource
        assertThat(group, is(instanceOf(TimelineSource.class)));
    }

    @Test
    @DisplayName("Should create group with static factory method")
    void shouldCreateGroupWithStaticFactoryMethod() {
        // Given: Channels with IDs
        final List<Channel> channels = channelsWithIds(1L, 2L, 3L);

        // When: Creating group with factory method
        final ChannelGroup group = ChannelGroup.of("API Services", channels);

        // Then: Should have generated ID and correct fields
        assertThat(group.getId(), is(notNullValue()));
        assertThat(group.getName(), is(equalTo("API Services")));
        assertThat(group.getChannelIds(), contains(1L, 2L, 3L));
    }

    @Test
    @DisplayName("Should serialize to channelIds and deserialize back")
    void shouldSerializeToChannelIdsAndDeserializeBack() {
        // Given: A group with channels
        final ChannelGroup group = ChannelGroup.builder()
            .id(UUID.randomUUID())
            .name("Test Group")
            .channels(channelsWithIds(1L, 2L))
            .build();

        // When: Getting channelIds (for serialization)
        final List<Long> ids = group.getChannelIds();

        // Then: Should return the IDs
        assertThat(ids, contains(1L, 2L));

        // When: Setting channelIds (simulating deserialization)
        final ChannelGroup newGroup = ChannelGroup.builder()
            .id(UUID.randomUUID())
            .name("New Group")
            .build();
        newGroup.setChannelIds(List.of(3L, 4L));

        // Then: Should have channels with those IDs
        assertThat(newGroup.getChannels(), hasSize(2));
        assertThat(newGroup.getChannelIds(), contains(3L, 4L));
    }

    private Channel createChannelWithTimeline(
        final Long id,
        final String name,
        final Severity severity,
        final OffsetDateTime start,
        final OffsetDateTime end
    ) {
        final Channel channel = new Channel();
        channel.setId(id);
        channel.setName(name);
        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setTimeline(
            Timeline.builder()
                .durations(
                    List.of(
                        new TimelineDuration()
                            .setSeverity(severity)
                            .setStartTime(start)
                            .setEndTime(end)
                    )
                )
                .build()
        );
        return channel;
    }

    private List<Channel> channelsWithIds(final Long... ids) {
        final List<Channel> channels = new ArrayList<>();
        for (final Long id : ids) {
            final Channel channel = new Channel();
            channel.setId(id);
            channels.add(channel);
        }
        return channels;
    }
}
