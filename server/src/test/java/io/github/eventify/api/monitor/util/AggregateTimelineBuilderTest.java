package io.github.eventify.api.monitor.util;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.BucketSize;
import io.github.eventify.api.monitor.model.TimeSpan;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineBucket;
import io.github.eventify.api.monitor.model.TimelineDuration;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for AggregateTimelineBuilder utility class.
 * Tests timeline construction from pre-computed time-series buckets.
 */
@DisplayName("Unit Test - Aggregate Timeline Builder")
class AggregateTimelineBuilderTest extends UnitTest {

    @Test
    @DisplayName("Should return NO_DATA timeline when no buckets provided")
    void shouldReturnNoDataTimelineWhenNoBucketsProvided() {
        // Given: No buckets, a live 12h range, and a 30min bucket size
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(12);
        final TimeSpan range = new TimeSpan(rangeStart, rangeEnd);
        final BucketSize bucketSize = BucketSize.PT30M;

        // When: Building timeline from empty bucket list
        final Timeline timeline = AggregateTimelineBuilder.fromBuckets(List.of(), range, bucketSize);

        // Then: Should have single NO_DATA duration spanning full range
        assertThat(timeline, is(notNullValue()));
        assertThat(timeline.getDurations(), hasSize(1));

        final TimelineDuration duration = timeline.getDurations().getFirst();
        assertThat(duration.getSeverity(), is(equalTo(Severity.NO_DATA)));
        assertThat(duration.getStartTime(), is(equalTo(rangeStart)));
        assertThat(duration.getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should create single duration from one bucket with same first and last severity")
    void shouldCreateSingleDurationFromOneBucket() {
        // Given: One bucket where first and last severity are the same
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(12);
        final TimeSpan range = new TimeSpan(rangeStart, rangeEnd);
        final BucketSize bucketSize = BucketSize.PT30M;

        final TimelineBucket bucket = aTimelineBucket(1, rangeStart, "OK", "OK", 5L);

        // When: Building timeline from single bucket
        final Timeline timeline = AggregateTimelineBuilder.fromBuckets(List.of(bucket), range, bucketSize);

        // Then: Should have one duration for the bucket with OK severity
        final TimelineDuration duration = timeline.getDurations().stream()
            .filter(d -> d.getSeverity() == Severity.OK)
            .findFirst()
            .orElseThrow();
        assertThat(duration.getSeverity(), is(equalTo(Severity.OK)));
    }

    @Test
    @DisplayName("Should merge consecutive buckets with same severity into one duration")
    void shouldMergeConsecutiveBucketsWithSameSeverity() {
        // Given: 3 consecutive OK buckets
        final OffsetDateTime rangeEnd = OffsetDateTime.now().minusMinutes(5);
        final OffsetDateTime rangeStart = rangeEnd.minusHours(12);
        final TimeSpan range = new TimeSpan(rangeStart, rangeEnd);
        final BucketSize bucketSize = BucketSize.PT30M;

        final List<TimelineBucket> buckets = List.of(
            aTimelineBucket(1, rangeStart, "OK", "OK", 3L),
            aTimelineBucket(1, rangeStart.plusMinutes(30), "OK", "OK", 2L),
            aTimelineBucket(1, rangeStart.plusMinutes(60), "OK", "OK", 4L)
        );

        // When: Building timeline from consecutive same-severity buckets
        final Timeline timeline = AggregateTimelineBuilder.fromBuckets(buckets, range, bucketSize);

        // Then: Should be merged into a single OK duration
        final long okDurationCount = timeline.getDurations().stream()
            .filter(d -> d.getSeverity() == Severity.OK)
            .count();
        assertThat(okDurationCount, is(equalTo(1L)));
    }

    @Test
    @DisplayName("Should create NO_DATA gap between non-adjacent buckets")
    void shouldCreateNoDataGapBetweenNonAdjacentBuckets() {
        // Given: Two buckets with a gap between them
        final OffsetDateTime rangeEnd = OffsetDateTime.now().minusMinutes(5);
        final OffsetDateTime rangeStart = rangeEnd.minusHours(12);
        final TimeSpan range = new TimeSpan(rangeStart, rangeEnd);
        final BucketSize bucketSize = BucketSize.PT30M;

        // Bucket at start and bucket 2 hours later — 1.5h gap between them
        final List<TimelineBucket> buckets = List.of(
            aTimelineBucket(1, rangeStart, "OK", "OK", 2L),
            aTimelineBucket(1, rangeStart.plusHours(2), "WARNING", "WARNING", 3L)
        );

        // When: Building timeline
        final Timeline timeline = AggregateTimelineBuilder.fromBuckets(buckets, range, bucketSize);

        // Then: Should have a NO_DATA gap between the two buckets
        final boolean hasNoDataGap = timeline.getDurations().stream()
            .anyMatch(
                d -> d.getSeverity() == Severity.NO_DATA
                    && d.getStartTime().isAfter(rangeStart)
                    && d.getStartTime().isBefore(rangeStart.plusHours(2))
            );
        assertThat(hasNoDataGap, is(true));
    }

    @Test
    @DisplayName("Should use prior bucket's last severity as initial state instead of NO_DATA")
    void shouldUsePriorBucketSeverityForInitialState() {
        // Given: A prior bucket (before range start) and no bucket at range start
        final OffsetDateTime rangeEnd = OffsetDateTime.now().minusMinutes(5);
        final OffsetDateTime rangeStart = rangeEnd.minusHours(12);
        final TimeSpan range = new TimeSpan(rangeStart, rangeEnd);
        final BucketSize bucketSize = BucketSize.PT30M;

        // Prior bucket is before range start
        final OffsetDateTime priorBucketTime = rangeStart.minusMinutes(30);
        final List<TimelineBucket> buckets = List.of(
            aTimelineBucket(1, priorBucketTime, "WARNING", "WARNING", 2L), // prior
            aTimelineBucket(1, rangeStart.plusHours(2), "OK", "OK", 3L)   // in range
        );

        // When: Building timeline from buckets including prior bucket
        final Timeline timeline = AggregateTimelineBuilder.fromBuckets(buckets, range, bucketSize);

        // Then: Initial state before first in-range bucket should be WARNING (not NO_DATA)
        final TimelineDuration firstDuration = timeline.getDurations().getFirst();
        assertThat(firstDuration.getSeverity(), is(not(equalTo(Severity.NO_DATA))));
        assertThat(firstDuration.getSeverity(), is(equalTo(Severity.WARNING)));
    }

    @Test
    @DisplayName("Should extend last duration to range end in live mode")
    void shouldExtendLastDurationToRangeEndInLiveMode() {
        // Given: One bucket in a live range (end = now)
        final OffsetDateTime rangeEnd = OffsetDateTime.now();
        final OffsetDateTime rangeStart = rangeEnd.minusHours(12);
        final TimeSpan range = new TimeSpan(rangeStart, rangeEnd);
        final BucketSize bucketSize = BucketSize.PT30M;

        final TimelineBucket bucket = aTimelineBucket(1, rangeStart, "CRITICAL", "CRITICAL", 2L);

        // When: Building timeline in live mode
        final Timeline timeline = AggregateTimelineBuilder.fromBuckets(List.of(bucket), range, bucketSize);

        // Then: Last duration should extend to rangeEnd
        final TimelineDuration lastDuration = timeline.getDurations().getLast();
        assertThat(lastDuration.getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should extend last duration to range end in non-live mode")
    void shouldExtendLastDurationToRangeEndInNonLiveMode() {
        // Given: One bucket in a non-live range (end is in the past)
        final OffsetDateTime rangeEnd = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5);
        final OffsetDateTime rangeStart = rangeEnd.minusHours(12);
        final TimeSpan range = new TimeSpan(rangeStart, rangeEnd);
        final BucketSize bucketSize = BucketSize.PT30M;

        // Place bucket at range start — the duration should extend to rangeEnd
        final TimelineBucket bucket = aTimelineBucket(1, rangeStart, "OK", "OK", 3L);

        // When: Building timeline in non-live mode
        final Timeline timeline = AggregateTimelineBuilder.fromBuckets(List.of(bucket), range, bucketSize);

        // Then: Last OK duration should extend to rangeEnd (last known severity fills the gap)
        final TimelineDuration okDuration = timeline.getDurations().stream()
            .filter(d -> d.getSeverity() == Severity.OK)
            .findFirst()
            .orElseThrow();
        assertThat(okDuration.getEndTime(), is(equalTo(rangeEnd)));
    }

    @Test
    @DisplayName("Should use lastSeverity from prior bucket (not firstSeverity) for initial state")
    void shouldUseLastSeverityFromPriorBucket() {
        // Given: Prior bucket with firstSeverity=OK but lastSeverity=CRITICAL
        final OffsetDateTime rangeEnd = OffsetDateTime.now().minusMinutes(5);
        final OffsetDateTime rangeStart = rangeEnd.minusHours(12);
        final TimeSpan range = new TimeSpan(rangeStart, rangeEnd);
        final BucketSize bucketSize = BucketSize.PT30M;

        // Prior bucket ends in CRITICAL state
        final TimelineBucket priorBucket = aTimelineBucket(
            1,
            rangeStart.minusMinutes(30),
            "OK",
            "CRITICAL",
            5L
        );
        // No in-range buckets — all should reflect prior state
        final List<TimelineBucket> buckets = List.of(priorBucket);

        // When: Building timeline
        final Timeline timeline = AggregateTimelineBuilder.fromBuckets(buckets, range, bucketSize);

        // Then: Initial state should use lastSeverity=CRITICAL (not firstSeverity=OK)
        assertThat(timeline.getDurations(), hasSize(1));
        assertThat(timeline.getDurations().getFirst().getSeverity(), is(equalTo(Severity.CRITICAL)));
    }

    // ========================= HELPER METHODS =========================

    private TimelineBucket aTimelineBucket(
        final long channelId,
        final OffsetDateTime bucket,
        final String firstSeverity,
        final String lastSeverity,
        final long eventCount
    ) {
        return TimelineBucket.of(
            channelId,
            bucket,
            firstSeverity,
            lastSeverity,
            eventCount,
            bucket,
            bucket.plusMinutes(29)
        );
    }
}
