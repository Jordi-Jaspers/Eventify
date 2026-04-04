package io.github.eventify.api.monitor.util;

import io.github.eventify.api.monitor.model.BucketSize;
import io.github.eventify.api.monitor.model.TimeSpan;
import lombok.experimental.UtilityClass;

import java.time.Duration;

/**
 * Utility class for selecting the appropriate LOD (Level of Detail) bucket size
 * for a given time range. Returns null for short ranges where raw events are used,
 * or a BucketSize for longer ranges using aggregated data.
 *
 * <p>LOD mapping:
 * <ul>
 * <li>&lt;= 4h: null (raw events)</li>
 * <li>&lt;= 24h: PT30M buckets</li>
 * <li>&lt;= 7d: PT2H buckets</li>
 * <li>&gt; 7d: PT4H buckets</li>
 * </ul>
 */
@UtilityClass
public class LodSelector {

    /**
     * Selects the bucket size for the given time range.
     * Returns null if raw events should be used (range &lt;= 4h).
     *
     * @param timeRange the time span to select LOD for
     * @return BucketSize for aggregate queries, or null for raw events
     */
    public BucketSize selectBucket(final TimeSpan timeRange) {
        final Duration effective = Duration.between(timeRange.getStart(), timeRange.getEnd()).abs();

        final BucketSize result;
        if (effective.compareTo(Duration.ofHours(4)) <= 0) {
            result = null;
        } else if (effective.compareTo(Duration.ofHours(24)) <= 0) {
            result = BucketSize.PT30M;
        } else if (effective.compareTo(Duration.ofDays(7)) <= 0) {
            result = BucketSize.PT2H;
        } else {
            result = BucketSize.PT4H;
        }
        return result;
    }
}
