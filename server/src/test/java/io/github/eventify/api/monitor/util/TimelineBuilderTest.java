package io.github.eventify.api.monitor.util;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.TimeSpan;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineDuration;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for TimelineBuilder utility class.
 */
@DisplayName("Unit Test - Timeline Builder")
class TimelineBuilderTest extends UnitTest {

    // ========================= FROM EVENTS TESTS =========================

    @Test
    @DisplayName("Should return NO_DATA timeline when no events provided")
    void shouldReturnNoDataTimelineWhenNoEventsProvided() {
        // Given: No events with a live time range (end = now)
        final List<Event> events = new ArrayList<>();
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusDays(1);

        // When: Building timeline
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Should have single NO_DATA duration
        assertThat(timeline, is(notNullValue()));
        assertThat(timeline.getDurations(), hasSize(1));

        final TimelineDuration duration = timeline.getDurations().getFirst();
        assertThat(duration.getSeverity(), is(equalTo(Severity.NO_DATA)));
        assertThat(duration.getStartTime(), is(equalTo(rangeStart)));
        assertThat(duration.getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should return NO_DATA timeline when events list is null")
    void shouldReturnNoDataTimelineWhenEventsListIsNull() {
        // Given: Null events with a live time range
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusDays(1);

        // When: Building timeline
        final Timeline timeline = TimelineBuilder.fromEvents(null, new TimeSpan(rangeStart, rangeEnd));

        // Then: Should have single NO_DATA duration
        assertThat(timeline.getDurations(), hasSize(1));
        assertThat(timeline.getDurations().getFirst().getSeverity(), is(equalTo(Severity.NO_DATA)));
    }

    @Test
    @DisplayName("Should create single duration when event at range start")
    void shouldCreateSingleDurationWhenEventAtRangeStart() {
        // Given: One event at range start (live mode - end = now)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(2);
        final List<Event> events = List.of(anEvent(1L, channel, Severity.WARNING, rangeStart));

        // When: Building timeline
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Should create single WARNING duration
        assertThat(timeline.getDurations(), hasSize(1));

        final TimelineDuration duration = timeline.getDurations().getFirst();
        assertThat(duration.getSeverity(), is(equalTo(Severity.WARNING)));
        assertThat(duration.getStartTime(), is(equalTo(rangeStart)));
        assertThat(duration.getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should create NO_DATA prefix when first event is after range start")
    void shouldCreateNoDataPrefixWhenFirstEventIsAfterRangeStart() {
        // Given: Event after range start (live mode - end = now)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(4);
        final OffsetDateTime eventTime = rangeEnd.minusHours(1);
        final List<Event> events = List.of(anEvent(1L, channel, Severity.OK, eventTime));

        // When: Building timeline in live mode
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Should have NO_DATA then OK
        assertThat(timeline.getDurations(), hasSize(2));

        final TimelineDuration noDataDuration = timeline.getDurations().get(0);
        assertThat(noDataDuration.getSeverity(), is(equalTo(Severity.NO_DATA)));
        assertThat(noDataDuration.getStartTime(), is(equalTo(rangeStart)));
        assertThat(noDataDuration.getEndTime(), is(equalTo(eventTime)));

        final TimelineDuration okDuration = timeline.getDurations().get(1);
        assertThat(okDuration.getSeverity(), is(equalTo(Severity.OK)));
        assertThat(okDuration.getStartTime(), is(equalTo(eventTime)));
        assertThat(okDuration.getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should merge consecutive events with same severity")
    void shouldMergeConsecutiveEventsWithSameSeverity() {
        // Given: Multiple events with same severity (live mode - end = now)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(4);
        final OffsetDateTime event1Time = rangeStart.plusHours(1);
        final OffsetDateTime event2Time = rangeStart.plusHours(2);
        final OffsetDateTime event3Time = rangeStart.plusHours(3);

        final List<Event> events = List.of(
            anEvent(1L, channel, Severity.WARNING, event1Time),
            anEvent(2L, channel, Severity.WARNING, event2Time),
            anEvent(3L, channel, Severity.WARNING, event3Time)
        );

        // When: Building timeline
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Should merge into single WARNING duration (after NO_DATA prefix)
        assertThat(timeline.getDurations(), hasSize(2));

        assertThat(timeline.getDurations().get(0).getSeverity(), is(equalTo(Severity.NO_DATA)));

        final TimelineDuration warningDuration = timeline.getDurations().get(1);
        assertThat(warningDuration.getSeverity(), is(equalTo(Severity.WARNING)));
        assertThat(warningDuration.getStartTime(), is(equalTo(event1Time)));
        assertThat(warningDuration.getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should create separate durations for different severities (non-live)")
    void shouldCreateSeparateDurationsForDifferentSeverities() {
        // Given: Events with different severities (non-live mode - end is in the past)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeEnd = OffsetDateTime.now().minusMinutes(5); // Non-live: end is 5 minutes ago
        final OffsetDateTime rangeStart = rangeEnd.minusHours(4);
        final OffsetDateTime okTime = rangeStart.plusHours(1);
        final OffsetDateTime warningTime = rangeStart.plusHours(2);
        final OffsetDateTime criticalTime = rangeStart.plusHours(3);

        final List<Event> events = List.of(
            anEvent(1L, channel, Severity.OK, okTime),
            anEvent(2L, channel, Severity.WARNING, warningTime),
            anEvent(3L, channel, Severity.CRITICAL, criticalTime)
        );

        // When: Building timeline (non-live mode)
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Should create separate durations
        assertThat(timeline.getDurations(), hasSize(4));

        assertThat(timeline.getDurations().get(0).getSeverity(), is(equalTo(Severity.NO_DATA)));
        assertThat(timeline.getDurations().get(1).getSeverity(), is(equalTo(Severity.OK)));
        assertThat(timeline.getDurations().get(2).getSeverity(), is(equalTo(Severity.WARNING)));
        assertThat(timeline.getDurations().get(3).getSeverity(), is(equalTo(Severity.CRITICAL)));
    }

    @Test
    @DisplayName("Should extend last duration to range end in live mode")
    void shouldExtendLastDurationToRangeEndInLiveMode() {
        // Given: Event before range end (live mode - end = now)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(2);
        final OffsetDateTime eventTime = rangeEnd.minusHours(1);
        final List<Event> events = List.of(anEvent(1L, channel, Severity.CRITICAL, eventTime));

        // When: Building timeline in live mode
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Last duration extends to range end
        assertThat(timeline.getDurations(), hasSize(2));
        final TimelineDuration lastDuration = timeline.getDurations().getLast();
        assertThat(lastDuration.getSeverity(), is(equalTo(Severity.CRITICAL)));
        assertThat(lastDuration.getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should not extend last duration in non-live mode")
    void shouldNotExtendLastDurationInNonLiveMode() {
        // Given: Event before range end (non-live mode - end is in the past)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeEnd = OffsetDateTime.now().minusMinutes(5); // Non-live: end is 5 minutes ago
        final OffsetDateTime rangeStart = rangeEnd.minusHours(2);
        final OffsetDateTime eventTime = rangeEnd.minusHours(1);
        final List<Event> events = List.of(anEvent(1L, channel, Severity.OK, eventTime));

        // When: Building timeline in non-live mode
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Last duration ends at event time
        assertThat(timeline.getDurations(), hasSize(2));
        final TimelineDuration lastDuration = timeline.getDurations().getLast();
        assertThat(lastDuration.getEndTime(), is(equalTo(eventTime)));
    }

    @Test
    @DisplayName("Should use prior event severity as initial state instead of NO_DATA")
    void shouldUsePriorEventSeverityAsInitialState() {
        // Given: Event before range start (prior event, live mode)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(2);
        final OffsetDateTime priorEventTime = rangeEnd.minusHours(5);
        final List<Event> events = List.of(anEvent(1L, channel, Severity.OK, priorEventTime));

        // When: Building timeline
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Should show OK (from prior event) for entire range
        assertThat(timeline.getDurations(), hasSize(1));
        assertThat(timeline.getDurations().getFirst().getSeverity(), is(equalTo(Severity.OK)));
        assertThat(timeline.getDurations().getFirst().getStartTime(), is(equalTo(rangeStart)));
        assertThat(timeline.getDurations().getFirst().getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should use prior event for prefix instead of NO_DATA when events exist in range")
    void shouldUsePriorEventForPrefixWhenEventsExistInRange() {
        // Given: Prior event and event in range (live mode)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(4);
        final OffsetDateTime priorEventTime = rangeEnd.minusHours(6);
        final OffsetDateTime eventInRangeTime = rangeEnd.minusHours(1);
        final List<Event> events = List.of(
            anEvent(1L, channel, Severity.WARNING, priorEventTime),
            anEvent(2L, channel, Severity.CRITICAL, eventInRangeTime)
        );

        // When: Building timeline
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Should have WARNING prefix (from prior) then CRITICAL
        assertThat(timeline.getDurations(), hasSize(2));

        final TimelineDuration warningDuration = timeline.getDurations().get(0);
        assertThat(warningDuration.getSeverity(), is(equalTo(Severity.WARNING)));
        assertThat(warningDuration.getStartTime(), is(equalTo(rangeStart)));
        assertThat(warningDuration.getEndTime(), is(equalTo(eventInRangeTime)));

        final TimelineDuration criticalDuration = timeline.getDurations().get(1);
        assertThat(criticalDuration.getSeverity(), is(equalTo(Severity.CRITICAL)));
        assertThat(criticalDuration.getStartTime(), is(equalTo(eventInRangeTime)));
        assertThat(criticalDuration.getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should use latest prior event when multiple exist before range")
    void shouldUseLatestPriorEventWhenMultipleExist() {
        // Given: Multiple events before range start (live mode)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(2);
        final OffsetDateTime oldPriorTime = rangeEnd.minusHours(10);
        final OffsetDateTime recentPriorTime = rangeEnd.minusHours(5);
        final List<Event> events = List.of(
            anEvent(1L, channel, Severity.OK, oldPriorTime),
            anEvent(2L, channel, Severity.CRITICAL, recentPriorTime)
        );

        // When: Building timeline
        final Timeline timeline = TimelineBuilder.fromEvents(events, new TimeSpan(rangeStart, rangeEnd));

        // Then: Should use CRITICAL (most recent prior event)
        assertThat(timeline.getDurations(), hasSize(1));
        assertThat(timeline.getDurations().getFirst().getSeverity(), is(equalTo(Severity.CRITICAL)));
    }

    // ========================= GET CURRENT SEVERITY TESTS =========================

    @Test
    @DisplayName("Should return null severity when no events")
    void shouldReturnNullSeverityWhenNoEvents() {
        // Given: No events
        final OffsetDateTime rangeStart = OffsetDateTime.now().minusHours(1);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();

        // When: Getting current severity
        final Severity severity = TimelineBuilder.getCurrentSeverity(null, rangeStart, rangeEnd);

        // Then: Should return null
        assertThat(severity, is(nullValue()));
    }

    @Test
    @DisplayName("Should return most recent event severity")
    void shouldReturnMostRecentEventSeverity() {
        // Given: Multiple events
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeStart = OffsetDateTime.now().minusHours(3);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();

        final List<Event> events = List.of(
            anEvent(1L, channel, Severity.OK, rangeStart.plusHours(1)),
            anEvent(2L, channel, Severity.CRITICAL, rangeStart.plusHours(2)),
            anEvent(3L, channel, Severity.WARNING, rangeStart.plusMinutes(90))
        );

        // When: Getting current severity
        final Severity severity = TimelineBuilder.getCurrentSeverity(events, rangeStart, rangeEnd);

        // Then: Should return CRITICAL (most recent at +2h)
        assertThat(severity, is(equalTo(Severity.CRITICAL)));
    }

    @Test
    @DisplayName("Should fall back to prior event severity when no events in range")
    void shouldFallBackToPriorEventSeverity() {
        // Given: Only prior events (before range)
        final Channel channel = aChannel(1L, "test", ChannelStatus.ACTIVE);
        final OffsetDateTime rangeStart = OffsetDateTime.now().minusHours(2);
        final OffsetDateTime rangeEnd = OffsetDateTime.now();

        final List<Event> events = List.of(
            anEvent(1L, channel, Severity.OK, rangeStart.minusHours(5)),
            anEvent(2L, channel, Severity.WARNING, rangeStart.minusHours(3))
        );

        // When: Getting current severity
        final Severity severity = TimelineBuilder.getCurrentSeverity(events, rangeStart, rangeEnd);

        // Then: Should return WARNING (most recent prior event)
        assertThat(severity, is(equalTo(Severity.WARNING)));
    }

    // ========================= HELPER METHODS =========================

    private Channel aChannel(final Long id, final String name, final ChannelStatus status) {
        final Channel channel = new Channel();
        channel.setId(id);
        channel.setName(name);
        channel.setStatus(status);
        return channel;
    }

    private Event anEvent(final Long id, final Channel channel, final Severity severity, final OffsetDateTime timestamp) {
        final Event event = new Event();
        event.setId(id);
        event.setChannel(channel);
        event.setSeverity(severity);
        event.setTitle("Test Event");
        event.setTimestamp(timestamp);
        return event;
    }
}
