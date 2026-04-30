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
        final List<TimelineDuration> durations = computeDurationsFromBuckets(buckets, range, bucketSize.getDuration());
        return Timeline.builder().durations(durations).build();
    }

    private List<TimelineDuration> computeDurationsFromBuckets(
        final List<TimelineBucket> buckets,
        final TimeSpan range,
        final Duration bucketDuration
    ) {
        if (buckets == null || buckets.isEmpty()) {
            return List.of(TimelineDuration.of(Severity.NO_DATA, range.getStart(), range.getEnd()));
        }
        return buildFromNonEmptyBuckets(buckets, range, bucketDuration);
    }

    private List<TimelineDuration> buildFromNonEmptyBuckets(
        final List<TimelineBucket> buckets,
        final TimeSpan range,
        final Duration bucketDuration
    ) {
        final OffsetDateTime rangeStart = range.getStart();
        final OffsetDateTime rangeEnd = range.getEnd();

        final List<TimelineBucket> sortedBuckets = buckets.stream()
            .sorted(Comparator.comparing(TimelineBucket::getBucketTime))
            .toList();

        final TimelineBucket priorBucket = findPriorBucket(sortedBuckets, rangeStart);
        final List<TimelineBucket> inRangeBuckets = filterInRangeBuckets(sortedBuckets, rangeStart, rangeEnd);
        final Severity initialSeverity = resolveInitialSeverity(priorBucket);

        if (inRangeBuckets.isEmpty()) {
            return List.of(TimelineDuration.of(initialSeverity, range.getStart(), range.getEnd()));
        }

        final List<TimelineDuration> durations = buildDurationsFromBuckets(
            inRangeBuckets,
            range.getStart(),
            range.getEnd(),
            bucketDuration,
            initialSeverity
        );

        if (!durations.isEmpty()) {
            durations.getLast().setEndTime(range.getEnd());
        }

        return durations;
    }

    private TimelineBucket findPriorBucket(
        final List<TimelineBucket> sortedBuckets,
        final OffsetDateTime rangeStart
    ) {
        return sortedBuckets.stream()
            .filter(b -> b.getBucketTime().isBefore(rangeStart))
            .reduce((a, b) -> b)
            .orElse(null);
    }

    private List<TimelineBucket> filterInRangeBuckets(
        final List<TimelineBucket> sortedBuckets,
        final OffsetDateTime start,
        final OffsetDateTime end
    ) {
        return sortedBuckets.stream()
            .filter(b -> !b.getBucketTime().isBefore(start) && b.getBucketTime().isBefore(end))
            .toList();
    }

    private Severity resolveInitialSeverity(final TimelineBucket priorBucket) {
        if (priorBucket == null) {
            return Severity.NO_DATA;
        }
        return Severity.fromString(priorBucket.getLastSeverity());
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
            final OffsetDateTime bucketEnd = computeBucketEnd(bucketStart, bucketDuration, rangeEnd);

            if (bucketStart.isAfter(prevEnd)) {
                durations.add(TimelineDuration.of(Severity.NO_DATA, prevEnd, bucketStart));
                prevSeverity = null;
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

    private OffsetDateTime computeBucketEnd(
        final OffsetDateTime bucketStart,
        final Duration bucketDuration,
        final OffsetDateTime rangeEnd
    ) {
        final OffsetDateTime naturalEnd = bucketStart.plus(bucketDuration);
        return naturalEnd.isAfter(rangeEnd) ? rangeEnd : naturalEnd;
    }

}
