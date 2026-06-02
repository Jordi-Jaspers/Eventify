package io.github.eventify.api.organization.model.mapper;

import io.github.eventify.api.organization.model.OrgApiKeyStatistics;
import io.github.eventify.api.organization.model.OrgErrorRateBucket;
import io.github.eventify.api.organization.model.OrgSummary;
import io.github.eventify.api.organization.model.OrgTimeline;
import io.github.eventify.api.organization.model.OrgTimelineBucketData;
import io.github.eventify.api.organization.model.OrgTopKey;
import io.github.eventify.api.organization.model.response.OrgApiKeyStatsResponse;
import io.github.eventify.api.organization.model.response.OrgErrorRateBucketResponse;
import io.github.eventify.api.organization.model.response.OrgSummaryResponse;
import io.github.eventify.api.organization.model.response.OrgTimelineBucketResponse;
import io.github.eventify.api.organization.model.response.OrgTimelineResponse;
import io.github.eventify.api.organization.model.response.OrgTopApiKeyResponse;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;

/** MapStruct mapper for org stats domain objects to response DTOs. */
@Mapper(config = SharedMapperConfig.class)
public abstract class OrgStatsMapper {

    /** Maps OrgTimeline domain object to OrgTimelineResponse DTO. */
    public abstract OrgTimelineResponse toTimelineResponse(OrgTimeline timeline);

    /** Maps OrgSummary domain object to OrgSummaryResponse DTO. */
    public abstract OrgSummaryResponse toSummaryResponse(OrgSummary summary);

    /** Maps OrgApiKeyStatistics domain object to OrgApiKeyStatsResponse DTO. */
    public abstract OrgApiKeyStatsResponse toApiKeyStatsResponse(OrgApiKeyStatistics stats);

    /** Maps OrgTopKey domain object to OrgTopApiKeyResponse DTO. */
    public abstract OrgTopApiKeyResponse toTopApiKeyResponse(OrgTopKey key);

    /** Maps list of OrgTopKey to list of OrgTopApiKeyResponse DTOs. */
    public abstract List<OrgTopApiKeyResponse> toTopApiKeyResponseList(List<OrgTopKey> keys);

    /** Maps OrgTimelineBucketData domain object to OrgTimelineBucketResponse DTO. */
    public abstract OrgTimelineBucketResponse toTimelineBucket(OrgTimelineBucketData data);

    /** Maps list of OrgTimelineBucketData to list of OrgTimelineBucketResponse DTOs. */
    public abstract List<OrgTimelineBucketResponse> toTimelineBuckets(List<OrgTimelineBucketData> data);

    /** Maps OrgErrorRateBucket domain object to OrgErrorRateBucketResponse DTO. */
    public abstract OrgErrorRateBucketResponse toErrorRateBucket(OrgErrorRateBucket bucket);

    /** Maps list of OrgErrorRateBucket to list of OrgErrorRateBucketResponse DTOs. */
    public abstract List<OrgErrorRateBucketResponse> toErrorRateBuckets(List<OrgErrorRateBucket> buckets);
}
