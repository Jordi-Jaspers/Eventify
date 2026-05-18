package io.github.eventify.api.admin.model.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/** Container holding audit log stats projection and hourly bucket data. */
@Getter
@RequiredArgsConstructor
public class AuditLogStatsData {

    private final AuditLogStatsProjection stats;

    private final List<HourlyBucketProjection> buckets;

}
