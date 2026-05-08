package io.github.eventify.api.monitor.util;

import io.github.eventify.api.monitor.model.BucketSize;
import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.monitor.model.TimeSpan;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for LodSelector utility class.
 * Tests LOD (Level of Detail) bucket selection based on time range span.
 */
@DisplayName("Unit Test - LodSelector")
class LodSelectorTest extends UnitTest {

    @Test
    @DisplayName("Should return null for last 2h range (raw events)")
    void shouldReturnNullForLast2hRange() {
        // Given: A 2h preset time span
        final OffsetDateTime end = OffsetDateTime.now();
        final OffsetDateTime start = end.minus(TimeRange.LAST_2H.getDuration());
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return null (use raw events)
        assertThat(bucket, is(nullValue()));
    }

    @Test
    @DisplayName("Should return null for last 4h range (raw events)")
    void shouldReturnNullForLast4hRange() {
        // Given: A 4h preset time span
        final OffsetDateTime end = OffsetDateTime.now();
        final OffsetDateTime start = end.minus(TimeRange.LAST_4H.getDuration());
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return null (use raw events)
        assertThat(bucket, is(nullValue()));
    }

    @Test
    @DisplayName("Should return 30min bucket for last 12h range")
    void shouldReturn30MinBucketForLast12hRange() {
        // Given: A 12h preset time span
        final OffsetDateTime end = OffsetDateTime.now();
        final OffsetDateTime start = end.minus(TimeRange.LAST_12H.getDuration());
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return PT30M
        assertThat(bucket, is(equalTo(BucketSize.PT30M)));
    }

    @Test
    @DisplayName("Should return 30min bucket for last 24h range")
    void shouldReturn30MinBucketForLast24hRange() {
        // Given: A 24h preset time span
        final OffsetDateTime end = OffsetDateTime.now();
        final OffsetDateTime start = end.minus(TimeRange.LAST_24H.getDuration());
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return PT30M
        assertThat(bucket, is(equalTo(BucketSize.PT30M)));
    }

    @Test
    @DisplayName("Should return 2h bucket for last 7d range")
    void shouldReturn2HourBucketForLast7dRange() {
        // Given: A 7d preset time span
        final OffsetDateTime end = OffsetDateTime.now();
        final OffsetDateTime start = end.minus(TimeRange.LAST_7D.getDuration());
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return PT2H
        assertThat(bucket, is(equalTo(BucketSize.PT2H)));
    }

    @Test
    @DisplayName("Should return 4h bucket for last 30d range")
    void shouldReturn4HourBucketForLast30dRange() {
        // Given: A 30d preset time span
        final OffsetDateTime end = OffsetDateTime.now();
        final OffsetDateTime start = end.minus(TimeRange.LAST_30D.getDuration());
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return PT4H
        assertThat(bucket, is(equalTo(BucketSize.PT4H)));
    }

    @Test
    @DisplayName("Should return null for custom range under 4h")
    void shouldReturnNullForCustomRangeUnder4h() {
        // Given: A custom 3h span
        final OffsetDateTime end = OffsetDateTime.now().minusMinutes(5);
        final OffsetDateTime start = end.minusHours(3);
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return null (raw events for ranges <= 4h)
        assertThat(bucket, is(nullValue()));
    }

    @Test
    @DisplayName("Should return 30min bucket for custom range of 12h")
    void shouldReturn30MinBucketForCustomRange12h() {
        // Given: A custom 12h span
        final OffsetDateTime end = OffsetDateTime.now().minusMinutes(5);
        final OffsetDateTime start = end.minusHours(12);
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return PT30M
        assertThat(bucket, is(equalTo(BucketSize.PT30M)));
    }

    @Test
    @DisplayName("Should return 2h bucket for custom range of 5 days")
    void shouldReturn2HourBucketForCustomRange5d() {
        // Given: A custom 5-day span
        final OffsetDateTime end = OffsetDateTime.now().minusMinutes(5);
        final OffsetDateTime start = end.minusDays(5);
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return PT2H
        assertThat(bucket, is(equalTo(BucketSize.PT2H)));
    }

    @Test
    @DisplayName("Should return 4h bucket for custom range of 20 days")
    void shouldReturn4HourBucketForCustomRange20d() {
        // Given: A custom 20-day span
        final OffsetDateTime end = OffsetDateTime.now().minusMinutes(5);
        final OffsetDateTime start = end.minusDays(20);
        final TimeSpan timeSpan = new TimeSpan(start, end);

        // When: Selecting bucket size
        final BucketSize bucket = LodSelector.selectBucket(timeSpan);

        // Then: Should return PT4H
        assertThat(bucket, is(equalTo(BucketSize.PT4H)));
    }
}
