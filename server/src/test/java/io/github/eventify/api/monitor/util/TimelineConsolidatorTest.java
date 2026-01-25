package io.github.eventify.api.monitor.util;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineSource;
import io.github.eventify.api.monitor.model.response.TimelineDuration;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for TimelineConsolidator utility class.
 */
@DisplayName("Unit Test - Timeline Consolidator")
class TimelineConsolidatorTest extends UnitTest {

    // ========================= CONSOLIDATE TIMELINE SOURCES =========================

    @Nested
    @DisplayName("consolidate(List<TimelineSource>)")
    class ConsolidateTimelineSources {

        @Test
        @DisplayName("Should consolidate timeline sources using interface")
        void shouldConsolidateTimelineSourcesUsingInterface() {
            // Given: Timeline sources (simulating channels/groups)
            final OffsetDateTime start = OffsetDateTime.now().minusHours(2);
            final OffsetDateTime end = OffsetDateTime.now();

            final TimelineSource source1 = () -> Timeline.builder()
                .durations(List.of(createDuration(Severity.OK, start, end)))
                .build();

            final TimelineSource source2 = () -> Timeline.builder()
                .durations(List.of(createDuration(Severity.WARNING, start, end)))
                .build();

            // When: Consolidating sources
            final Timeline result = TimelineConsolidator.consolidate(List.of(source1, source2));

            // Then: Should pick worst severity (WARNING)
            assertThat(result.getDurations(), hasSize(1));
            assertThat(result.getDurations().getFirst().getSeverity(), is(equalTo(Severity.WARNING)));
        }

        @Test
        @DisplayName("Should return empty timeline when sources list is null")
        void shouldReturnEmptyTimelineWhenSourcesListIsNull() {
            // When: Consolidating null sources
            final Timeline result = TimelineConsolidator.consolidate(null);

            // Then: Should return empty timeline
            assertThat(result, is(notNullValue()));
            assertThat(result.getDurations(), is(empty()));
        }

        @Test
        @DisplayName("Should return empty timeline when sources list is empty")
        void shouldReturnEmptyTimelineWhenSourcesListIsEmpty() {
            // When: Consolidating empty sources
            final Timeline result = TimelineConsolidator.consolidate(List.of());

            // Then: Should return empty timeline
            assertThat(result, is(notNullValue()));
            assertThat(result.getDurations(), is(empty()));
        }
    }

    // ========================= CONSOLIDATE RAW TIMELINES =========================


    @Nested
    @DisplayName("consolidateTimelines(List<Timeline>)")
    class ConsolidateRawTimelines {

        @Test
        @DisplayName("Should return empty timeline when no timelines provided")
        void shouldReturnEmptyTimelineWhenNoTimelinesProvided() {
            // Given: Empty list of timelines
            final List<Timeline> timelines = new ArrayList<>();

            // When: Consolidating
            final Timeline result = TimelineConsolidator.consolidateTimelines(timelines);

            // Then: Should return empty timeline
            assertThat(result, is(notNullValue()));
            assertThat(result.getDurations(), is(empty()));
        }

        @Test
        @DisplayName("Should return empty timeline when null list provided")
        void shouldReturnEmptyTimelineWhenNullListProvided() {
            // When: Consolidating null
            final Timeline result = TimelineConsolidator.consolidateTimelines(null);

            // Then: Should return empty timeline
            assertThat(result, is(notNullValue()));
            assertThat(result.getDurations(), is(empty()));
        }

        @Test
        @DisplayName("Should return same timeline when single timeline provided")
        void shouldReturnSameTimelineWhenSingleTimelineProvided() {
            // Given: Single timeline
            final OffsetDateTime start = OffsetDateTime.now().minusHours(1);
            final OffsetDateTime end = OffsetDateTime.now();
            final Timeline timeline = Timeline.builder()
                .durations(List.of(createDuration(Severity.OK, start, end)))
                .build();

            // When: Consolidating
            final Timeline result = TimelineConsolidator.consolidateTimelines(List.of(timeline));

            // Then: Should return same timeline
            assertThat(result.getDurations(), hasSize(1));
            assertThat(result.getDurations().getFirst().getSeverity(), is(equalTo(Severity.OK)));
        }

        @Test
        @DisplayName("Should pick worst severity when timelines overlap")
        void shouldPickWorstSeverityWhenTimelinesOverlap() {
            // Given: Three timelines with overlapping intervals, different severities
            final OffsetDateTime start = OffsetDateTime.now().minusHours(2);
            final OffsetDateTime end = OffsetDateTime.now();

            final Timeline okTimeline = Timeline.builder()
                .durations(List.of(createDuration(Severity.OK, start, end)))
                .build();

            final Timeline warningTimeline = Timeline.builder()
                .durations(List.of(createDuration(Severity.WARNING, start, end)))
                .build();

            final Timeline criticalTimeline = Timeline.builder()
                .durations(List.of(createDuration(Severity.CRITICAL, start, end)))
                .build();

            // When: Consolidating
            final Timeline result = TimelineConsolidator.consolidateTimelines(
                List.of(okTimeline, warningTimeline, criticalTimeline)
            );

            // Then: Should show CRITICAL (worst severity)
            assertThat(result.getDurations(), hasSize(1));
            assertThat(result.getDurations().getFirst().getSeverity(), is(equalTo(Severity.CRITICAL)));
            assertThat(result.getDurations().getFirst().getStartTime(), is(equalTo(start)));
            assertThat(result.getDurations().getFirst().getEndTime(), is(equalTo(end)));
        }

        @Test
        @DisplayName("Should merge consecutive durations with same severity")
        void shouldMergeConsecutiveDurationsWithSameSeverity() {
            // Given: Two timelines that produce consecutive OK durations
            final OffsetDateTime t0 = OffsetDateTime.now().minusHours(4);
            final OffsetDateTime t1 = OffsetDateTime.now().minusHours(3);
            final OffsetDateTime t2 = OffsetDateTime.now().minusHours(2);
            final OffsetDateTime t3 = OffsetDateTime.now().minusHours(1);

            final Timeline timeline1 = Timeline.builder()
                .durations(List.of(createDuration(Severity.OK, t0, t2)))
                .build();

            final Timeline timeline2 = Timeline.builder()
                .durations(List.of(createDuration(Severity.OK, t1, t3)))
                .build();

            // When: Consolidating
            final Timeline result = TimelineConsolidator.consolidateTimelines(List.of(timeline1, timeline2));

            // Then: Should merge into single OK duration
            assertThat(result.getDurations(), hasSize(1));
            assertThat(result.getDurations().getFirst().getSeverity(), is(equalTo(Severity.OK)));
            assertThat(result.getDurations().getFirst().getStartTime(), is(equalTo(t0)));
            assertThat(result.getDurations().getFirst().getEndTime(), is(equalTo(t3)));
        }

        @Test
        @DisplayName("Should handle non-overlapping timelines")
        void shouldHandleNonOverlappingTimelines() {
            // Given: Two timelines that don't overlap
            final OffsetDateTime t0 = OffsetDateTime.now().minusHours(4);
            final OffsetDateTime t1 = OffsetDateTime.now().minusHours(3);
            final OffsetDateTime t2 = OffsetDateTime.now().minusHours(2);
            final OffsetDateTime t3 = OffsetDateTime.now().minusHours(1);

            final Timeline timeline1 = Timeline.builder()
                .durations(List.of(createDuration(Severity.OK, t0, t1)))
                .build();

            final Timeline timeline2 = Timeline.builder()
                .durations(List.of(createDuration(Severity.WARNING, t2, t3)))
                .build();

            // When: Consolidating
            final Timeline result = TimelineConsolidator.consolidateTimelines(List.of(timeline1, timeline2));

            // Then: Should have both durations
            assertThat(result.getDurations(), hasSize(2));
            assertThat(result.getDurations().get(0).getSeverity(), is(equalTo(Severity.OK)));
            assertThat(result.getDurations().get(1).getSeverity(), is(equalTo(Severity.WARNING)));
        }

        @Test
        @DisplayName("Should handle partial overlaps correctly")
        void shouldHandlePartialOverlapsCorrectly() {
            // Given: Two timelines with partial overlap
            //        Timeline 1: [OK------|--------]
            //        Timeline 2:     [----CRITICAL----]
            //        Expected:   [OK][--CRITICAL--]
            final OffsetDateTime t0 = OffsetDateTime.now().minusHours(4);
            final OffsetDateTime t1 = OffsetDateTime.now().minusHours(3);
            final OffsetDateTime t2 = OffsetDateTime.now().minusHours(2);
            final OffsetDateTime t3 = OffsetDateTime.now().minusHours(1);

            final Timeline timeline1 = Timeline.builder()
                .durations(List.of(createDuration(Severity.OK, t0, t2)))
                .build();

            final Timeline timeline2 = Timeline.builder()
                .durations(List.of(createDuration(Severity.CRITICAL, t1, t3)))
                .build();

            // When: Consolidating
            final Timeline result = TimelineConsolidator.consolidateTimelines(List.of(timeline1, timeline2));

            // Then: Should show OK, then CRITICAL (overlap shows worst)
            assertThat(result.getDurations(), hasSize(2));

            // First segment: OK from t0 to t1 (before CRITICAL starts)
            assertThat(result.getDurations().get(0).getSeverity(), is(equalTo(Severity.OK)));
            assertThat(result.getDurations().get(0).getStartTime(), is(equalTo(t0)));
            assertThat(result.getDurations().get(0).getEndTime(), is(equalTo(t1)));

            // Second segment: CRITICAL from t1 to t3 (worst during overlap and beyond)
            assertThat(result.getDurations().get(1).getSeverity(), is(equalTo(Severity.CRITICAL)));
            assertThat(result.getDurations().get(1).getStartTime(), is(equalTo(t1)));
            assertThat(result.getDurations().get(1).getEndTime(), is(equalTo(t3)));
        }

        @Test
        @DisplayName("Should filter out empty timelines")
        void shouldFilterOutEmptyTimelines() {
            // Given: Mix of empty and non-empty timelines
            final OffsetDateTime start = OffsetDateTime.now().minusHours(1);
            final OffsetDateTime end = OffsetDateTime.now();

            final Timeline emptyTimeline = Timeline.empty();
            final Timeline nullDurationsTimeline = Timeline.builder().durations(null).build();
            final Timeline validTimeline = Timeline.builder()
                .durations(List.of(createDuration(Severity.OK, start, end)))
                .build();

            // When: Consolidating
            final Timeline result = TimelineConsolidator.consolidateTimelines(
                List.of(emptyTimeline, nullDurationsTimeline, validTimeline)
            );

            // Then: Should only include valid timeline
            assertThat(result.getDurations(), hasSize(1));
            assertThat(result.getDurations().getFirst().getSeverity(), is(equalTo(Severity.OK)));
        }

        @Test
        @DisplayName("Should handle complex multi-channel scenario")
        void shouldHandleComplexMultiChannelScenario() {
            // Given: Three channels with varying severities over time
            //        Channel 1: [NO_DATA][OK--------]
            //        Channel 2: [NO_DATA][--WARNING-]
            //        Channel 3: [NO_DATA][----CRITICAL]
            //        Expected:  [NO_DATA][CRITICAL throughout overlap]
            final OffsetDateTime rangeStart = OffsetDateTime.now().minusHours(4);
            final OffsetDateTime eventTime = OffsetDateTime.now().minusHours(2);
            final OffsetDateTime rangeEnd = OffsetDateTime.now();

            final Timeline channel1 = Timeline.builder()
                .durations(
                    List.of(
                        createDuration(Severity.NO_DATA, rangeStart, eventTime),
                        createDuration(Severity.OK, eventTime, rangeEnd)
                    )
                )
                .build();

            final Timeline channel2 = Timeline.builder()
                .durations(
                    List.of(
                        createDuration(Severity.NO_DATA, rangeStart, eventTime),
                        createDuration(Severity.WARNING, eventTime, rangeEnd)
                    )
                )
                .build();

            final Timeline channel3 = Timeline.builder()
                .durations(
                    List.of(
                        createDuration(Severity.NO_DATA, rangeStart, eventTime),
                        createDuration(Severity.CRITICAL, eventTime, rangeEnd)
                    )
                )
                .build();

            // When: Consolidating
            final Timeline result = TimelineConsolidator.consolidateTimelines(List.of(channel1, channel2, channel3));

            // Then: Should show NO_DATA then CRITICAL
            assertThat(result.getDurations(), hasSize(2));

            assertThat(result.getDurations().get(0).getSeverity(), is(equalTo(Severity.NO_DATA)));
            assertThat(result.getDurations().get(0).getStartTime(), is(equalTo(rangeStart)));
            assertThat(result.getDurations().get(0).getEndTime(), is(equalTo(eventTime)));

            assertThat(result.getDurations().get(1).getSeverity(), is(equalTo(Severity.CRITICAL)));
            assertThat(result.getDurations().get(1).getStartTime(), is(equalTo(eventTime)));
            assertThat(result.getDurations().get(1).getEndTime(), is(equalTo(rangeEnd)));
        }
    }

    // ========================= HELPER METHODS =========================

    private TimelineDuration createDuration(
        final Severity severity,
        final OffsetDateTime start,
        final OffsetDateTime end
    ) {
        return new TimelineDuration()
            .setSeverity(severity)
            .setStartTime(start)
            .setEndTime(end);
    }
}
