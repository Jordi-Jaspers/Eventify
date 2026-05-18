package io.github.eventify.api.admin.model.response;

import java.time.Instant;

/**
 * Interface projection for hourly bucket native query results.
 */
public interface HourlyBucketProjection {

    /** Returns the start of the hour bucket. */
    Instant getHour();

    /** Returns the total number of requests in this hour. */
    Long getTotal();

    /** Returns the number of error requests in this hour. */
    Long getErrors();
}
