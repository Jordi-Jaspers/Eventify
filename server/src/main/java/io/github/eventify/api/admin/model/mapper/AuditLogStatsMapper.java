package io.github.eventify.api.admin.model.mapper;

import io.github.eventify.api.admin.model.response.AuditLogStatsProjection;
import io.github.eventify.api.admin.model.response.AuditLogStatsResponse;
import io.github.eventify.api.admin.model.response.HourlyBucketProjection;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.mapstruct.Mapper;

/** MapStruct mapper for converting audit log stats projections to response objects. */
@Mapper(config = SharedMapperConfig.class)
public abstract class AuditLogStatsMapper {

    /** Converts stats projection and hourly buckets to a response object. */
    public AuditLogStatsResponse toResponse(
        final AuditLogStatsProjection stats,
        final List<HourlyBucketProjection> buckets) {
        final List<AuditLogStatsResponse.HourlyBucket> hourlyBuckets = buckets.stream()
            .map(this::toBucket)
            .toList();

        return new AuditLogStatsResponse()
            .setTotalRequests(stats.getTotalRequests())
            .setErrorCount(stats.getErrorCount())
            .setMutationCount(stats.getMutationCount())
            .setUniqueActors(stats.getUniqueActors())
            .setHourlyBuckets(hourlyBuckets);
    }

    /** Converts a single hourly bucket projection to a response object. */
    private AuditLogStatsResponse.HourlyBucket toBucket(final HourlyBucketProjection projection) {
        return new AuditLogStatsResponse.HourlyBucket()
            .setHour(projection.getHour() != null ? OffsetDateTime.ofInstant(projection.getHour(), ZoneOffset.UTC) : null)
            .setTotal(projection.getTotal())
            .setErrors(projection.getErrors());
    }
}
