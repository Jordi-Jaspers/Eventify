package org.jordijaspers.eventify.api.monitoring.service;

import java.util.List;

import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineDurationResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;
import org.jordijaspers.eventify.support.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jordijaspers.eventify.api.event.model.Status.*;

@DisplayName("Timeline Consolidator Unit Test")
public class TimelineConsolidatorTest extends UnitTest {

    @Nested
    @DisplayName("Consolidate Timelines Test")
    public class ConsolidateTimelinesTest {

        @Test
        @DisplayName("should correctly merge overlapping timelines with different statuses")
        public void shouldCorrectlyMergeOverlappingTimelinesWithDifferentStatuses() {
            // Given: Two timelines with overlapping periods
            // Timeline 1: [09:00-09:30 OK] [09:30-10:00 WARNING] [10:00-10:05 OK] [10:05-10:15 CRITICAL] [10:15-null OK]
            final TimelineResponse timeline1 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME, BASE_TIME.plusMinutes(30), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(30), BASE_TIME.plusMinutes(60), WARNING),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(60), BASE_TIME.plusMinutes(65), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(65), BASE_TIME.plusMinutes(75), CRITICAL),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(75), null, OK)
                )
            );

            // Timeline 2: [09:15-09:45 CRITICAL] [09:45-10:10 OK] [10:10-null WARNING]
            final TimelineResponse timeline2 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(15), BASE_TIME.plusMinutes(45), CRITICAL),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(45), BASE_TIME.plusMinutes(70), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(70), null, WARNING)
                )
            );

            // When: Consolidating the timelines
            final TimelineResponse result = TimelineConsolidator.consolidateTimelines(List.of(timeline1, timeline2));

            // Then: The timelines should be merged correctly
            assertThat(result.getDurations())
                .hasSize(6)
                .satisfies(durations -> {
                    // [09:00-09:15 OK]
                    assertDuration(
                        durations.get(0),
                        BASE_TIME,
                        BASE_TIME.plusMinutes(15),
                        OK
                    );

                    // [09:15-09:45 CRITICAL]
                    assertDuration(
                        durations.get(1),
                        BASE_TIME.plusMinutes(15),
                        BASE_TIME.plusMinutes(45),
                        CRITICAL
                    );

                    // [09:45-10:00 WARNING]
                    assertDuration(
                        durations.get(2),
                        BASE_TIME.plusMinutes(45),
                        BASE_TIME.plusMinutes(60),
                        WARNING
                    );

                    // [10:00-10:05 OK]
                    assertDuration(
                        durations.get(3),
                        BASE_TIME.plusMinutes(60),
                        BASE_TIME.plusMinutes(65),
                        OK
                    );

                    // [10:05-10:15 CRITICAL]
                    assertDuration(
                        durations.get(4),
                        BASE_TIME.plusMinutes(65),
                        BASE_TIME.plusMinutes(75),
                        CRITICAL
                    );

                    // [10:15-null WARNING]
                    assertThat(durations.get(5))
                        .satisfies(duration -> {
                            assertThat(duration.getStartTime()).isEqualTo(BASE_TIME.plusMinutes(75));
                            assertThat(duration.getEndTime()).isNull();
                            assertThat(duration.getStatus()).isEqualTo(WARNING);
                        });
                });
        }

        @Test
        @DisplayName("should handle multiple overlapping critical periods")
        public void shouldHandleMultipleOverlappingCriticalPeriods() {
            // Given: Two timelines with overlapping critical periods
            // Timeline 1: [09:00-09:30 OK] [09:30-10:00 CRITICAL] [10:00-null OK]
            final TimelineResponse timeline1 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME, BASE_TIME.plusMinutes(30), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(30), BASE_TIME.plusMinutes(60), CRITICAL),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(60), null, OK)
                )
            );

            // Timeline 2: [09:15-09:45 CRITICAL] [09:45-null CRITICAL]
            final TimelineResponse timeline2 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(15), BASE_TIME.plusMinutes(45), CRITICAL),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(45), null, CRITICAL)
                )
            );

            // When: Consolidating the timelines
            final TimelineResponse result = TimelineConsolidator.consolidateTimelines(List.of(timeline1, timeline2));

            // Then: The critical periods should be merged
            assertThat(result.getDurations())
                .hasSize(2)
                .satisfies(durations -> {
                    // [09:00-09:15 OK]
                    assertDuration(
                        durations.get(0),
                        BASE_TIME,
                        BASE_TIME.plusMinutes(15),
                        OK
                    );

                    // [09:15-null CRITICAL]
                    assertThat(durations.get(1))
                        .satisfies(duration -> {
                            assertThat(duration.getStartTime()).isEqualTo(BASE_TIME.plusMinutes(15));
                            assertThat(duration.getEndTime()).isNull();
                            assertThat(duration.getStatus()).isEqualTo(CRITICAL);
                        });
                });
        }

        @Test
        @DisplayName("should handle non-overlapping timelines")
        public void shouldHandleNonOverlappingTimelines() {
            // Given: Two non-overlapping timelines
            // Timeline 1: [09:00-null OK]
            final TimelineResponse timeline1 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME, OK)
                )
            );

            // Timeline 2: [10:00-null WARNING]
            final TimelineResponse timeline2 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(60), WARNING)
                )
            );

            // When: Consolidating the timelines
            final TimelineResponse result = TimelineConsolidator.consolidateTimelines(List.of(timeline1, timeline2));

            // Then: The timelines should remain separate
            assertThat(result.getDurations())
                .hasSize(2)
                .satisfies(durations -> {
                    // [09:00-09:30 OK]
                    assertDuration(
                        durations.get(0),
                        BASE_TIME,
                        BASE_TIME.plusMinutes(60),
                        OK
                    );

                    // [10:00-null WARNING]
                    assertThat(durations.get(1))
                        .satisfies(duration -> {
                            assertThat(duration.getStartTime()).isEqualTo(BASE_TIME.plusMinutes(60));
                            assertThat(duration.getEndTime()).isNull();
                            assertThat(duration.getStatus()).isEqualTo(WARNING);
                        });
                });
        }

        @Test
        @DisplayName("should handle empty list of timelines by returning an empty timeline with status UNKNOWN")
        public void shouldHandleEmptyTimelines() {
            // Given: No timelines -- empty list
            final List<TimelineResponse> timelines = List.of();

            // When: Consolidating the timelines
            final TimelineResponse result = TimelineConsolidator.consolidateTimelines(timelines);

            // Then: The result should be an empty timeline
            assertThat(result.getDurations())
                .isNotNull()
                .hasSize(1)
                .first()
                .satisfies(duration -> {
                    assertThat(duration.getStartTime()).isNotNull();
                    assertThat(duration.getEndTime()).isNull();
                    assertThat(duration.getStatus()).isEqualTo(UNKNOWN);
                });
        }

        @Test
        @DisplayName("should handle null by returning an empty timeline with status UNKNOWN")
        public void shouldHandleNull() {
            // Given: No timelines - null
            final List<TimelineResponse> timelines = null;

            // When: Consolidating the timelines
            final TimelineResponse result = TimelineConsolidator.consolidateTimelines(timelines);

            // Then: The result should be an empty timeline
            assertThat(result.getDurations())
                .isNotNull()
                .hasSize(1)
                .first()
                .satisfies(duration -> {
                    assertThat(duration.getStartTime()).isNotNull();
                    assertThat(duration.getEndTime()).isNull();
                    assertThat(duration.getStatus()).isEqualTo(UNKNOWN);
                });
        }

        @Test
        @DisplayName("should handle single timeline and return the same timeline")
        void shouldHandleSingleTimeline() {
            // Given: A single timeline
            final TimelineResponse timeline = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME, BASE_TIME.plusMinutes(30), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(30), BASE_TIME.plusMinutes(60), WARNING),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(60), BASE_TIME.plusMinutes(65), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(65), BASE_TIME.plusMinutes(75), CRITICAL),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(75), null, OK)
                )
            );

            // When: Consolidating the timeline
            final TimelineResponse result = TimelineConsolidator.consolidateTimelines(List.of(timeline));

            // Then: The result should be the same as the input
            assertThat(result).isSameAs(timeline);
        }
    }


    @Nested
    @DisplayName("Combine Timelines Test")
    public class CombineTimelineTest {

        @Test
        @DisplayName("should correctly merge overlapping timelines with different statuses")
        public void shouldCorrectlyMergeOverlappingTimelinesWithDifferentStatuses() {
            // Given: Two timelines with overlapping periods
            // Timeline 1: [09:00-09:30 OK] [09:30-10:00 WARNING] [10:00-10:05 OK] [10:05-10:15 CRITICAL] [10:15-null OK]
            final TimelineResponse timeline1 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME, BASE_TIME.plusMinutes(30), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(30), BASE_TIME.plusMinutes(60), WARNING),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(60), BASE_TIME.plusMinutes(65), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(65), BASE_TIME.plusMinutes(75), CRITICAL),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(75), null, OK)
                )
            );

            // Timeline 2: [09:15-09:45 CRITICAL] [09:45-10:10 OK] [10:10-null WARNING]
            final TimelineResponse timeline2 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(15), BASE_TIME.plusMinutes(45), CRITICAL),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(45), BASE_TIME.plusMinutes(70), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(70), null, WARNING)
                )
            );

            // When: Combining the timelines
            final TimelineResponse result = TimelineConsolidator.combineTimelines(List.of(timeline1, timeline2));

            // Then: The timelines should be merged correctly
            assertThat(result.getDurations())
                .hasSize(7)
                .satisfies(durations -> {
                    // [09:00-09:15 OK]
                    assertDuration(
                        durations.getFirst(),
                        BASE_TIME,
                        BASE_TIME.plusMinutes(15),
                        OK
                    );

                    // [09:15-09:30 CRITICAL]
                    assertDuration(
                        durations.get(1),
                        BASE_TIME.plusMinutes(15),
                        BASE_TIME.plusMinutes(30),
                        CRITICAL
                    );

                    // [09:30-09:45 WARNING]
                    assertDuration(
                        durations.get(2),
                        BASE_TIME.plusMinutes(30),
                        BASE_TIME.plusMinutes(45),
                        WARNING
                    );

                    // [09:45-10:05 OK]
                    assertDuration(
                        durations.get(3),
                        BASE_TIME.plusMinutes(45),
                        BASE_TIME.plusMinutes(65),
                        OK
                    );

                    // [10:05-10:10 CRITICAL]
                    assertDuration(
                        durations.get(4),
                        BASE_TIME.plusMinutes(65),
                        BASE_TIME.plusMinutes(70),
                        CRITICAL
                    );

                    // [10:10-10:15 WARNING]
                    assertDuration(
                        durations.get(5),
                        BASE_TIME.plusMinutes(70),
                        BASE_TIME.plusMinutes(75),
                        WARNING
                    );

                    // [10:15-null OK]
                    assertThat(durations.getLast())
                        .satisfies(duration -> {
                            assertThat(duration.getStartTime()).isEqualTo(BASE_TIME.plusMinutes(75));
                            assertThat(duration.getEndTime()).isNull();
                            assertThat(duration.getStatus()).isEqualTo(OK);
                        });
                });
        }

        @Test
        @DisplayName("should handle multiple overlapping critical periods")
        public void shouldHandleMultipleOverlappingCriticalPeriods() {
            // Given: Two timelines with overlapping critical periods
            // Timeline 1: [09:00-09:30 OK] [09:30-10:00 CRITICAL] [10:00-null OK]
            final TimelineResponse timeline1 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME, BASE_TIME.plusMinutes(30), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(30), BASE_TIME.plusMinutes(60), CRITICAL),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(60), null, OK)
                )
            );

            // Timeline 2: [09:15-null CRITICAL]
            final TimelineResponse timeline2 = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(15), null, CRITICAL)
                )
            );

            // When: Combining the timelines
            final TimelineResponse result = TimelineConsolidator.combineTimelines(List.of(timeline1, timeline2));

            // Then: The critical periods should be merged
            assertThat(result.getDurations())
                .hasSize(3)
                .satisfies(durations -> {
                    // [09:00-09:15 OK]
                    assertDuration(
                        durations.getFirst(),
                        BASE_TIME,
                        BASE_TIME.plusMinutes(15),
                        OK
                    );

                    // [09:15-10:00 CRITICAL]
                    assertDuration(
                        durations.get(1),
                        BASE_TIME.plusMinutes(15),
                        BASE_TIME.plusMinutes(60),
                        CRITICAL
                    );

                    // [10:00-null OK]
                    assertThat(durations.getLast())
                        .satisfies(duration -> {
                            assertThat(duration.getStartTime()).isEqualTo(BASE_TIME.plusMinutes(60));
                            assertThat(duration.getEndTime()).isNull();
                            assertThat(duration.getStatus()).isEqualTo(OK);
                        });
                });
        }

        @Test
        @DisplayName("should handle single timeline and return the same timeline")
        public void shouldHandleSingleTimeline() {
            // Given: A single timeline
            final TimelineResponse timeline = new TimelineResponse(
                List.of(
                    new TimelineDurationResponse(BASE_TIME, BASE_TIME.plusMinutes(30), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(30), BASE_TIME.plusMinutes(60), WARNING),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(60), BASE_TIME.plusMinutes(65), OK),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(65), BASE_TIME.plusMinutes(75), CRITICAL),
                    new TimelineDurationResponse(BASE_TIME.plusMinutes(75), null, OK)
                )
            );

            // When: Combining the timeline
            final TimelineResponse result = TimelineConsolidator.combineTimelines(List.of(timeline));

            // Then: The result should be the same as the input
            assertThat(result).isSameAs(timeline);
        }

        @Test
        @DisplayName("should handle empty list of timelines by returning an empty timeline with status UNKNOWN")
        public void shouldHandleEmptyTimelines() {
            // Given: No timelines -- empty list
            final List<TimelineResponse> timelines = List.of();

            // When: Combining the timelines
            final TimelineResponse result = TimelineConsolidator.combineTimelines(timelines);

            // Then: The result should be an empty timeline
            assertThat(result.getDurations())
                .isNotNull()
                .hasSize(1)
                .first()
                .satisfies(duration -> {
                    assertThat(duration.getStartTime()).isNotNull();
                    assertThat(duration.getEndTime()).isNull();
                    assertThat(duration.getStatus()).isEqualTo(UNKNOWN);
                });
        }
    }


    @Nested
    @DisplayName("Calculate Durations Test")
    public class CalculateTimelineTest {

        @Test
        @DisplayName("should create timeline from ordered events")
        public void shouldCreateTimelineFromOrderedEvents() {
            // Given: a list of ordered events with status changes
            final List<EventRequest> events = List.of(
                createEvent(0, OK),
                createEvent(30, WARNING),
                createEvent(60, OK),
                createEvent(90, CRITICAL),
                createEvent(120, OK)
            );

            // When: calculating the timeline
            final TimelineResponse result = TimelineConsolidator.calculateTimeline(events);

            // Then: the timeline should contain the correct durations
            assertThat(result.getDurations())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(
                    List.of(
                        new TimelineDurationResponse(BASE_TIME, BASE_TIME.plusMinutes(30), OK),
                        new TimelineDurationResponse(BASE_TIME.plusMinutes(30), BASE_TIME.plusMinutes(60), WARNING),
                        new TimelineDurationResponse(BASE_TIME.plusMinutes(60), BASE_TIME.plusMinutes(90), OK),
                        new TimelineDurationResponse(BASE_TIME.plusMinutes(90), BASE_TIME.plusMinutes(120), CRITICAL),
                        new TimelineDurationResponse(BASE_TIME.plusMinutes(120), null, OK)
                    )
                );
        }

        @Test
        @DisplayName("should handle consecutive events with same status")
        void shouldHandleConsecutiveEventsWithSameStatus() {
            // Given: events with repeated statuses
            final List<EventRequest> events = List.of(
                createEvent(0, OK),
                createEvent(30, OK),
                createEvent(60, WARNING),
                createEvent(90, WARNING),
                createEvent(120, CRITICAL),
                createEvent(150, CRITICAL)
            );

            // When: calculating the timeline
            final TimelineResponse result = TimelineConsolidator.calculateTimeline(events);

            // Then: it should merge consecutive durations with same status
            assertThat(result.getDurations())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(
                    List.of(
                        new TimelineDurationResponse(BASE_TIME, BASE_TIME.plusMinutes(60), OK),
                        new TimelineDurationResponse(BASE_TIME.plusMinutes(60), BASE_TIME.plusMinutes(120), WARNING),
                        new TimelineDurationResponse(BASE_TIME.plusMinutes(120), null, CRITICAL)
                    )
                );
        }

        @Test
        @DisplayName("should handle empty event list")
        public void shouldHandleEmptyEventList() {
            // Given: an empty list of events
            final List<EventRequest> events = List.of();

            // When: calculating the timeline
            final TimelineResponse result = TimelineConsolidator.calculateTimeline(events);

            // Then: the result should be an empty timeline
            assertThat(result.getDurations())
                .isNotNull()
                .hasSize(1)
                .first()
                .satisfies(duration -> {
                    assertThat(duration.getStartTime()).isNotNull();
                    assertThat(duration.getEndTime()).isNull();
                    assertThat(duration.getStatus()).isEqualTo(UNKNOWN);
                });
        }

        @Test
        @DisplayName("should handle null event list")
        public void shouldHandleNullEventList() {
            // Given: a null list of events
            final List<EventRequest> events = null;

            // When: calculating the timeline
            final TimelineResponse result = TimelineConsolidator.calculateTimeline(events);

            // Then: the result should be an empty timeline
            assertThat(result.getDurations())
                .isNotNull()
                .hasSize(1)
                .first()
                .satisfies(duration -> {
                    assertThat(duration.getStartTime()).isNotNull();
                    assertThat(duration.getEndTime()).isNull();
                    assertThat(duration.getStatus()).isEqualTo(UNKNOWN);
                });
        }
    }
}
