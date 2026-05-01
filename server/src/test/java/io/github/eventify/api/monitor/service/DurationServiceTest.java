package io.github.eventify.api.monitor.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.monitor.model.TimelineDuration;
import io.github.eventify.api.monitor.model.response.DurationDetailsResponse;
import io.github.eventify.support.TestBuilders;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("Unit Test - Duration Service")
public class DurationServiceTest extends UnitTest {

    private DurationService durationService;
    private EventRepository eventRepository;

    @BeforeEach
    public void setUp() {
        eventRepository = mock(EventRepository.class);
        durationService = new DurationService(eventRepository);
    }

    @Test
    @DisplayName("Should return durations with selected index when timestamp points to middle duration")
    public void shouldReturnDurationsWithSelectedIndexWhenTimestampPointsToMiddleDuration() {
        // Given: A channel with three events creating three durations
        final Channel channel = aChannel();
        final OffsetDateTime t1 = OffsetDateTime.of(2026, 2, 12, 9, 50, 0, 0, UTC);
        final OffsetDateTime t2 = OffsetDateTime.of(2026, 2, 12, 10, 20, 0, 0, UTC);
        final OffsetDateTime t3 = OffsetDateTime.of(2026, 2, 12, 10, 45, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, t1),
            TestBuilders.anEvent(channel, Severity.CRITICAL, t2),
            TestBuilders.anEvent(channel, Severity.OK, t3)
        );

        given(eventRepository.findEventsAroundTimestamp(eq(channel.getId()), any(), anyInt()))
            .willReturn(events);

        // When: Requesting durations around the CRITICAL timestamp
        final DurationDetailsResponse response = durationService.getDurationsAround(channel.getId(), t2);

        // Then: Should return 3 durations
        assertThat(response.getDurations(), hasSize(3));

        // And: Selected index should be 1 (CRITICAL duration)
        assertThat(response.getSelectedIndex(), is(1));

        // And: First duration is OK
        final TimelineDuration d1 = response.getDurations().get(0);
        assertThat(d1.getSeverity(), is(Severity.OK));
        assertThat(d1.getStartTime(), is(t1));
        assertThat(d1.getEndTime(), is(t2));

        // And: Second duration is CRITICAL
        final TimelineDuration d2 = response.getDurations().get(1);
        assertThat(d2.getSeverity(), is(Severity.CRITICAL));
        assertThat(d2.getStartTime(), is(t2));
        assertThat(d2.getEndTime(), is(t3));

        // And: Third duration is OK
        final TimelineDuration d3 = response.getDurations().get(2);
        assertThat(d3.getSeverity(), is(Severity.OK));
        assertThat(d3.getStartTime(), is(t3));
        assertThat(d3.getEndTime(), is(nullValue()));
    }

    @Test
    @DisplayName("Should cut off duration that extends before minimum display window")
    public void shouldCutOffDurationThatExtendsBeforeMinimumDisplayWindow() {
        // Given: A duration that started hours ago
        final Channel channel = aChannel();
        final OffsetDateTime veryOldStart = OffsetDateTime.of(2026, 2, 12, 6, 0, 0, 0, UTC);
        final OffsetDateTime selectedTime = OffsetDateTime.of(2026, 2, 12, 10, 20, 0, 0, UTC);
        final OffsetDateTime recentTime = OffsetDateTime.of(2026, 2, 12, 10, 45, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, veryOldStart),
            TestBuilders.anEvent(channel, Severity.CRITICAL, selectedTime),
            TestBuilders.anEvent(channel, Severity.OK, recentTime)
        );

        given(eventRepository.findEventsAroundTimestamp(eq(channel.getId()), any(), anyInt()))
            .willReturn(events);

        // When: Requesting durations around the CRITICAL timestamp
        final DurationDetailsResponse response = durationService.getDurationsAround(channel.getId(), selectedTime);

        // Then: The OK duration should be cut off at window start
        final TimelineDuration d1 = response.getDurations().get(0);
        assertThat(d1.getSeverity(), is(Severity.OK));
        assertThat(d1.getStartTime(), not(equalTo(veryOldStart)));
        assertThat(d1.getEndTime(), is(selectedTime));
    }

    @Test
    @DisplayName("Should return enough durations to fill display window when flapping occurs")
    public void shouldReturnEnoughDurationsToFillDisplayWindowWhenFlappingOccurs() {
        // Given: Multiple rapid severity changes (flapping)
        final Channel channel = aChannel();
        final OffsetDateTime base = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, base),
            TestBuilders.anEvent(channel, Severity.CRITICAL, base.plusMinutes(2)),
            TestBuilders.anEvent(channel, Severity.OK, base.plusMinutes(4)),
            TestBuilders.anEvent(channel, Severity.WARNING, base.plusMinutes(6)),
            TestBuilders.anEvent(channel, Severity.CRITICAL, base.plusMinutes(8)),
            TestBuilders.anEvent(channel, Severity.OK, base.plusMinutes(10)),
            TestBuilders.anEvent(channel, Severity.CRITICAL, base.plusMinutes(12))
        );

        given(eventRepository.findEventsAroundTimestamp(eq(channel.getId()), any(), anyInt()))
            .willReturn(events);

        // When: Requesting durations around middle of flapping period
        final DurationDetailsResponse response = durationService.getDurationsAround(channel.getId(), base.plusMinutes(6));

        // Then: Should return multiple durations to cover the window
        assertThat(response.getDurations(), hasSize(greaterThan(3)));

        // And: Selected index should point to WARNING duration
        final TimelineDuration selected = response.getDurations().get(response.getSelectedIndex());
        assertThat(selected.getSeverity(), is(Severity.WARNING));
    }

    @Test
    @DisplayName("Should handle very long previous duration without performance issues")
    public void shouldHandleVeryLongPreviousDurationWithoutPerformanceIssues() {
        // Given: A 41-day OK period before selected CRITICAL
        final Channel channel = aChannel();
        final OffsetDateTime longAgo = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, UTC);
        final OffsetDateTime now = OffsetDateTime.of(2026, 2, 12, 10, 20, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, longAgo),
            TestBuilders.anEvent(channel, Severity.CRITICAL, now)
        );

        given(eventRepository.findEventsAroundTimestamp(eq(channel.getId()), any(), anyInt()))
            .willReturn(events);

        // When: Requesting durations around the CRITICAL timestamp
        final DurationDetailsResponse response = durationService.getDurationsAround(channel.getId(), now);

        // Then: Should return 2 durations
        assertThat(response.getDurations(), hasSize(2));

        // And: First duration should be cut off at window boundary
        final TimelineDuration d1 = response.getDurations().get(0);
        assertThat(d1.getSeverity(), is(Severity.OK));
        assertThat(d1.getStartTime(), not(equalTo(longAgo)));
    }

    @Test
    @DisplayName("Should mark first duration as having no previous when channel just created")
    public void shouldMarkFirstDurationAsHavingNoPreviousWhenChannelJustCreated() {
        // Given: Channel's first event (NO_DATA before this)
        final Channel channel = aChannel();
        final OffsetDateTime firstEvent = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, firstEvent)
        );

        given(eventRepository.findEventsAroundTimestamp(eq(channel.getId()), any(), anyInt()))
            .willReturn(events);

        // When: Requesting durations around the first event
        final DurationDetailsResponse response = durationService.getDurationsAround(channel.getId(), firstEvent);

        // Then: Should have NO_DATA as first duration
        assertThat(response.getDurations().get(0).getSeverity(), is(Severity.NO_DATA));

        // And: hasPrevious should be false
        assertThat(response.isHasPrevious(), is(false));
    }

    @Test
    @DisplayName("Should mark live duration with null end time")
    public void shouldMarkLiveDurationWithNullEndTime() {
        // Given: Events with last one being the most recent
        final Channel channel = aChannel();
        final OffsetDateTime t1 = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);
        final OffsetDateTime t2 = OffsetDateTime.of(2026, 2, 12, 10, 30, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, t1),
            TestBuilders.anEvent(channel, Severity.CRITICAL, t2)
        );

        given(eventRepository.findEventsAroundTimestamp(eq(channel.getId()), any(), anyInt()))
            .willReturn(events);

        // When: Requesting durations around the latest event
        final DurationDetailsResponse response = durationService.getDurationsAround(channel.getId(), t2);

        // Then: Last duration should have null endTime (ongoing)
        final TimelineDuration lastDuration = response.getDurations().get(response.getDurations().size() - 1);
        assertThat(lastDuration.getEndTime(), is(nullValue()));
    }

    @Test
    @DisplayName("Should fetch durations before given timestamp when direction is before")
    public void shouldFetchDurationsBeforeGivenTimestampWhenDirectionIsBefore() {
        // Given: Multiple durations across time
        final Channel channel = aChannel();
        final OffsetDateTime base = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, base.minusHours(2)),
            TestBuilders.anEvent(channel, Severity.WARNING, base.minusHours(1)),
            TestBuilders.anEvent(channel, Severity.CRITICAL, base),
            TestBuilders.anEvent(channel, Severity.OK, base.plusHours(1))
        );

        given(eventRepository.findEventsBefore(eq(channel.getId()), any(), anyInt()))
            .willReturn(events.subList(0, 3));

        // When: Requesting durations before timestamp
        final DurationDetailsResponse response = durationService.getDurationsBefore(channel.getId(), base);

        // Then: Should only return durations ending before timestamp
        assertThat(response.getDurations(), hasSize(2));
        assertThat(response.getDurations().get(0).getSeverity(), is(Severity.OK));
        assertThat(response.getDurations().get(1).getSeverity(), is(Severity.WARNING));
    }

    @Test
    @DisplayName("Should fetch durations after given timestamp when direction is after")
    public void shouldFetchDurationsAfterGivenTimestampWhenDirectionIsAfter() {
        // Given: Multiple durations across time
        final Channel channel = aChannel();
        final OffsetDateTime base = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, base.minusHours(1)),
            TestBuilders.anEvent(channel, Severity.CRITICAL, base),
            TestBuilders.anEvent(channel, Severity.WARNING, base.plusHours(1)),
            TestBuilders.anEvent(channel, Severity.OK, base.plusHours(2))
        );

        given(eventRepository.findEventsAfter(eq(channel.getId()), any(), anyInt()))
            .willReturn(events.subList(1, 4));

        // When: Requesting durations after timestamp
        final DurationDetailsResponse response = durationService.getDurationsAfter(channel.getId(), base);

        // Then: Should only return durations starting after timestamp
        assertThat(response.getDurations(), hasSize(2));
        assertThat(response.getDurations().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(response.getDurations().get(1).getSeverity(), is(Severity.OK));
    }

    @Test
    @DisplayName("Should respect maximum durations limit when many durations exist")
    public void shouldRespectMaximumDurationsLimitWhenManyDurationsExist() {
        // Given: More than max durations (>10)
        final Channel channel = aChannel();
        final OffsetDateTime base = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, base),
            TestBuilders.anEvent(channel, Severity.CRITICAL, base.plusMinutes(1)),
            TestBuilders.anEvent(channel, Severity.OK, base.plusMinutes(2)),
            TestBuilders.anEvent(channel, Severity.WARNING, base.plusMinutes(3)),
            TestBuilders.anEvent(channel, Severity.CRITICAL, base.plusMinutes(4)),
            TestBuilders.anEvent(channel, Severity.OK, base.plusMinutes(5)),
            TestBuilders.anEvent(channel, Severity.WARNING, base.plusMinutes(6)),
            TestBuilders.anEvent(channel, Severity.CRITICAL, base.plusMinutes(7)),
            TestBuilders.anEvent(channel, Severity.OK, base.plusMinutes(8)),
            TestBuilders.anEvent(channel, Severity.WARNING, base.plusMinutes(9)),
            TestBuilders.anEvent(channel, Severity.CRITICAL, base.plusMinutes(10)),
            TestBuilders.anEvent(channel, Severity.OK, base.plusMinutes(11)),
            TestBuilders.anEvent(channel, Severity.WARNING, base.plusMinutes(12))
        );

        given(eventRepository.findEventsAroundTimestamp(eq(channel.getId()), any(), anyInt()))
            .willReturn(events);

        // When: Requesting durations around timestamp
        final DurationDetailsResponse response = durationService.getDurationsAround(channel.getId(), base.plusMinutes(6));

        // Then: Should return at most 10 durations
        assertThat(response.getDurations(), hasSize(lessThanOrEqualTo(10)));
    }

    @Test
    @DisplayName("Should mark hasNext as false when at end of timeline")
    public void shouldMarkHasNextAsFalseWhenAtEndOfTimeline() {
        // Given: Events ending at current time
        final Channel channel = aChannel();
        final OffsetDateTime t1 = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);
        final OffsetDateTime t2 = OffsetDateTime.of(2026, 2, 12, 10, 30, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.OK, t1),
            TestBuilders.anEvent(channel, Severity.CRITICAL, t2)
        );

        given(eventRepository.findEventsAroundTimestamp(eq(channel.getId()), any(), anyInt()))
            .willReturn(events);

        // When: Requesting durations at the end
        final DurationDetailsResponse response = durationService.getDurationsAround(channel.getId(), t2);

        // Then: hasNext should be false
        assertThat(response.isHasNext(), is(false));
    }

    @Test
    @DisplayName("Should mark hasPrevious as true when earlier durations exist")
    public void shouldMarkHasPreviousAsTrueWhenEarlierDurationsExist() {
        // Given: Events with history before current window
        final Channel channel = aChannel();
        final OffsetDateTime t1 = OffsetDateTime.of(2026, 2, 12, 8, 0, 0, 0, UTC);
        final OffsetDateTime t2 = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, UTC);
        final OffsetDateTime t3 = OffsetDateTime.of(2026, 2, 12, 10, 30, 0, 0, UTC);

        final List<Event> events = List.of(
            TestBuilders.anEvent(channel, Severity.WARNING, t1),
            TestBuilders.anEvent(channel, Severity.OK, t2),
            TestBuilders.anEvent(channel, Severity.CRITICAL, t3)
        );

        given(eventRepository.findEventsAroundTimestamp(eq(channel.getId()), any(), anyInt()))
            .willReturn(events);

        // When: Requesting durations around t3
        final DurationDetailsResponse response = durationService.getDurationsAround(channel.getId(), t3);

        // Then: hasPrevious should be true
        assertThat(response.isHasPrevious(), is(true));
    }

    // ==================== Helper Methods ====================

    private Channel aChannel() {
        final Channel channel = new Channel();
        channel.setId(1L);
        channel.setName("Test Channel");
        channel.setCreatedAt(OffsetDateTime.of(2026, 2, 12, 9, 0, 0, 0, UTC));
        return channel;
    }
}
