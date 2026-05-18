package io.github.eventify.api.admin.model.response;

/**
 * Interface projection for aggregate audit log stats native query.
 */
public interface AuditLogStatsProjection {

    /** Returns the total number of requests. */
    Long getTotalRequests();

    /** Returns the number of error responses (status >= 400). */
    Long getErrorCount();

    /** Returns the number of mutating requests (POST, PUT, PATCH, DELETE). */
    Long getMutationCount();

    /** Returns the number of distinct actors. */
    Long getUniqueActors();
}
