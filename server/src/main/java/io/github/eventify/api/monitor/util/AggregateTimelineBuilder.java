package io.github.eventify.api.monitor.util;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.BucketSize;
import io.github.eventify.api.monitor.model.TimeSpan;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineBucket;
import io.github.eventify.api.monitor.model.TimelineDuration;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds aggregate timelines from pre-computed time-series buckets.
 * Extracted from TimelineBuilder to separate aggregate vs raw-event logic.
 */
@UtilityClass
public class AggregateTimelineBuilder {

    /**
     * Builds a timeline from pre-aggregated time buckets.
     * Used by the LOD query layer for longer time ranges.
     *
     * @param buckets    the list of aggregated buckets (may include one prior bucket before range start)
     * @param range      the time span with start, end, and live mode
     * @param bucketSize the bucket size enum (e.g. PT30M, PT2H)
     * @return a Timeline with severity durations
     */
    public Timeline fromBuckets(
        final List<TimelineBucket> buckets,
        final TimeSpan range,
        final BucketSize bucketSize
    ) {
        final OffsetDateTime rangeStart = range.getStart();
        final OffsetDateTime rangeEnd = range.getEnd();

        if (buckets == null || buckets.isEmpty()) {
            return Timeline.builder()
                .durations(List.of(TimelineDuration.of(Severity.NO_DATA, rangeStart, rangeEnd)))
                .build();
        }

        final Duration bucketDuration = bucketSize.getDuration();

        final List<TimelineBucket> sortedBuckets = buckets.stream()
            .sorted(Comparator.comparing(TimelineBucket::getBucketTime))
            .toList();

        final TimelineBucket priorBucket = sortedBuckets.stream()
            .filter(b -> b.getBucketTime().isBefore(rangeStart))
            .reduce((a, b) -> b)
            .orElse(null);

        final List<TimelineBucket> inRangeBuckets = sortedBuckets.stream()
            .filter(b -> !b.getBucketTime().isBefore(rangeStart) && b.getBucketTime().isBefore(rangeEnd))
            .toList();

        final Severity initialSeverity = priorBucket == null
            ? Severity.NO_DATA
            : Severity.fromString(priorBucket.getLastSeverity());

        final List<TimelineDuration> durations;
        if (inRangeBuckets.isEmpty()) {
            durations = new ArrayList<>(List.of(TimelineDuration.of(initialSeverity, rangeStart, rangeEnd)));
        } else {
            durations = buildDurationsFromBuckets(inRangeBuckets, rangeStart, rangeEnd, bucketDuration, initialSeverity);
            if (!durations.isEmpty()) {
                durations.getLast().setEndTime(rangeEnd);
            }
        }

        return Timeline.builder().durations(durations).build();
    }

    private List<TimelineDuration> buildDurationsFromBuckets(
        final List<TimelineBucket> inRangeBuckets,
        final OffsetDateTime rangeStart,
        final OffsetDateTime rangeEnd,
        final Duration bucketDuration,
        final Severity initialSeverity
    ) {
        final List<TimelineDuration> durations = new ArrayList<>();

        final OffsetDateTime firstBucketStart = inRangeBuckets.getFirst().getBucketTime();
        if (firstBucketStart.isAfter(rangeStart)) {
            durations.add(TimelineDuration.of(initialSeverity, rangeStart, firstBucketStart));
        }

        OffsetDateTime prevEnd = firstBucketStart;
        Severity prevSeverity = null;

        for (final TimelineBucket bucket : inRangeBuckets) {
            final OffsetDateTime bucketStart = bucket.getBucketTime();
            final OffsetDateTime naturalEnd = bucketStart.plus(bucketDuration);
            final OffsetDateTime bucketEnd = naturalEnd.isAfter(rangeEnd) ? rangeEnd : naturalEnd;

            if (bucketStart.isAfter(prevEnd)) {
                final Severity gapSeverity = prevSeverity != null ? prevSeverity : initialSeverity;
                durations.add(TimelineDuration.of(gapSeverity, prevEnd, bucketStart));
            }

            final Severity bucketSeverity = Severity.fromString(bucket.getLastSeverity());

            if (prevSeverity != null && prevSeverity == bucketSeverity) {
                durations.getLast().setEndTime(bucketEnd);
            } else {
                durations.add(
                    new TimelineDuration()
                        .setSeverity(bucketSeverity)
                        .setStartTime(bucketStart)
                        .setEndTime(bucketEnd)
                );
                prevSeverity = bucketSeverity;
            }

            prevEnd = bucketEnd;
        }

        return durations;
    }

}
